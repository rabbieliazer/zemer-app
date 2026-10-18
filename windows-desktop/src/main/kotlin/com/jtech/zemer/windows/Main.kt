package com.jtech.zemer.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jtech.zemer.desktopcore.DesktopAppEnvironment
import com.jtech.zemer.desktopcore.DesktopPlayerController
import com.jtech.zemer.desktopcore.DesktopQueueStore
import com.jtech.zemer.desktopcore.DesktopTrack

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Zemer Windows"
    ) {
        val environment = remember { DesktopAppEnvironment() }
        val queueStore = remember { DesktopQueueStore() }
        val player = remember {
            DesktopPlayerController(
                queueStore = queueStore,
                playback = environment.playback,
                settings = environment.settings
            )
        }
        val initialTracks = remember { environment.database.listTracks() }
        queueStore.replace(initialTracks)
        ZemerDesktopApp(
            environment = environment,
            player = player,
            initialTracks = initialTracks,
            queueStore = queueStore
        )
    }
}

@Composable
private fun ZemerDesktopApp(
    environment: DesktopAppEnvironment,
    player: DesktopPlayerController,
    initialTracks: List<DesktopTrack>,
    queueStore: DesktopQueueStore
) {
    val colorScheme = darkColorScheme(
        primary = Color(0xFFFB4F69),
        secondary = Color(0xFFF29AB3),
        background = Color(0xFF111318),
        surface = Color(0xFF1C2027),
    )

    MaterialTheme(colorScheme = colorScheme) {
        var selectedTab by remember { mutableStateOf("Home") }
        var searchText by remember { mutableStateOf("") }
        var showSettings by remember { mutableStateOf(false) }
        var trackList by remember { mutableStateOf(initialTracks) }
        var playing by remember { mutableStateOf(false) }
        val settings = remember { environment.settings }
        var autoplay by remember { mutableStateOf(settings.getBoolean("autoplay", true)) }

        val filteredTracks = remember(searchText, trackList) {
            if (searchText.isBlank()) trackList else trackList.filter {
                it.title.contains(searchText, ignoreCase = true)
                    || it.artist.contains(searchText, ignoreCase = true)
            }
        }

        val queue = remember { queueStore.currentQueue() }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == "Home",
                        onClick = { selectedTab = "Home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == "Search",
                        onClick = { selectedTab = "Search" },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text("Search") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == "Library",
                        onClick = { selectedTab = "Library" },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                        label = { Text("Library") }
                    )
                    NavigationBarItem(
                        selected = showSettings,
                        onClick = { showSettings = !showSettings },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings") }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Zemer", style = MaterialTheme.typography.headlineMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val item = filteredTracks.firstOrNull()
                        if (item != null) {
                            player.playTrack(item)
                            playing = true
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Text(if (playing) "Now playing" else "Play")
                        }
                    }
                    Button(onClick = {
                        player.pause()
                        playing = false
                    }) {
                        Text("Pause")
                    }
                    Button(onClick = {
                        player.stop()
                        playing = false
                    }) {
                        Text("Stop")
                    }
                }

                if (showSettings) {
                    DesktopSettingsCard(
                        autoplay = autoplay,
                        onAutoplayChanged = {
                            autoplay = it
                            settings.setBoolean("autoplay", it)
                        }
                    )
                }

                TabRow(selectedTabIndex = when (selectedTab) {
                    "Home" -> 0
                    "Search" -> 1
                    else -> 2
                }) {
                    Tab(selected = selectedTab == "Home", onClick = { selectedTab = "Home" }) { Text("Home") }
                    Tab(selected = selectedTab == "Search", onClick = { selectedTab = "Search" }) { Text("Search") }
                    Tab(selected = selectedTab == "Library", onClick = { selectedTab = "Library" }) { Text("Library") }
                }

                if (selectedTab == "Search") {
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search your music…") }
                    )
                }

                when (selectedTab) {
                    "Home" -> HomeContent(filteredTracks)
                    "Search" -> SearchContent(filteredTracks)
                    "Library" -> LibraryContent(queue)
                }
            }
        }
    }
}

@Composable
private fun DesktopSettingsCard(
    autoplay: Boolean,
    onAutoplayChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Autoplay")
                Button(onClick = { onAutoplayChanged(!autoplay) }) {
                    Text(if (autoplay) "On" else "Off")
                }
            }
            Text("Desktop runtime: local SQLite library + ffmpeg playback", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun HomeContent(tracks: List<DesktopTrack>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SpotlightCard() }
        items(tracks) { track -> MusicRow(track) }
    }
}

@Composable
private fun SearchContent(tracks: List<DesktopTrack>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(tracks) { track -> MusicRow(track) }
    }
}

@Composable
private fun LibraryContent(queue: List<DesktopTrack>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(queue) { track ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(track.title, style = MaterialTheme.typography.titleMedium)
                    Text(track.artist, style = MaterialTheme.typography.bodyMedium)
                    Text(track.mood, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun SpotlightCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Featured for you", style = MaterialTheme.typography.labelLarge)
            Text("New Jewish & international favorites", style = MaterialTheme.typography.headlineSmall)
            Text("Curated tracks for your listening mood.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MusicRow(track: DesktopTrack) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(52.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.titleMedium)
                Text(track.artist, style = MaterialTheme.typography.bodyMedium)
            }
            Text(track.mood, style = MaterialTheme.typography.labelMedium)
        }
    }
}
