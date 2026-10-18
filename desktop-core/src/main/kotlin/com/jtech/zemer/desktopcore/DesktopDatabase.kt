package com.jtech.zemer.desktopcore

import java.io.File
import java.sql.DriverManager

class DesktopDatabase(private val dbFile: File) {
    init {
        dbFile.parentFile?.mkdirs()
        Class.forName("org.sqlite.JDBC")
        initialize()
    }

    private fun connection() = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")

    private fun initialize() {
        connection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS track (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "artist TEXT NOT NULL, " +
                        "mood TEXT NOT NULL, " +
                        "audioUrl TEXT NOT NULL DEFAULT ''" +
                        ")"
                )
            }
        }
        if (listTracks().isEmpty()) {
            insertSeedTracks()
        }
    }

    private fun insertSeedTracks() {
        val seed = listOf(
            DesktopTrack(0L, "Morning Glow", "Noa Levi", "Chill", "https://example.com/morning-glow.mp3"),
            DesktopTrack(0L, "Breathe Again", "David Kedar", "Feel-good", "https://example.com/breathe-again.mp3"),
            DesktopTrack(0L, "Sunset in the City", "Ari Ben", "Nostalgic", "https://example.com/sunset-city.mp3"),
            DesktopTrack(0L, "Open Roads", "Mira Shah", "Focus", "https://example.com/open-roads.mp3"),
            DesktopTrack(0L, "Homecoming", "Ilan Morel", "Calm", "https://example.com/homecoming.mp3"),
            DesktopTrack(0L, "Late Night Drive", "Rina Sol", "Energetic", "https://example.com/late-night-drive.mp3")
        )

        connection().use { conn ->
            val sql = "INSERT INTO track(title, artist, mood, audioUrl) VALUES (?, ?, ?, ?)"
            conn.prepareStatement(sql).use { ps ->
                seed.forEach { track ->
                    ps.setString(1, track.title)
                    ps.setString(2, track.artist)
                    ps.setString(3, track.mood)
                    ps.setString(4, track.audioUrl)
                    ps.addBatch()
                }
                ps.executeBatch()
            }
        }
    }

    fun listTracks(): List<DesktopTrack> {
        val out = ArrayList<DesktopTrack>()
        connection().use { conn ->
            conn.prepareStatement("SELECT id, title, artist, mood, audioUrl FROM track ORDER BY title").use { ps ->
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        out += DesktopTrack(
                            id = rs.getLong("id"),
                            title = rs.getString("title"),
                            artist = rs.getString("artist"),
                            mood = rs.getString("mood"),
                            audioUrl = rs.getString("audioUrl")
                        )
                    }
                }
            }
        }
        return out
    }

    fun searchTracks(query: String): List<DesktopTrack> {
        val q = query.trim()
        if (q.isEmpty()) return listTracks()
        return listTracks().filter {
            it.title.contains(q, ignoreCase = true)
                || it.artist.contains(q, ignoreCase = true)
                || it.mood.contains(q, ignoreCase = true)
        }
    }
}

data class DesktopTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val mood: String,
    val audioUrl: String = ""
)
