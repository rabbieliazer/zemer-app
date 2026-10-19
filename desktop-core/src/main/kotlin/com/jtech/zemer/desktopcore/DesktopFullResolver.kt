package com.jtech.zemer.desktopcore

import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * DesktopFullResolver attempts to achieve full parity with the Android streaming resolver by:
 * - fetching the watch page and extracting the player response
 * - when signatureCipher/cipher is present, attempting to use the repository's cipher/poToken
 *   classes via reflection (if they are available on the classpath) to decipher the signature
 *   and mint a pot= token. This reflection-based approach avoids a hard compile-time dependency
 *   on the cipher/innertube modules while enabling local runs when the submodules are present.
 *
 * Note: This is best-effort. If reflection hooks are not available or fail, the resolver falls back
 * to the pragmatic HTML parser (DesktopStreamingResolverImpl) behavior so non-ciphered videos still
 * resolve.
 */
class DesktopFullResolver(private val repoRoot: File = File(".")) : StreamingResolver {
    private val fallback = DesktopStreamingResolverImpl(repoRoot)

    override fun resolve(videoId: String): String? {
        try {
            val watchUrl = "https://www.youtube.com/watch?v=$videoId"
            val cookieFile = File(repoRoot, "innertube_cookie.txt")
            val cookieHeader = if (cookieFile.exists()) cookieFile.readText().trim().takeIf { it.isNotEmpty() } else null

            val html = httpGet(watchUrl, cookieHeader)

            // Try to find ytInitialPlayerResponse and player args like the pragmatic resolver did
            val playerResponseRegex = Regex("ytInitialPlayerResponse\\s*=\\s*(\\{.*?\\})\\s*;", RegexOption.DOT_MATCHES_ALL)
            val m = playerResponseRegex.find(html)
            val json = m?.groupValues?.get(1)
            if (json == null) {
                // Fallback to pragmatic resolver
                return fallback.resolve(videoId)
            }

            // If no signatureCipher/cipher present, defer to fallback which handles direct-URL extraction
            if (!json.contains("signatureCipher") && !json.contains("cipher")) {
                return fallback.resolve(videoId)
            }

            println("[FullResolver] Detected signatureCipher/cipher in player response; attempting cipher flow")

            // Find one signatureCipher entry and parse it
            // signatureCipher examples appear as: "signatureCipher":"s=...&url=...&sp=signature"
            val scRegex = Regex("\"signatureCipher\"\\s*:\\s*\"(.*?)\"", RegexOption.DOT_MATCHES_ALL)
            val scMatch = scRegex.find(json)
            if (scMatch == null) {
                println("[FullResolver] no signatureCipher match found despite marker; falling back")
                return fallback.resolve(videoId)
            }
            val scEncoded = scMatch.groupValues[1]
            val sc = unescapeJsonString(scEncoded)
            val params = parseQueryString(sc)
            val url = params["url"] ?: run {
                println("[FullResolver] signatureCipher missing url param; falling back")
                return fallback.resolve(videoId)
            }
            val s = params["s"] ?: params["sig"] ?: params["signature"]
            val sp = params["sp"] ?: "signature"

            var finalUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.name())

            if (s != null) {
                // Attempt to decipher using available cipher classes via reflection
                val deciphered = tryDecipherSignature(s, finalUrl)
                if (deciphered != null) {
                    // append the deciphered signature as the sp param
                    if (finalUrl.contains("?")) finalUrl += "&$sp=$deciphered" else finalUrl += "?$sp=$deciphered"
                } else {
                    println("[FullResolver] could not decipher signature via reflection; falling back to fallback resolver")
                    return fallback.resolve(videoId)
                }
            }

            // Attempt to mint a poToken (pot) if PoToken generator available
            val pot = tryMintPoToken(videoId, finalUrl)
            if (!pot.isNullOrBlank()) {
                if (finalUrl.contains("?")) finalUrl += "&pot=$pot" else finalUrl += "?pot=$pot"
            }

            println("[FullResolver] resolved url with decipher/pot: $finalUrl")
            return finalUrl
        } catch (e: Exception) {
            println("[FullResolver] unexpected error: ${e.message}")
            e.printStackTrace()
            return fallback.resolve(videoId)
        }
    }

    private fun tryDecipherSignature(s: String, playerUrl: String): String? {
        val candidateClasses = listOf(
            "com.zemer.cipher.CipherUtils",
            "com.zemer.cipher.PoTokenGenerator",
            "com.zemer.cipher.Cipher",
            "com.zemer.cipher.Decipher",
            "com.zemer.cipher.CipherFn"
        )
        for (clsName in candidateClasses) {
            try {
                val cls = Class.forName(clsName)
                // try several method names commonly used
                val methodNames = listOf("decipher", "decipherSignature", "applyCipher", "runCipher", "decrypt")
                for (mName in methodNames) {
                    try {
                        val m = cls.methods.firstOrNull { it.name == mName && it.parameterCount >= 1 }
                        if (m != null) {
                            // call static or instance
                            val instance = if (!java.lang.reflect.Modifier.isStatic(m.modifiers)) cls.getDeclaredConstructor().newInstance() else null
                            val result = if (instance != null) m.invoke(instance, s, playerUrl) else m.invoke(null, s, playerUrl)
                            if (result is String && result.isNotBlank()) return result
                        }
                    } catch (_: Throwable) {
                        // ignore and try next
                    }
                }
            } catch (_: ClassNotFoundException) {
                // not present; try next candidate
            } catch (e: Exception) {
                println("[FullResolver] reflection error on $clsName: ${e.message}")
            }
        }
        return null
    }

    private fun tryMintPoToken(videoId: String, playerUrl: String): String? {
        val candidate = "com.zemer.cipher.PoTokenGenerator"
        try {
            val cls = Class.forName(candidate)
            // try static mint(videoId, playerUrl) or instance method mintPoToken(videoId)
            val static = cls.methods.firstOrNull { it.name.equals("mintPoToken", ignoreCase = true) && it.parameterCount >= 1 }
            if (static != null) {
                val args = when (static.parameterCount) {
                    1 -> arrayOf(videoId)
                    2 -> arrayOf(videoId, playerUrl)
                    else -> arrayOf(videoId)
                }
                val inst = if (!java.lang.reflect.Modifier.isStatic(static.modifiers)) cls.getDeclaredConstructor().newInstance() else null
                val res = if (inst != null) static.invoke(inst, *args) else static.invoke(null, *args)
                if (res is String && res.isNotBlank()) return res
            }
        } catch (e: ClassNotFoundException) {
            // not present
        } catch (e: Exception) {
            println("[FullResolver] error trying to mint PoToken via reflection: ${e.message}")
        }
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

    private fun parseQueryString(q: String): Map<String, String> {
        return q.split('&').mapNotNull {
            val i = it.indexOf('=')
            if (i <= 0) return@mapNotNull null
            val k = URLDecoder.decode(it.substring(0, i), StandardCharsets.UTF_8.name())
            val v = URLDecoder.decode(it.substring(i + 1), StandardCharsets.UTF_8.name())
            k to v
        }.toMap()
    }

    private fun unescapeJsonString(s: String): String {
        var r = s.replace("\\\\/", "/")
        r = r.replace("\\u0026", "&")
        r = r.replace("\\u003d", "=")
        r = r.replace("\\u00253A", ":")
        r = r.replace("\\\"", "\"")
        return r
    }
}
