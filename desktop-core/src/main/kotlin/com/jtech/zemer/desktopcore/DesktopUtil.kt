package com.jtech.zemer.desktopcore

import java.io.File

/**
 * Small desktop utility helpers.
 */
object DesktopUtil {
    /**
     * Returns true if ffplay or ffmpeg is available on PATH.
     */
    fun isFfmpegAvailable(): Boolean {
        val candidates = listOf("ffplay", "ffmpeg")
        val pathEnv = System.getenv("PATH") ?: return false
        val segments = pathEnv.split(File.pathSeparatorChar)
        for (dir in segments) {
            for (candidate in candidates) {
                val target = File(dir, candidate)
                if (target.exists() && target.canExecute()) return true
                val exeTarget = File(dir, "$candidate.exe")
                if (exeTarget.exists() && exeTarget.canExecute()) return true
            }
        }
        return false
    }
}
