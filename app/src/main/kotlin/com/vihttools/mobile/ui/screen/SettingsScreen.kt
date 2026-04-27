package com.vihttools.mobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vihttools.mobile.settings.SettingsManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedPosition by SettingsManager.getLastKnownPosition(context).collectAsState(initial = "TOP_RIGHT")
    val savedTransparency by SettingsManager.getButtonTransparency(context).collectAsState(initial = 0.5f)
    val savedScanInterval by SettingsManager.getOCRScanInterval(context).collectAsState(initial = 1500L)
    val savedIsDarkTheme by SettingsManager.getIsDarkTheme(context).collectAsState(initial = true)

    var selectedPosition by remember(savedPosition) { mutableStateOf(savedPosition) }
    var transparency by remember(savedTransparency) { mutableStateOf(savedTransparency) }
    var scanInterval by remember(savedScanInterval) { mutableStateOf(savedScanInterval) }
    var isDarkTheme by remember(savedIsDarkTheme) { mutableStateOf(savedIsDarkTheme) }

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
                text = "← Настройки",
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
            SettingSection(title = "Положение кнопки") {
                val positions = SettingsManager.ButtonPosition.entries.associate { position ->
                    position.name to when (position) {
                        SettingsManager.ButtonPosition.TOP_RIGHT -> "Сверху справа"
                        SettingsManager.ButtonPosition.TOP_LEFT -> "Сверху слева"
                        SettingsManager.ButtonPosition.BOTTOM_RIGHT -> "Снизу справа"
                        SettingsManager.ButtonPosition.BOTTOM_LEFT -> "Снизу слева"
                    }
                }
                positions.forEach { (position, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPosition = position }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPosition == position,
                            onClick = { selectedPosition = position }
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

            SettingSection(title = "Прозрачность кнопки") {
                Column(modifier = Modifier.padding(8.dp)) {
                    Slider(
                        value = transparency,
                        onValueChange = { transparency = it },
                        valueRange = 0.2f..0.8f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Прозрачность: ${(transparency * 100).toInt()}%",
                        color = Color(0xFFB0B0B0),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingSection(title = "Интервал OCR-сканирования") {
                val intervals = listOf(
                    SettingsManager.OCRInterval.FAST.ms to "Быстро (0.5 сек)",
                    SettingsManager.OCRInterval.NORMAL.ms to "Обычно (1 сек)",
                    SettingsManager.OCRInterval.BALANCED.ms to "Баланс (1.5 сек)",
                    SettingsManager.OCRInterval.SLOW.ms to "Медленно (2 сек)"
                )
                intervals.forEach { (interval, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { scanInterval = interval }
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

            SettingSection(title = "Тема") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Тёмная тема",
                        color = Color(0xFFB0B0B0)
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        val position = SettingsManager.ButtonPosition.valueOf(selectedPosition)
                        SettingsManager.setButtonPosition(context, position.x, position.y)
                        SettingsManager.setLastKnownPosition(context, selectedPosition)
                        SettingsManager.setButtonTransparency(context, transparency)
                        SettingsManager.setOCRScanInterval(context, scanInterval)
                        SettingsManager.setIsDarkTheme(context, isDarkTheme)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE63946)
                )
            ) {
                Text(
                    text = "Сохранить настройки",
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
