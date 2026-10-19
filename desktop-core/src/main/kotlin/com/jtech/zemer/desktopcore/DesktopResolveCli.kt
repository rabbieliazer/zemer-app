package com.jtech.zemer.desktopcore

/**
 * Small CLI helper to test the desktop resolver locally.
 * Usage: run this main class with a single argument: a YouTube video id (e.g., JTF9fLJvniI)
 * It will attempt to resolve the video id to a playable URL and print the result.
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: java -cp <app-jar> com.jtech.zemer.desktopcore.DesktopResolveCli <videoId>")
        return
    }
    val videoId = args[0]
    val resolver: StreamingResolver = DesktopStreamingResolverImpl()
    val url = resolver.resolve(videoId)
    if (url != null) {
        println("Resolved URL: $url")
    } else {
        println("Failed to resolve a playable URL for $videoId. See logs for details.")
    }
}
