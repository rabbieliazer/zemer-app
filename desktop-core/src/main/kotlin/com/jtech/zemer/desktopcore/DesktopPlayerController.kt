package com.jtech.zemer.desktopcore

class DesktopPlayerController(
    private val queueStore: DesktopQueueStore,
    private val playback: PlaybackBackend,
    private val settings: DesktopSettingsStore,
    private val resolver: StreamingResolver
) {
    private var currentTrack: DesktopTrack? = null

    fun playTrack(track: DesktopTrack) {
        currentTrack = track
        val url = resolvePlayableUrl(track)
        playback.play(url)
        queueStore.queue(track)
    }

    private fun resolvePlayableUrl(track: DesktopTrack): String {
        // Support a "yt:<videoId>" shorthand in the audioUrl field to mark YouTube video ids that
        // must be resolved via the streaming resolver.
        val audio = track.audioUrl
        if (audio.startsWith("yt:")) {
            val videoId = audio.removePrefix("yt:")
            val resolved = try {
                resolver.resolve(videoId)
            } catch (e: Exception) {
                println("[PlayerController] resolver threw: ${e.message}")
                null
            }
            if (!resolved.isNullOrBlank()) {
                return resolved
            }
            println("[PlayerController] resolver returned null for $videoId, falling back to placeholder URL")
            return "https://example.com/fallback/${videoId}.mp3"
        }
        return if (audio.isBlank()) "" else audio
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
