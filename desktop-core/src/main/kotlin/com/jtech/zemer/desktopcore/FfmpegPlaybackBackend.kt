package com.jtech.zemer.desktopcore

import java.io.File
import java.util.Locale

class FfmpegPlaybackBackend : PlaybackBackend {
    private var process: Process? = null

    override val isPlaying: Boolean
        get() = process != null && process.isAlive

    override fun play(url: String) {
        stop()
        val command = findPlayableCommand() ?: run {
            println("[DesktopPlayback] no ffplay/ffmpeg found on PATH; playback disabled")
            return
        }

        val cmd = when (command) {
            "ffplay" -> listOf(command, "-nodisp", "-autoexit", url)
            else -> listOf(command, "-y", "-v", "quiet", "-i", url, "-f", "null", "-")
        }

        try {
            process = ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start()
            println("[DesktopPlayback] started: ${cmd.joinToString(separator = " ")}")
        } catch (e: IOException) {
            println("[DesktopPlayback] failed to start process: ${e.message}")
        }
    }

    override fun pause() {
        process?.destroy()
        process = null
    }

    override fun stop() {
        process?.destroy()
        process = null
    }

    private fun findPlayableCommand(): String? {
        val candidates = listOf("ffplay", "ffmpeg")
        for (candidate in candidates) {
            val resolved = findExecutable(candidate)
            if (resolved != null) return candidate
        }
        return null
    }

    private fun findExecutable(program: String): String? {
        val pathEnv = System.getenv("PATH") ?: return null
        val segments = pathEnv.split(File.pathSeparatorChar)
        for (dir in segments) {
            val target = File(dir, program)
            if (target.exists() && target.canExecute()) return target.absolutePath
            val exeTarget = File(dir, "$program.exe")
            if (exeTarget.exists() && exeTarget.canExecute()) return exeTarget.absolutePath
        }
        return null
    }
}
