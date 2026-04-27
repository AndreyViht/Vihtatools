package com.vihttools.mobile.ui.panel

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vihttools.mobile.R
import com.vihttools.mobile.data.Report

class ReportsListPanel(
    private val context: Context,
    private val windowManager: WindowManager,
    private val onReportSelected: (Report) -> Unit,
    private val onClose: () -> Unit
) {

    private var panelView: FrameLayout? = null
    private var isShowing = false

    fun show(reports: List<Report>) {
        if (isShowing) return

        createPanelView(reports)
        isShowing = true
    }

    fun hide() {
        if (panelView != null && isShowing) {
            windowManager.removeView(panelView)
            panelView = null
            isShowing = false
            onClose()
        }
    }

    private fun createPanelView(reports: List<Report>) {
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
            width = 300
            height = 400
            gravity = Gravity.TOP or Gravity.LEFT
            x = 20
            y = 100
        }

        panelView = FrameLayout(context).apply {
            setBackgroundColor(0xFF1A1A1A.toInt())
            setOnTouchListener { _, event -> handlePanelTouch(event) }
        }

        // Header
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                48
            )
            setBackgroundColor(0xFF2D2D2D.toInt())
        }

        val titleText = TextView(context).apply {
            text = "Reports (${reports.size})"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                setMargins(12, 0, 0, 0)
            }
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        header.addView(titleText)

        val closeBtn = TextView(context).apply {
            text = "✕"
            textSize = 18f
            setTextColor(0xFFB0B0B0.toInt())
            layoutParams = LinearLayout.LayoutParams(
                48,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = android.view.Gravity.CENTER
            setOnClickListener { hide() }
        }
        header.addView(closeBtn)

        panelView?.addView(header)

        // Reports list
        val scrollView = ScrollView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                topMargin = 48
            }
        }

        val listContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (report in reports) {
            val reportItem = createReportItem(report)
            listContainer.addView(reportItem)
        }

        scrollView.addView(listContainer)
        panelView?.addView(scrollView)

        windowManager.addView(panelView, params)
    }

    private fun createReportItem(report: Report): View {
        val itemContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0xFF2D2D2D.toInt())
            setPadding(12, 8, 12, 8)
            setOnClickListener { onReportSelected(report) }
        }

        // Nickname and ID
        val nicknameText = TextView(context).apply {
            text = "${report.nickname} [${report.playerId}]"
            textSize = 13f
            setTextColor(
                if (report.isAnswered) 0xFF606060.toInt()
                else 0xFFE63946.toInt()
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        itemContainer.addView(nicknameText)

        // Report text
        val textView = TextView(context).apply {
            text = report.text
            textSize = 11f
            setTextColor(0xFFB0B0B0.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
            maxLines = 2
        }
        itemContainer.addView(textView)

        // Report count
        val countText = TextView(context).apply {
            text = "Reports: ${report.reportCount}"
            textSize = 10f
            setTextColor(0xFF606060.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
        }
        itemContainer.addView(countText)

        return itemContainer
    }

    private fun handlePanelTouch(event: MotionEvent): Boolean {
        return true  // Consume touch events
    }
}
