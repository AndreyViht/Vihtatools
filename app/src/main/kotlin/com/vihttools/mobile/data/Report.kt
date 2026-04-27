package com.vihttools.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nickname: String,
    val playerId: Int,
    val text: String,
    val reportCount: Int,
    val timestamp: Long,
    val isAnswered: Boolean = false,
    val answeredAt: Long? = null
)

data class ReportTemplate(
    val id: Int = 0,
    val label: String,  // Max 5 characters
    val command: String,  // Contains {ID} placeholder
    val order: Int = 0
)

data class AppSettings(
    val buttonPositionX: Int = 0,
    val buttonPositionY: Int = 100,
    val buttonTransparency: Float = 0.5f,  // 0.2 to 0.8
    val ocrScanInterval: Long = 1500L,  // ms: 500, 1000, 1500, 2000
    val isDarkTheme: Boolean = true
)
