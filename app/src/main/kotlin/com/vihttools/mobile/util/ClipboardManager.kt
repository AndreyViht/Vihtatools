package com.vihttools.mobile.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ClipboardManager {

    /**
     * Copy text to clipboard and show toast
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Команда") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show()
    }

    /**
     * Get text from clipboard
     */
    fun getFromClipboard(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        return if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).text?.toString() ?: ""
        } else {
            ""
        }
    }

    /**
     * Check if clipboard has text
     */
    fun hasText(context: Context): Boolean {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        return clipboard.primaryClip != null && clipboard.primaryClip!!.itemCount > 0
    }
}
