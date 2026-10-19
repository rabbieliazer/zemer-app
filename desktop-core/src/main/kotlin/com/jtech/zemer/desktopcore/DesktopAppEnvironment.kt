package com.jtech.zemer.desktopcore

import java.io.File

class DesktopAppEnvironment(private val baseDir: File = File(System.getProperty("user.home"), ".zemer-desktop")) {
    val database = DesktopDatabase(File(baseDir, "music.db"))
    val settings = DesktopSettingsStore(File(baseDir, "settings.properties"))
    val playback: PlaybackBackend = FfmpegPlaybackBackend()
    val storage: StorageBackend = DesktopStorageStub()
    val resolver: StreamingResolver = DesktopStreamingResolverImpl(File("."))
    val playableUrlProvider: PlayableUrlProvider = FileBackedPlayableUrlProvider(File("."))

    init {
        baseDir.mkdirs()
    }
}
