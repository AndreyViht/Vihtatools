package com.vihttools.mobile.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.vihttools.mobile.R
import com.vihttools.mobile.notification.NotificationManager
import com.vihttools.mobile.settings.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

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

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        NotificationManager.createNotificationChannels(this)
        createNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createOverlayButton()
        loadSettings()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            windowManager.removeView(overlayView)
        }
        scope.cancel()
    }

    private fun createNotification() {
        val notification = NotificationManager.buildOverlayNotification(this).build()
        startForeground(1, notification)
    }

    private fun loadSettings() {
        scope.launch {
            // Load button position
            SettingsManager.getButtonPositionX(this@OverlayService).collect { x ->
                updateButtonPosition(x, lastY.toInt())
            }

            // Load transparency
            SettingsManager.getButtonTransparency(this@OverlayService).collect { transparency ->
                updateButtonTransparency(transparency)
            }
        }
    }

    private fun createOverlayButton() {
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
        // TODO: Implement reports panel opening
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
}
