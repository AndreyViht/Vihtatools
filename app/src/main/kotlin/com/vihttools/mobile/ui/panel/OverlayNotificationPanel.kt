package com.vihttools.mobile.ui.panel

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.vihttools.mobile.data.Report

class OverlayNotificationPanel(
    private val context: Context,
    private val windowManager: WindowManager,
    private val onReportClicked: (Report) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var readyView: View? = null
    private val reportViews = linkedMapOf<Int, View>()

    fun showReady() {
        readyView?.let { windowManager.removeView(it) }

        val view = createCard(
            title = "Viht aTools",
            message = "Готов к работе",
            accentColor = 0xFF2A9D8F.toInt()
        ).apply {
            setOnClickListener { hideReady() }
        }
        readyView = view
        windowManager.addView(view, createParams(y = 220))

        handler.removeCallbacksAndMessages(READY_TOKEN)
        handler.postAtTime({ hideReady() }, READY_TOKEN, System.currentTimeMillis() + READY_HIDE_DELAY_MS)
    }

    fun showReport(report: Report) {
        if (report.isAnswered || reportViews.containsKey(report.id)) {
            return
        }

        val view = createCard(
            title = "Новый репорт",
            message = "${report.nickname} [${report.playerId}]\n${report.text}",
            accentColor = 0xFFE63946.toInt()
        ).apply {
            setOnClickListener { onReportClicked(report) }
        }

        val index = reportViews.size
        reportViews[report.id] = view
        windowManager.addView(view, createParams(y = REPORT_START_Y + index * REPORT_SPACING_Y))
    }

    fun hideReport(reportId: Int) {
        val view = reportViews.remove(reportId) ?: return
        windowManager.removeView(view)
        relayoutReports()
    }

    fun hideAll() {
        hideReady()
        reportViews.values.forEach { windowManager.removeView(it) }
        reportViews.clear()
    }

    private fun hideReady() {
        val view = readyView ?: return
        readyView = null
        windowManager.removeView(view)
    }

    private fun relayoutReports() {
        reportViews.values.forEachIndexed { index, view ->
            val params = view.layoutParams as? WindowManager.LayoutParams ?: return@forEachIndexed
            params.y = REPORT_START_Y + index * REPORT_SPACING_Y
            windowManager.updateViewLayout(view, params)
        }
    }

    private fun createCard(title: String, message: String, accentColor: Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xEE1A1A1A.toInt())
            setPadding(18, 14, 18, 14)
            isClickable = true
            isFocusable = false

            addView(TextView(context).apply {
                text = title
                textSize = 14f
                setTextColor(accentColor)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            addView(TextView(context).apply {
                text = message
                textSize = 12f
                setTextColor(0xFFFFFFFF.toInt())
                maxLines = 3
            })
        }
    }

    private fun createParams(y: Int): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            width = 360
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.RIGHT
            x = 18
            this.y = y
        }
    }

    private companion object {
        private const val READY_HIDE_DELAY_MS = 3500L
        private const val REPORT_START_Y = 352
        private const val REPORT_SPACING_Y = 132
        private val READY_TOKEN = Any()
    }
}
