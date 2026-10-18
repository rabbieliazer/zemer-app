package com.jtech.zemer.desktopcore

import java.io.File

/**
 * Desktop-specific runtime contract for a playable URL fetcher.
 *
 * This is a small domain boundary used by the desktop app to resolve a track to a stream URL.
 * It is intentionally lightweight and does not absorb the full Android streaming pipeline; that
 * remains in the Android repo and the cipher submodule until a proper desktop port is implemented.
 */
interface PlayableUrlProvider {
    fun provide(videoId: String): String?
}

class FileBackedPlayableUrlProvider(private val root: File = File(".")) : PlayableUrlProvider {
    override fun provide(videoId: String): String? {
        val cookieFile = File(root, "innertube_cookie.txt")
        if (!cookieFile.exists()) {
            println("[PlayableUrlProvider] missing innertube_cookie.txt; live resolution unavailable.")
            return null
        }
        println("[PlayableUrlProvider] cookie present for videoId=$videoId; full resolver is not implemented yet.")
        return null
    }
}
