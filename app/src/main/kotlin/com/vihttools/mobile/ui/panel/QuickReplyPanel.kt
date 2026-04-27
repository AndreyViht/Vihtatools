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
import com.vihttools.mobile.data.Report
import com.vihttools.mobile.data.Template
import com.vihttools.mobile.util.ClipboardManager

class QuickReplyPanel(
    private val context: Context,
    private val windowManager: WindowManager,
    private val onTemplateSelected: (Report, Template) -> Unit,
    private val onClose: () -> Unit
) {

    private var panelView: FrameLayout? = null
    private var isShowing = false
    private var showingTemplates = false

    fun show(report: Report, templates: List<Template>) {
        if (isShowing) return

        createPanelView(report, templates, showTemplates = false)
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

    private fun createPanelView(report: Report, templates: List<Template>, showTemplates: Boolean) {
        showingTemplates = showTemplates
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
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
            x = 0
            y = 0
        }

        // Transparent background container
        panelView = FrameLayout(context).apply {
            setBackgroundColor(0x00000000)  // Fully transparent
            setOnTouchListener { _, event -> handlePanelTouch(event) }
        }

        // Main content container (centered, with buttons)
        val contentContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            setPadding(16, 16, 16, 16)
        }

        // Report info card (semi-transparent dark background)
        val infoCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                320,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0xCC1A1A1A.toInt())  // Semi-transparent dark
            setPadding(16, 12, 16, 12)
        }

        val nicknameText = TextView(context).apply {
            text = "${report.nickname} [${report.playerId}]"
            textSize = 14f
            setTextColor(0xFFE63946.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        infoCard.addView(nicknameText)

        val reportText = TextView(context).apply {
            text = report.text
            textSize = 12f
            setTextColor(0xFFB0B0B0.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
            }
            maxLines = 2
        }
        infoCard.addView(reportText)

        contentContainer.addView(infoCard)

        val spacer1 = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                16
            )
        }
        contentContainer.addView(spacer1)

        if (showingTemplates) {
            contentContainer.addView(createTemplatesList(report, templates))
        } else {
            contentContainer.addView(createAnswerButton(report, templates))
        }

        // Spacer
        val spacer2 = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                16
            )
        }
        contentContainer.addView(spacer2)

        // Close button
        val closeButton = createCloseButton()
        contentContainer.addView(closeButton)

        panelView?.addView(contentContainer)

        windowManager.addView(panelView, params)
    }

    private fun showTemplates(report: Report, templates: List<Template>) {
        hideWithoutCallback()
        createPanelView(report, templates, showTemplates = true)
        isShowing = true
    }

    private fun hideWithoutCallback() {
        if (panelView != null && isShowing) {
            windowManager.removeView(panelView)
            panelView = null
            isShowing = false
        }
    }

    private fun createAnswerButton(report: Report, templates: List<Template>): View {
        val answerButton = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                320,
                52
            )
            setBackgroundColor(0xCCE63946.toInt())
            setPadding(16, 12, 16, 12)
            isClickable = true
            isFocusable = true
            setOnClickListener { showTemplates(report, templates) }
        }

        val answerText = TextView(context).apply {
            text = "Ответ"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = android.view.Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        answerButton.addView(answerText)

        return answerButton
    }

    private fun createTemplatesList(report: Report, templates: List<Template>): View {
        val scrollView = ScrollView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                320,
                400
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        val buttonsContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for ((index, template) in templates.withIndex()) {
            val buttonView = createTemplateButton(report, template)
            buttonsContainer.addView(buttonView)

            if (index < templates.size - 1) {
                val spacing = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        8
                    )
                }
                buttonsContainer.addView(spacing)
            }
        }

        scrollView.addView(buttonsContainer)
        return scrollView
    }

    private fun createTemplateButton(report: Report, template: Template): View {
        val buttonContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                320,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0xCC2D2D2D.toInt())  // Semi-transparent dark
            setPadding(16, 12, 16, 12)
            isClickable = true
            isFocusable = true

            // Ripple effect on click
            setOnClickListener {
                val formattedCommand = template.formatCommand(report.playerId)
                ClipboardManager.copyToClipboard(context, formattedCommand, template.label)
                onTemplateSelected(report, template)
            }

            // Hover effect
            setOnHoverListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_HOVER_ENTER -> {
                        setBackgroundColor(0xDD457B9D.toInt())  // Lighter on hover
                    }
                    MotionEvent.ACTION_HOVER_EXIT -> {
                        setBackgroundColor(0xCC2D2D2D.toInt())  // Back to normal
                    }
                }
                true
            }
        }

        // Template label (title)
        val labelText = TextView(context).apply {
            text = template.label
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        buttonContainer.addView(labelText)

        // Template command (preview)
        val commandText = TextView(context).apply {
            text = template.formatCommand(report.playerId)
            textSize = 11f
            setTextColor(0xFF457B9D.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 6
            }
            maxLines = 1
        }
        buttonContainer.addView(commandText)

        return buttonContainer
    }

    private fun createCloseButton(): View {
        val closeButton = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                320,
                48
            )
            setBackgroundColor(0xCC606060.toInt())  // Semi-transparent gray
            setPadding(16, 12, 16, 12)
            isClickable = true
            isFocusable = true

            setOnClickListener { hide() }

            setOnHoverListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_HOVER_ENTER -> {
                        setBackgroundColor(0xDD808080.toInt())
                    }
                    MotionEvent.ACTION_HOVER_EXIT -> {
                        setBackgroundColor(0xCC606060.toInt())
                    }
                }
                true
            }
        }

        val closeText = TextView(context).apply {
            text = "Закрыть"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        closeButton.addView(closeText)

        return closeButton
    }

    private fun handlePanelTouch(event: MotionEvent): Boolean {
        // Allow touches to pass through transparent areas
        return false
    }
}
