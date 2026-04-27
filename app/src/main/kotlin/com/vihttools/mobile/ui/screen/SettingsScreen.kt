package com.vihttools.mobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var selectedPosition by remember { mutableStateOf("TOP_RIGHT") }
    var transparency by remember { mutableStateOf(0.5f) }
    var scanInterval by remember { mutableStateOf(1500L) }
    var isDarkTheme by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2D2D))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.clickable { onBack() }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Button Position
            SettingSection(title = "Button Position") {
                val positions = listOf("TOP_RIGHT", "TOP_LEFT", "BOTTOM_RIGHT", "BOTTOM_LEFT")
                positions.forEach { position ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPosition == position,
                            onClick = { selectedPosition = position }
                        )
                        Text(
                            text = position,
                            color = Color(0xFFB0B0B0),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transparency
            SettingSection(title = "Button Transparency") {
                Column(modifier = Modifier.padding(8.dp)) {
                    Slider(
                        value = transparency,
                        onValueChange = { transparency = it },
                        valueRange = 0.2f..0.8f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Opacity: ${(transparency * 100).toInt()}%",
                        color = Color(0xFFB0B0B0),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // OCR Scan Interval
            SettingSection(title = "OCR Scan Interval") {
                val intervals = listOf(
                    500L to "Fast (0.5s)",
                    1000L to "Normal (1s)",
                    1500L to "Balanced (1.5s)",
                    2000L to "Slow (2s)"
                )
                intervals.forEach { (interval, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = scanInterval == interval,
                            onClick = { scanInterval = interval }
                        )
                        Text(
                            text = label,
                            color = Color(0xFFB0B0B0),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Theme
            SettingSection(title = "Theme") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Dark Theme",
                        color = Color(0xFFB0B0B0)
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE63946)
                )
            ) {
                Text(
                    text = "Save Settings",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SettingSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun Clickable(modifier: Modifier = Modifier, onClick: () -> Unit) {
    // Placeholder for clickable modifier
}
