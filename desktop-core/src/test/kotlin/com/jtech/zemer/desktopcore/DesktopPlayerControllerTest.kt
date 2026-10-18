package com.jtech.zemer.desktopcore

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

private class FakePlayback : PlaybackBackend {
    var playedUrl: String? = null
    var paused = false
    var stopped = false
    override val isPlaying: Boolean
        get() = playedUrl != null && !stopped

    override fun play(url: String) {
        playedUrl = url
        paused = false
        stopped = false
    }

    override fun pause() {
        paused = true
    }

    override fun stop() {
        stopped = true
        playedUrl = null
    }
}

class DesktopPlayerControllerTest {
    @Test
    fun `playTrack enqueues and calls playback`() {
        val store = DesktopQueueStore()
        val fake = FakePlayback()
        val settings = DesktopSettingsStore(java.io.File.createTempFile("zemer-test", "props"))
        val controller = DesktopPlayerController(store, fake, settings)

        val t = DesktopTrack(42, "Song", "Artist", "Mood", "https://example.com/s.mp3")
        controller.playTrack(t)

        assertEquals(t.id, store.currentQueue().first().id)
        assertEquals("https://example.com/s.mp3", fake.playedUrl)
    }

    @Test
    fun `pause and stop forward to playback`() {
        val store = DesktopQueueStore()
        val fake = FakePlayback()
        val settings = DesktopSettingsStore(java.io.File.createTempFile("zemer-test", "props"))
        val controller = DesktopPlayerController(store, fake, settings)

        val t = DesktopTrack(43, "Song2", "Artist2", "Mood", "https://example.com/s2.mp3")
        controller.playTrack(t)
        controller.pause()
        assertTrue(fake.paused)
        controller.stop()
        assertTrue(fake.stopped)
    }
}
