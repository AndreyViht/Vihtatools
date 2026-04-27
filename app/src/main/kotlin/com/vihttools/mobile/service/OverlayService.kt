package com.vihttools.mobile.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.vihttools.mobile.data.AppDatabase
import com.vihttools.mobile.data.Report
import com.vihttools.mobile.data.Template
import com.vihttools.mobile.notification.NotificationManager
import com.vihttools.mobile.settings.SettingsManager
import com.vihttools.mobile.ui.panel.OverlayNotificationPanel
import com.vihttools.mobile.ui.panel.QuickReplyPanel
import com.vihttools.mobile.ui.panel.ReportsListPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: FrameLayout? = null
    private var reportCountBadge: TextView? = null
    private var lastX = 0f
    private var lastY = 0f
    private var initialX = 0f
    private var initialY = 0f
    private var isDragging = false
    private var buttonColor = 0x80606060.toInt()  // Gray by default
    private lateinit var database: AppDatabase
    private var reportsPanel: ReportsListPanel? = null
    private var quickReplyPanel: QuickReplyPanel? = null
    private var overlayNotificationPanel: OverlayNotificationPanel? = null

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var settingsJob: Job? = null
    private var reportCountJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        database = AppDatabase.getDatabase(this)
        overlayNotificationPanel = OverlayNotificationPanel(this, windowManager) { report ->
            openQuickReplyPanel(report)
        }
        NotificationManager.createNotificationChannels(this)
        createNotification()
        observeReportCount()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createOverlayButton()
        loadSettings()
        when (intent?.action) {
            ACTION_SHOW_READY -> showReadyOverlayNotification()
            ACTION_SHOW_REPORT -> handleNewReportEvent(intent)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        quickReplyPanel?.hide()
        reportsPanel?.hide()
        overlayNotificationPanel?.hideAll()
        if (overlayView != null) {
            windowManager.removeView(overlayView)
        }
        scope.cancel()
    }

    private fun createNotification() {
        val notification = NotificationManager.buildOverlayNotification(this).build()
        startForeground(1, notification)
    }

    private fun observeReportCount() {
        if (reportCountJob != null) {
            return
        }

        reportCountJob = scope.launch {
            database.reportDao().getUnreadReportCount().collect { count ->
                updateReportCount(count)
                if (count > 0) {
                    setButtonToRed()
                } else {
                    setButtonToGray()
                }
            }
        }
    }

    private fun loadSettings() {
        if (settingsJob != null) {
            return
        }

        settingsJob = scope.launch {
            combine(
                SettingsManager.getButtonPositionX(this@OverlayService),
                SettingsManager.getButtonPositionY(this@OverlayService),
                SettingsManager.getButtonTransparency(this@OverlayService)
            ) { x, y, transparency ->
                Triple(x, y, transparency)
            }.collect { (x, y, transparency) ->
                updateButtonPosition(x, y)
                updateButtonTransparency(transparency)
            }
        }
    }

    private fun createOverlayButton() {
        if (overlayView != null) {
            return
        }

        val params = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            width = 104  // 52dp * 2 for badge
            height = 104
            gravity = Gravity.TOP or Gravity.RIGHT
            x = 0
            y = 100
        }

        overlayView = FrameLayout(this).apply {
            setBackgroundColor(buttonColor)
            setOnTouchListener { _, event -> handleTouchEvent(event) }
        }

        // Add "V" text
        val vText = TextView(this).apply {
            text = "V"
            textSize = 32f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(104, 104).apply {
                gravity = Gravity.CENTER
            }
        }
        overlayView?.addView(vText)

        // Add badge
        reportCountBadge = TextView(this).apply {
            text = "0"
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFE63946.toInt())
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(24, 24).apply {
                gravity = Gravity.TOP or Gravity.RIGHT
            }
            visibility = View.GONE
        }
        overlayView?.addView(reportCountBadge)

        windowManager.addView(overlayView, params)

        // Enable touch events
        params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        windowManager.updateViewLayout(overlayView, params)
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        val params = (overlayView?.layoutParams as? WindowManager.LayoutParams) ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.rawX
                initialY = event.rawY
                lastX = event.rawX
                lastY = event.rawY
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - lastX
                val deltaY = event.rawY - lastY

                if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
                    isDragging = true
                }

                if (isDragging) {
                    params.x += deltaX.toInt()
                    params.y += deltaY.toInt()
                    windowManager.updateViewLayout(overlayView, params)
                }

                lastX = event.rawX
                lastY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    // Handle click - open reports panel
                    openReportsPanel()
                } else {
                    // Save position
                    scope.launch {
                        SettingsManager.setButtonPosition(
                            this@OverlayService,
                            params.x,
                            params.y
                        )
                    }
                }
                isDragging = false
            }
        }
        return true
    }

    private fun openReportsPanel() {
        scope.launch {
            if (reportsPanel != null) {
                reportsPanel?.hide()
                reportsPanel = null
                return@launch
            }

            val reports = database.reportDao().getAllReports().first()
            reportsPanel = ReportsListPanel(
                context = this@OverlayService,
                windowManager = windowManager,
                onReportSelected = { report ->
                    openQuickReplyPanel(report)
                },
                onClose = {
                    reportsPanel = null
                }
            ).also { panel ->
                panel.show(reports)
            }
        }
    }

    private fun openQuickReplyPanel(report: Report) {
        scope.launch {
            reportsPanel?.hide()
            reportsPanel = null
            quickReplyPanel?.hide()

            val templates = database.templateDao().getAllActiveTemplates().first()
                .ifEmpty { defaultTemplates() }
            quickReplyPanel = QuickReplyPanel(
                context = this@OverlayService,
                windowManager = windowManager,
                onTemplateSelected = { selectedReport, _ ->
                    scope.launch {
                        database.reportDao().markAsAnswered(selectedReport.id, System.currentTimeMillis())
                        overlayNotificationPanel?.hideReport(selectedReport.id)
                    }
                    quickReplyPanel?.hide()
                    quickReplyPanel = null
                },
                onClose = {
                    quickReplyPanel = null
                }
            ).also { panel ->
                panel.show(report, templates)
            }
        }
    }

    private fun showReadyOverlayNotification() {
        overlayNotificationPanel?.showReady()
    }

    private fun handleNewReportEvent(intent: Intent) {
        val reportId = intent.getLongExtra(EXTRA_REPORT_ID, 0L).toInt()
        if (reportId == 0) return

        scope.launch {
            val report = database.reportDao().getReportById(reportId) ?: return@launch
            overlayNotificationPanel?.showReport(report)
        }
    }

    private fun defaultTemplates(): List<Template> {
        return listOf(
            Template(label = "ТП", command = "/pm {ID} Здравствуйте, чем могу помочь?", order = 0),
            Template(label = "Слежу", command = "/pm {ID} Принял репорт, начинаю проверку.", order = 1),
            Template(label = "Закр", command = "/pm {ID} Репорт закрыт. Приятной игры.", order = 2)
        )
    }

    fun updateReportCount(count: Int) {
        reportCountBadge?.apply {
            if (count > 0) {
                text = count.toString()
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    fun setButtonColor(color: Int) {
        buttonColor = color
        overlayView?.setBackgroundColor(color)
    }

    fun setButtonToGray() {
        setButtonColor(0x80606060.toInt())
    }

    fun setButtonToRed() {
        setButtonColor(0x80E63946.toInt())
    }

    fun setButtonToGreen() {
        setButtonColor(0x802A9D8F.toInt())
    }

    private fun updateButtonPosition(x: Int, y: Int) {
        val params = overlayView?.layoutParams as? WindowManager.LayoutParams
        if (params != null) {
            params.x = x
            params.y = y
            windowManager.updateViewLayout(overlayView, params)
        }
    }

    private fun updateButtonTransparency(transparency: Float) {
        overlayView?.alpha = transparency
    }

    companion object {
        const val ACTION_SHOW_READY = "com.vihttools.mobile.action.SHOW_READY_OVERLAY"
        const val ACTION_SHOW_REPORT = "com.vihttools.mobile.action.SHOW_REPORT_OVERLAY"
        const val EXTRA_REPORT_ID = "extra_report_id"
    }
}
