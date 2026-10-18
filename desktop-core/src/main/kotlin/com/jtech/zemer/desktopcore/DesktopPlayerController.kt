package com.jtech.zemer.desktopcore

class DesktopPlayerController(
    private val queueStore: DesktopQueueStore,
    private val playback: PlaybackBackend,
    private val settings: DesktopSettingsStore
) {
    private var currentTrack: DesktopTrack? = null

    fun playTrack(track: DesktopTrack) {
        currentTrack = track
        playback.play(track.audioUrl.ifBlank { "https://example.com/sample.mp3" })
        queueStore.queue(track)
    }

    fun playFirstAvailable(): DesktopTrack? {
        val track = queueStore.next() ?: return null
        playTrack(track)
        return track
    }

    fun pause() {
        playback.pause()
    }

    fun stop() {
        playback.stop()
    }

    fun isAutoplayEnabled(): Boolean = settings.getBoolean("autoplay", true)
}
