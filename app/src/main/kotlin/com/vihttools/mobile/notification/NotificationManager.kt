package com.vihttools.mobile.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.vihttools.mobile.R

object NotificationManager {

    const val OVERLAY_CHANNEL_ID = "overlay_channel"
    const val REPORT_CHANNEL_ID = "report_channel"
    const val OCR_CHANNEL_ID = "ocr_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Overlay channel
            val overlayChannel = NotificationChannel(
                OVERLAY_CHANNEL_ID,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for overlay service"
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(overlayChannel)

            // Report channel
            val reportChannel = NotificationChannel(
                REPORT_CHANNEL_ID,
                "New Reports",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new reports"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(reportChannel)

            // OCR channel
            val ocrChannel = NotificationChannel(
                OCR_CHANNEL_ID,
                "OCR Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for OCR monitoring"
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(ocrChannel)
        }
    }

    fun buildOverlayNotification(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, OVERLAY_CHANNEL_ID)
            .setContentTitle("Viht Tools Mobile")
            .setContentText("Overlay is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    fun buildReportNotification(
        context: Context,
        nickname: String,
        playerId: Int,
        reportText: String,
        reportCount: Int
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, REPORT_CHANNEL_ID)
            .setContentTitle("🔴 New Report")
            .setContentText("$nickname [ID: $playerId]")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$reportText · Reports: $reportCount")
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))
    }

    fun buildOCRNotification(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, OCR_CHANNEL_ID)
            .setContentTitle("Viht Tools Mobile")
            .setContentText("Monitoring game chat...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }
}
