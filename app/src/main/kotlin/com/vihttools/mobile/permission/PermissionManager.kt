package com.vihttools.mobile.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionManager {

    /**
     * Check if the app has permission to display overlay
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Check if the app has notification permission (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasAllPermissions(context: Context): Boolean {
        return hasOverlayPermission(context) && hasNotificationPermission(context)
    }

    /**
     * Get the intent to request overlay permission
     */
    fun getOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        )
    }

    /**
     * Get list of missing permissions
     */
    fun getMissingPermissions(context: Context): List<String> {
        val missing = mutableListOf<String>()

        if (!hasOverlayPermission(context)) {
            missing.add("OVERLAY")
        }

        if (!hasNotificationPermission(context)) {
            missing.add("NOTIFICATION")
        }

        return missing
    }

    /**
     * Get permission description for UI display
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            "OVERLAY" -> "Показывать поверх других приложений"
            "NOTIFICATION" -> "Отправлять уведомления"
            else -> "Неизвестное разрешение"
        }
    }
}
