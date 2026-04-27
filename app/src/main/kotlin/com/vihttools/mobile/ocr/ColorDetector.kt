package com.vihttools.mobile.ocr

import android.graphics.Bitmap
import kotlin.math.abs

object ColorDetector {

    // Target colors
    private const val RED_NICKNAME_R = 230  // #e63946
    private const val RED_NICKNAME_G = 57
    private const val RED_NICKNAME_B = 70

    private const val ORANGE_ADMIN_R = 255  // #ff6b35
    private const val ORANGE_ADMIN_G = 107
    private const val ORANGE_ADMIN_B = 53

    private const val TOLERANCE = 40

    /**
     * Check if a color is close to red (report nickname)
     */
    fun isRedColor(r: Int, g: Int, b: Int): Boolean {
        return isColorInRange(r, g, b, RED_NICKNAME_R, RED_NICKNAME_G, RED_NICKNAME_B, TOLERANCE)
    }

    /**
     * Check if a color is close to orange (admin message)
     */
    fun isOrangeColor(r: Int, g: Int, b: Int): Boolean {
        return isColorInRange(r, g, b, ORANGE_ADMIN_R, ORANGE_ADMIN_G, ORANGE_ADMIN_B, TOLERANCE)
    }

    /**
     * Check if a color is within range of target color
     */
    private fun isColorInRange(
        r: Int, g: Int, b: Int,
        targetR: Int, targetG: Int, targetB: Int,
        tolerance: Int
    ): Boolean {
        return abs(r - targetR) <= tolerance &&
                abs(g - targetG) <= tolerance &&
                abs(b - targetB) <= tolerance
    }

    /**
     * Analyze a bitmap for red pixels (report nicknames)
     * Returns percentage of red pixels found
     */
    fun analyzeRedPixels(bitmap: Bitmap): Float {
        var redPixelCount = 0
        var totalPixels = 0

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                if (isRedColor(r, g, b)) {
                    redPixelCount++
                }
                totalPixels++
            }
        }

        return if (totalPixels > 0) {
            (redPixelCount.toFloat() / totalPixels.toFloat()) * 100
        } else {
            0f
        }
    }

    /**
     * Find the dominant color in a bitmap
     */
    fun findDominantColor(bitmap: Bitmap): Int {
        val colorMap = mutableMapOf<Int, Int>()

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                colorMap[pixel] = (colorMap[pixel] ?: 0) + 1
            }
        }

        return colorMap.maxByOrNull { it.value }?.key ?: 0
    }

    /**
     * Extract RGB components from a pixel
     */
    fun extractRGB(pixel: Int): Triple<Int, Int, Int> {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return Triple(r, g, b)
    }

    /**
     * Create a pixel from RGB components
     */
    fun createPixel(r: Int, g: Int, b: Int, a: Int = 255): Int {
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
