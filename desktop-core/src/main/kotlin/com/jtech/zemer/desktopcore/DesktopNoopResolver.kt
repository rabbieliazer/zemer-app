package com.jtech.zemer.desktopcore

import java.io.File

/**
 * A no-op resolver that looks for an innertube_cookie.txt file and otherwise returns null.
 * This is a safe default used until a full resolver that depends on :innertube + cipher is
 * implemented. It logs helpful instructions for developers who want to test real streams.
 */
class DesktopNoopResolver(private val repoRoot: File = File(".")) : StreamingResolver {
    override fun resolve(videoId: String): String? {
        val cookie = File(repoRoot, "innertube_cookie.txt")
        if (!cookie.exists()) {
            println("[StreamingResolver] innertube_cookie.txt not found at ${cookie.absolutePath}.")
            println("[StreamingResolver] To enable live stream resolution, place your innertube_cookie.txt at the repo root (gitignored).")
            return null
        }
        // We intentionally do not attempt live resolution here. A full resolver will use
        // the innertube module + cipher submodule to mint poTokens and decipher signatures.
        println("[StreamingResolver] innertube_cookie.txt found but no desktop resolver implemented yet.")
        println("[StreamingResolver] Implement a resolver that reuses :innertube and the cipher library to resolve videoId=$videoId")
        return null
    }
}
