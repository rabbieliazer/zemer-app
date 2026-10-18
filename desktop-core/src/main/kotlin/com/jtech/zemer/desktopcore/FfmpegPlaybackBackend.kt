package com.jtech.zemer.desktopcore

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lightweight ffmpeg/ffplay launcher with basic supervision and a single retry on failure.
 *
 * Behavior:
 * - Looks for ffplay then ffmpeg on PATH.
 * - Launches a process and streams its stdout/stderr to the JVM output for diagnostics.
 * - If the process exits with a non-zero code, retries once.
 * - pause() / stop() both stop the process. Pause is best-effort due to subprocess approach.
 *
 * This is intentionally small and dependency-free; it expects system ffplay/ffmpeg to be installed.
 */
class FfmpegPlaybackBackend : PlaybackBackend {
    @Volatile
    private var process: Process? = null
    private val running = AtomicBoolean(false)
    private val retried = AtomicBoolean(false)
    private val outputPool = Executors.newCachedThreadPool()

    override val isPlaying: Boolean
        get() = running.get()

    override fun play(url: String) {
        stop()
        retried.set(false)
        launchProcessFor(url)
    }

    private fun launchProcessFor(url: String) {
        val commandName = findPlayableCommand() ?: run {
            println("[FfmpegPlayback] no ffplay/ffmpeg found on PATH; playback disabled")
            return
        }

        val cmd = when (commandName) {
            "ffplay" -> listOf(commandName, "-nodisp", "-autoexit", url)
            else -> listOf(commandName, "-y", "-v", "quiet", "-i", url, "-f", "null", "-")
        }

        try {
            val pb = ProcessBuilder(cmd)
                .redirectErrorStream(true)
            val p = pb.start()
            process = p
            running.set(true)

            // Stream the process output asynchronously so logs are visible for debugging.
            outputPool.submit {
                try {
                    BufferedReader(InputStreamReader(p.inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            println("[Ffmpeg] $line")
                            line = reader.readLine()
                        }
                    }
                } catch (e: IOException) {
                    // Ignore read errors on shutdown
                }
            }

            // Watcher thread to react on exit
            outputPool.submit {
                try {
                    val exit = p.waitFor()
                    running.set(false)
                    process = null
                    if (exit != 0 && !retried.get()) {
                        println("[FfmpegPlayback] process exited with $exit, retrying once")
                        retried.set(true)
                        // small backoff
                        Thread.sleep(250)
                        launchProcessFor(url)
                    } else if (exit != 0) {
                        println("[FfmpegPlayback] process exited with $exit after retry")
                    } else {
                        println("[FfmpegPlayback] process exited with 0")
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }

        } catch (e: IOException) {
            println("[FfmpegPlayback] failed to start process: ${e.message}")
            running.set(false)
            process = null
        }
    }

    override fun pause() {
        // Best-effort: stop the process. A true pause/resume requires a player with IPC.
        stop()
    }

    override fun stop() {
        try {
            process?.let {
                try {
                    it.destroy()
                } catch (_: Throwable) {
                }
            }
        } finally {
            process = null
            running.set(false)
        }
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
        val segments = pathEnv.split(java.io.File.pathSeparatorChar)
        for (dir in segments) {
            val target = java.io.File(dir, program)
            if (target.exists() && target.canExecute()) return target.absolutePath
            val exeTarget = java.io.File(dir, "$program.exe")
            if (exeTarget.exists() && exeTarget.canExecute()) return exeTarget.absolutePath
        }
        return null
    }
}
