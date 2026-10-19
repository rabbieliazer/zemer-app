package com.jtech.zemer.desktopcore

import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * A pragmatic desktop streaming resolver that attempts to extract a playable googlevideo URL
 * from YouTube's player HTML. This is an incremental, best-effort approach that will work
 * for some videos which embed direct URLs in the player response. Videos that require
 * signatureCipher/deciphering will return null — in that case the full cipher port is required.
 *
 * Notes:
 * - This implementation is intentionally self-contained and does not depend on :innertube
 *   or the cipher submodule so it can be built and tested without extra Gradle wiring.
 * - For full parity with Android, the cipher/poToken flow must be implemented. This class
 *   provides a bridge so developers can test live resolution for non-ciphered videos.
 */
class DesktopStreamingResolverImpl(private val repoRoot: File = File(".")) : StreamingResolver {
    override fun resolve(videoId: String): String? {
        val cookieFile = File(repoRoot, "innertube_cookie.txt")
        val cookieHeader = if (cookieFile.exists()) {
            try {
                cookieFile.readText().trim().takeIf { it.isNotEmpty() }
            } catch (e: Exception) {
                println("[Resolver] Failed to read cookie file: ${e.message}")
                null
            }
        } else null

        val watchUrl = "https://www.youtube.com/watch?v=$videoId"
        println("[Resolver] fetching $watchUrl")

        val html = try {
            httpGet(watchUrl, cookieHeader)
        } catch (e: Exception) {
            println("[Resolver] http fetch failed: ${e.message}")
            return null
        }

        // Try to extract ytInitialPlayerResponse JSON blob
        val playerResponseRegex = Regex("ytInitialPlayerResponse\\s*=\\s*(\\{.*?\\})\\s*;", RegexOption.DOT_MATCHES_ALL)
        val m = playerResponseRegex.find(html)
        if (m != null) {
            val json = m.groupValues[1]
            // Look for direct url entries in the player response
            val urlRegex = Regex("\"url\"\s*:\s*\"(https:[^\\\\\"]+)\"")
            val um = urlRegex.find(json)
            if (um != null) {
                val raw = um.groupValues[1]
                val unescaped = unescapeJsonString(raw)
                println("[Resolver] found direct url in player response")
                return unescaped
            }

            // If signatureCipher or cipher is present we can't handle it here
            if (json.contains("signatureCipher") || json.contains("cipher")) {
                println("[Resolver] player response uses signatureCipher/cipher; full cipher port required")
                return null
            }
        }

        // Fallback: try to parse the older get_video_info map from HTML
        val fmtRegex = Regex("url_encoded_fmt_stream_map\"\s*:\s*\"([^"]+)")
        val fm = fmtRegex.find(html)
        if (fm != null) {
            val encoded = fm.groupValues[1]
            // url-encoded map contains url=... entries; try to extract the first https URL
            val urlMatch = Regex("https%3A%2F%2F[^&\\\\]+|https://[^,&\\\\]+).toRegex()")
            // The above regex is conservative; instead, unescape the string and try a simpler find
            val decoded = decodeBasicJsonEscapes(encoded)
            val u2 = Regex("https://[^\\\\\\\"]+").find(decoded)
            if (u2 != null) {
                println("[Resolver] found url in url_encoded_fmt_stream_map")
                return u2.value
            }
        }

        println("[Resolver] no playable url found in HTML; full innertube/cipher flow required")
        return null
    }

    private fun httpGet(url: String, cookieHeader: String?): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0 Safari/537.36")
        if (!cookieHeader.isNullOrBlank()) {
            conn.setRequestProperty("Cookie", cookieHeader)
        }
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream ?: conn.inputStream
        return InputStreamReader(stream, conn.contentEncoding ?: "UTF-8").readText()
    }

    private fun unescapeJsonString(s: String): String {
        // Very small unescape utility for common escapes we see in ytInitialPlayerResponse
        var r = s.replace("\\\\/", "/")
        r = r.replace("\\u0026", "&")
        r = r.replace("\\u003d", "=")
        r = r.replace("\\u00253A", ":")
        r = r.replace("\\\"", "\"")
        return r
    }

    private fun decodeBasicJsonEscapes(s: String): String {
        return s.replace("\\\\/", "/").replace("\\\"", "\"").replace("\\u0026", "&")
    }
}
