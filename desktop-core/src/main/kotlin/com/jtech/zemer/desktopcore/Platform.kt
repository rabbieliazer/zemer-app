package com.jtech.zemer.desktopcore

interface PlaybackBackend {
    fun play(url: String)
    fun pause()
    fun stop()
    val isPlaying: Boolean
}

interface StorageBackend {
    fun saveFile(path: String, bytes: ByteArray): Boolean
    fun readFile(path: String): ByteArray?
}

fun getDefaultPlayback(): PlaybackBackend = DesktopPlaybackStub()

fun getDefaultStorage(): StorageBackend = DesktopStorageStub()
