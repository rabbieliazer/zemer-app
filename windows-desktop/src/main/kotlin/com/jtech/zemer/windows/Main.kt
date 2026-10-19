@@
                 Text(text = "Zemer", style = MaterialTheme.typography.headlineMedium)
-
+                // ffmpeg check: if ffmpeg/ffplay is not available, show a hint banner with download link
+                if (!com.jtech.zemer.desktopcore.DesktopUtil.isFfmpegAvailable()) {
+                    Card(
+                        modifier = Modifier.fillMaxWidth(),
+                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f))
+                    ) {
+                        Column(modifier = Modifier.padding(12.dp)) {
+                            Text("ffmpeg/ffplay not found on PATH", style = MaterialTheme.typography.labelLarge)
+                            Text("Install ffmpeg and ensure ffplay or ffmpeg is on your PATH. See https://ffmpeg.org/download.html", style = MaterialTheme.typography.bodySmall)
+                        }
+                    }
+                }
+
