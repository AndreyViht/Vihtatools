package com.vihttools.mobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    isOverlayRunning: Boolean,
    activeReports: Int,
    respondedReports: Int,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var statusMessage by remember(isOverlayRunning) {
        mutableStateOf(
            when {
                isOverlayRunning -> "Overlay is running..."
                else -> "Waiting for game..."
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Viht Tools Mobile",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = if (isOverlayRunning) Color(0xFF2A9D8F) else Color(0xFF606060),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFB0B0B0)
                    )
                }

                Divider(
                    color = Color(0xFF404040),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Active",
                        value = activeReports.toString()
                    )
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = Color(0xFF404040)
                    )
                    StatItem(
                        label = "Responded",
                        value = respondedReports.toString()
                    )
                }
            }
        }

        // Control Buttons
        if (!isOverlayRunning) {
            Button(
                onClick = onStartOverlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE63946)
                )
            ) {
                Text(
                    text = "Start Overlay",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        } else {
            Button(
                onClick = onStopOverlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF606060)
                )
            ) {
                Text(
                    text = "Stop Overlay",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Settings Button
        OutlinedButton(
            onClick = onNavigateToSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF457B9D)
            )
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How it works",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "1. Tap \"Start Overlay\" to begin monitoring\n2. The floating button will appear on your screen\n3. New reports will be highlighted\n4. Tap the button to reply quickly",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB0B0B0),
                    lineHeight = 18.dp
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFE63946)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFB0B0B0)
        )
    }
}
