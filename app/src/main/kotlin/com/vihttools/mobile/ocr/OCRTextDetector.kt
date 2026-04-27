package com.vihttools.mobile.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OCRTextDetector {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Recognize text from a bitmap image
     */
    suspend fun recognizeText(bitmap: Bitmap): String {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val visionText = textRecognizer.process(image).await()
            visionText.text
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Check if the game welcome message is detected
     */
    fun isGameDetected(text: String): Boolean {
        val welcomePatterns = listOf(
            "Добро пожаловать на Grand Mobile RolePlay",
            "Welcome to Grand Mobile RolePlay",
            "Grand Mobile RolePlay"
        )
        return welcomePatterns.any { pattern ->
            text.contains(pattern, ignoreCase = true)
        }
    }

    /**
     * Extract reports from recognized text
     * Pattern: Nickname[ID]: text [К-во репорта: N]
     */
    fun extractReports(text: String): List<ReportData> {
        val reports = mutableListOf<ReportData>()
        
        // Pattern: Nickname[ID]: text [К-во репорта: N]
        val reportPattern = Regex("""(\w+)\[(\d+)\]:\s*(.+?)\s*\[К-во репорта:\s*(\d+)\]""")
        
        val matches = reportPattern.findAll(text)
        for (match in matches) {
            val nickname = match.groupValues[1]
            val playerId = match.groupValues[2].toIntOrNull() ?: continue
            val reportText = match.groupValues[3]
            val reportCount = match.groupValues[4].toIntOrNull() ?: 1

            reports.add(
                ReportData(
                    nickname = nickname,
                    playerId = playerId,
                    text = reportText,
                    reportCount = reportCount
                )
            )
        }

        return reports
    }

    /**
     * Detect if a line contains a red nickname (report)
     * RGB ≈ #e63946, tolerance ±40 per channel
     * Analyzes pixel colors in the bitmap and checks for red text
     */
    fun isRedNickname(bitmap: Bitmap, lineIndex: Int): Boolean {
        val redPercentage = ColorDetector.analyzeRedPixels(bitmap)
        // Consider it a red nickname if at least 5% of pixels are red
        return redPercentage >= 5.0f
    }

    /**
     * Filter out admin messages (orange color #ff6b35)
     */
    fun isAdminMessage(text: String): Boolean {
        val adminPatterns = listOf(
            "<ADM>",
            "[ADM]",
            "ответил"
        )
        return adminPatterns.any { pattern ->
            text.contains(pattern, ignoreCase = true)
        }
    }

    data class ReportData(
        val nickname: String,
        val playerId: Int,
        val text: String,
        val reportCount: Int
    )
}
