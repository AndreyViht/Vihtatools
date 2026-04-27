package com.vihttools.mobile.ocr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OCRTextDetector {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val reportCountRegex = Regex(
        """(?:к-?\s*во|кол-?\s*во|количество)\s+репорт[а-я]*\s*:?\s*(\d+)""",
        RegexOption.IGNORE_CASE
    )
    private val reportLineRegexes = listOf(
        Regex(
            """(?:\d{1,2}[:.]\d{2}\s*)?([A-Za-zА-Яа-яЁё0-9_.-]{2,32})\s*[\[\(]\s*(\d{1,10})\s*[\]\)]\s*(?:[:：\-]\s*)?(.+)""",
            RegexOption.IGNORE_CASE
        ),
        Regex(
            """(?:репорт|жалоба).*?(?:от|на)\s+([A-Za-zА-Яа-яЁё0-9_ .-]{2,32}).*?[\[\(]\s*(\d{1,10})\s*[\]\)].*?[:：\-]\s*(.+)""",
            RegexOption.IGNORE_CASE
        ),
        Regex(
            """(?:игрок|player)\s+([A-Za-zА-Яа-яЁё0-9_ .-]{2,32})\s*(?:id|ид)?\s*[:#№]?\s*(\d{1,10})\s*[:：\-]\s*(.+)""",
            RegexOption.IGNORE_CASE
        )
    )

    /**
     * Recognize text from a bitmap image
     */
    suspend fun recognizeText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return processImage(textRecognizer, image)
    }

    /**
     * Check if the game welcome message is detected
     */
    fun isGameDetected(text: String, bitmap: Bitmap? = null): Boolean {
        val normalizedText = normalizeForSearch(text)
        val welcomePatterns = listOf(
            "добро пожаловать",
            "добро пожаловать на grand mobile",
            "grand mobile roleplay",
            "grand mobile",
            "welcome to grand mobile"
        )
        val textDetected = welcomePatterns.any { pattern ->
            normalizedText.contains(normalizeForSearch(pattern))
        }
        return textDetected || bitmap?.let { hasWelcomeLineColors(it) } == true
    }

    /**
     * Fallback for the yellow welcome/system line in the chat area.
     */
    fun hasWelcomeLineColors(bitmap: Bitmap): Boolean {
        val width = bitmap.width
        val height = bitmap.height
        val scanWidth = (width * 0.75f).toInt().coerceAtLeast(1)
        val scanHeight = (height * 0.45f).toInt().coerceAtLeast(1)
        val rowStep = 2
        val colStep = 2

        for (y in 0 until scanHeight step rowStep) {
            var yellowPixelsInRow = 0
            var longestRun = 0
            var currentRun = 0

            for (x in 0 until scanWidth step colStep) {
                if (isWelcomeYellow(bitmap.getPixel(x, y))) {
                    yellowPixelsInRow++
                    currentRun++
                    longestRun = maxOf(longestRun, currentRun)
                } else {
                    currentRun = 0
                }
            }

            if (yellowPixelsInRow >= 8 && longestRun >= 3) {
                return true
            }
        }

        return false
    }

    /**
     * Extract reports from recognized text
     * Pattern: Nickname[ID]: text [К-во репорта: N]
     */
    fun extractReports(text: String): List<ReportData> {
        val reports = mutableListOf<ReportData>()

        val lines = text
            .replace("\r", "\n")
            .split('\n')
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val candidateLines = buildList {
            addAll(lines)
            for (index in 0 until lines.lastIndex) {
                add("${lines[index]} ${lines[index + 1]}")
            }
            for (index in 0 until lines.size - 2) {
                add("${lines[index]} ${lines[index + 1]} ${lines[index + 2]}")
            }
            add(lines.joinToString(" "))
        }.distinct()

        for (line in candidateLines) {
            val normalizedLine = normalizeForSearch(line)
            if (!looksLikeReportLine(normalizedLine) || isAdminMessage(line)) {
                continue
            }

            for (regex in reportLineRegexes) {
                val match = regex.find(line) ?: continue
                val nickname = cleanupNickname(match.groupValues[1])
                val playerId = match.groupValues[2].toIntOrNull() ?: continue
                val reportText = cleanupReportText(match.groupValues[3])
                if (nickname.isBlank() || reportText.isBlank()) {
                    continue
                }

                reports.add(
                    ReportData(
                        nickname = nickname,
                        playerId = playerId,
                        text = reportText,
                        reportCount = extractReportCount(line)
                    )
                )
                break
            }
        }

        return reports.distinctBy { "${it.nickname.lowercase()}:${it.playerId}" }
    }

    /**
     * Detect if a line contains a red nickname (report)
     * RGB ≈ #e63946, tolerance ±40 per channel
     */
    fun isRedNickname(bitmap: Bitmap, lineIndex: Int): Boolean {
        // This is a placeholder - actual implementation would analyze pixel colors
        // in the specific line where the nickname appears
        return false  // TODO: Implement pixel color analysis
    }

    /**
     * Filter out admin messages (orange color #ff6b35)
     */
    fun isAdminMessage(text: String): Boolean {
        val normalizedText = normalizeForSearch(text)
        val adminPatterns = listOf(
            "<adm>",
            "[adm]",
            "администратор",
            "админ",
            "ответил"
        )
        return adminPatterns.any { pattern ->
            normalizedText.contains(normalizeForSearch(pattern))
        }
    }

    private fun looksLikeReportLine(normalizedLine: String): Boolean {
        return normalizedLine.contains("репорт") ||
                normalizedLine.contains("жалоб") ||
                reportLineRegexes.any { it.containsMatchIn(normalizedLine) }
    }

    private fun extractReportCount(line: String): Int {
        return reportCountRegex.find(line)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
    }

    private fun cleanupNickname(value: String): String {
        return value
            .replace(Regex("""^(?:репорт|жалоба|от|на|игрок|player)\s+""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun cleanupReportText(value: String): String {
        return Regex("""[\[\(]\s*(?:к-?\s*во|кол-?\s*во|количество)\s+репорт[а-я]*\s*:?\s*\d+\s*[\]\)]""", RegexOption.IGNORE_CASE)
            .replace(value, "")
            .replace(reportCountRegex, "")
            .replace(Regex("""[\[\]()\s]+$"""), "")
            .trim()
    }

    private fun normalizeForSearch(value: String): String {
        return value
            .lowercase()
            .replace('ё', 'е')
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private suspend fun processImage(recognizer: TextRecognizer, image: InputImage): String {
        return try {
            recognizer.process(image).await().text
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun isWelcomeYellow(pixel: Int): Boolean {
        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)
        return red >= 150 &&
                green >= 110 &&
                blue <= 150 &&
                red > blue + 45 &&
                green > blue + 35 &&
                red + green >= 285
    }

    data class ReportData(
        val nickname: String,
        val playerId: Int,
        val text: String,
        val reportCount: Int
    )
}
