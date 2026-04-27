package com.vihttools.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,           // e.g., "Warn", "Kick", "Ban"
    val command: String,         // e.g., "/warn {ID} Spam", "/kick {ID}"
    val order: Int = 0,          // For sorting
    val isActive: Boolean = true
) {
    /**
     * Replace {ID} placeholder with actual player ID
     */
    fun formatCommand(playerId: Int): String {
        return command.replace("{ID}", playerId.toString())
    }
}
