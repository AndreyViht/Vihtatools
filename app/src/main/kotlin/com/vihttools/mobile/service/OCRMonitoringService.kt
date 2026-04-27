package com.vihttools.mobile.service

import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.vihttools.mobile.data.AppDatabase
import com.vihttools.mobile.data.Report
import com.vihttools.mobile.data.ReportCircularBuffer
import com.vihttools.mobile.notification.NotificationManager
import com.vihttools.mobile.ocr.OCRTextDetector
import com.vihttools.mobile.ocr.ScreenCaptureManager
import com.vihttools.mobile.settings.SettingsManager
import kotlinx.coroutines.*

class OCRMonitoringService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var screenCaptureManager: ScreenCaptureManager? = null
    private val ocrDetector = OCRTextDetector()
    private val reportBuffer = ReportCircularBuffer(maxSize = 10)
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val handler = Handler(Looper.getMainLooper())

    private var isMonitoring = false
    private var gameDetected = false
    private var scanIntervalMs = 1500L
    private var settingsJob: Job? = null
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        NotificationManager.createNotificationChannels(this)
        createNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mediaProjectionData = intent?.getParcelableExtra<Intent>("media_projection_data")
        if (mediaProjectionData != null) {
            val mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
            mediaProjection = mediaProjectionManager.getMediaProjection(RESULT_OK, mediaProjectionData)
            screenCaptureManager = ScreenCaptureManager(this, mediaProjection!!)
            startMonitoring()
        }

        if (settingsJob == null) {
            settingsJob = scope.launch {
                SettingsManager.getOCRScanInterval(this@OCRMonitoringService).collect { interval ->
                    scanIntervalMs = interval
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        screenCaptureManager?.release()
        mediaProjection?.stop()
        scope.cancel()
    }

    private fun createNotification() {
        val notification = NotificationManager.buildOCRNotification(this).build()
        startForeground(2, notification)
    }

    private fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        performOCRScan()
        scheduleNextScan()
    }

    private fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun scheduleNextScan() {
        if (!isMonitoring) return
        handler.postDelayed({
            performOCRScan()
            scheduleNextScan()
        }, scanIntervalMs)
    }

    private fun performOCRScan() {
        scope.launch {
            try {
                val bitmap = screenCaptureManager?.captureChatArea() ?: return@launch
                val recognizedText = ocrDetector.recognizeText(bitmap)

                val reports = ocrDetector.extractReports(recognizedText)

                if (!gameDetected && (ocrDetector.isGameDetected(recognizedText, bitmap) || reports.isNotEmpty())) {
                    gameDetected = true
                    showReadyNotification()
                }

                if (gameDetected) {
                    processReports(reports)
                }

                bitmap.recycle()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun processReports(reportDataList: List<OCRTextDetector.ReportData>) {
        for (reportData in reportDataList) {
            // Skip admin messages
            if (ocrDetector.isAdminMessage(reportData.text)) {
                continue
            }

            // Check if report is new
            if (reportBuffer.exists(reportData.nickname, reportData.playerId)) {
                continue
            }

            // Create and add report
            val report = Report(
                nickname = reportData.nickname,
                playerId = reportData.playerId,
                text = reportData.text,
                reportCount = reportData.reportCount,
                timestamp = System.currentTimeMillis()
            )

            if (reportBuffer.add(report)) {
                val reportId = database.reportDao().insertReport(report).toInt()
                val savedReport = report.copy(id = reportId)

                showReportNotification(savedReport)
            }
        }
    }

    private fun showReadyNotification() {
        sendOverlayEvent(Intent(ACTION_READY))

        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager.notify(2, NotificationManager.buildOCRReadyNotification(this).build())
        notificationManager.notify(3, NotificationManager.buildReadyNotification(this).build())
    }

    private fun showReportNotification(report: Report) {
        sendOverlayEvent(
            Intent(ACTION_NEW_REPORT)
                .putExtra(EXTRA_REPORT_ID, report.id)
        )

        val notification = NotificationManager.buildReportNotification(
            this,
            report.nickname,
            report.playerId,
            report.text,
            report.reportCount
        ).build()

        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager.notify(
            report.playerId,  // Use playerId as notification ID
            notification
        )
    }

    private fun sendOverlayEvent(intent: Intent) {
        startService(intent.setClass(this, OverlayService::class.java))
    }

    companion object {
        private const val RESULT_OK = -1
        const val ACTION_READY = "com.vihttools.mobile.ACTION_READY"
        const val ACTION_NEW_REPORT = "com.vihttools.mobile.ACTION_NEW_REPORT"
        const val EXTRA_REPORT_ID = "extra_report_id"
    }
}
