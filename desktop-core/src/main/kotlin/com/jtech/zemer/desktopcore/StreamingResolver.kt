package com.jtech.zemer.desktopcore

/**
 * Resolves a YouTube video id (or other catalog id) into a playable URL for the desktop player.
 * Implementations may require credentials (innertube_cookie.txt) or access to the cipher submodule.
 */
interface StreamingResolver {
    /**
     * Resolve a video id like "JTF9fLJvniI" into a playable URL (googlevideo link) or null if
     * resolution is not possible in the current environment.
     */
    fun resolve(videoId: String): String?
}
