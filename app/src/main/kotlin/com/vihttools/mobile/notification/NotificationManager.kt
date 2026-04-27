package com.vihttools.mobile.notification

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.vihttools.mobile.R

object NotificationManager {

    const val OVERLAY_CHANNEL_ID = "overlay_channel"
    const val REPORT_CHANNEL_ID = "report_channel"
    const val OCR_CHANNEL_ID = "ocr_channel"
    const val READY_CHANNEL_ID = "ready_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

            // Overlay channel
            val overlayChannel = NotificationChannel(
                OVERLAY_CHANNEL_ID,
                "Плавающая кнопка",
                AndroidNotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления для плавающей кнопки"
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(overlayChannel)

            // Report channel
            val reportChannel = NotificationChannel(
                REPORT_CHANNEL_ID,
                "Новые репорты",
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о новых репортах"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(reportChannel)

            // OCR channel
            val ocrChannel = NotificationChannel(
                OCR_CHANNEL_ID,
                "OCR мониторинг",
                AndroidNotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления OCR мониторинга"
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(ocrChannel)

            // Ready channel
            val readyChannel = NotificationChannel(
                READY_CHANNEL_ID,
                "Готовность приложения",
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о готовности Viht aTools"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(readyChannel)
        }
    }

    fun buildOverlayNotification(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, OVERLAY_CHANNEL_ID)
            .setContentTitle("Viht Tools Mobile")
            .setContentText("Плавающая кнопка запущена")
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
            .setContentTitle("Новый репорт")
            .setContentText("$nickname [ID: $playerId]")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$reportText · Репортов: $reportCount")
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))
    }

    fun buildOCRNotification(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, OCR_CHANNEL_ID)
            .setContentTitle("Viht Tools Mobile")
            .setContentText("Идёт поиск игры и репортов...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    fun buildOCRReadyNotification(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, OCR_CHANNEL_ID)
            .setContentTitle("Viht aTools")
            .setContentText("Готов к работе")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    fun buildReadyNotification(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, READY_CHANNEL_ID)
            .setContentTitle("Viht aTools")
            .setContentText("Готов к работе")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 250, 150, 250))
    }
}
