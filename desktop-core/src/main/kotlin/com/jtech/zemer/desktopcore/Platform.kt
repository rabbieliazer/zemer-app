package com.jtech.zemer.desktopcore

import java.io.File

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

fun getDefaultPlayback(): PlaybackBackend = FfmpegPlaybackBackend()

fun getDefaultStorage(): StorageBackend = DesktopStorageStub()
