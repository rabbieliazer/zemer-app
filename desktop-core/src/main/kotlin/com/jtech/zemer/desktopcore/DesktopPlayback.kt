package com.jtech.zemer.desktopcore

class DesktopPlaybackStub : PlaybackBackend {
    private var playing = false

    override val isPlaying: Boolean
        get() = playing

    override fun play(url: String) {
        // Simple stub — replace with real playback (ffmpeg/libVLC) later
        println("[DesktopPlayback] play: $url")
        playing = true
    }

    override fun pause() {
        println("[DesktopPlayback] pause")
        playing = false
    }

    override fun stop() {
        println("[DesktopPlayback] stop")
        playing = false
    }
}
