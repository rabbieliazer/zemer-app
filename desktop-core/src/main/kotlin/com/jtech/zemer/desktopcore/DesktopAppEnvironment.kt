package com.jtech.zemer.desktopcore

import java.io.File
import java.io.IOException

class DesktopAppEnvironment(private val baseDir: File = File(System.getProperty("user.home"), ".zemer-desktop")) {
    val database = DesktopDatabase(File(baseDir, "music.db"))
    val settings = DesktopSettingsStore(File(baseDir, "settings.properties"))
    val playback: PlaybackBackend = FfmpegPlaybackBackend()
    val storage: StorageBackend = DesktopStorageStub()

    init {
        baseDir.mkdirs()
    }
}
