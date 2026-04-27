package com.vihttools.mobile.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ScreenCaptureManager(
    private val context: Context,
    private val mediaProjection: MediaProjection
) {

    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val displayMetrics = DisplayMetrics()
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        mediaProjection.registerCallback(
            object : MediaProjection.Callback() {
                override fun onStop() {
                    release()
                }
            },
            mainHandler
        )
    }

    /**
     * Capture the chat area of the screen
     * Chat area is typically in the top-left corner
     */
    suspend fun captureChatArea(): Bitmap? {
        return try {
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            val density = displayMetrics.densityDpi

            // Define chat area (top-left, approximately 1/3 of screen)
            val chatAreaWidth = (screenWidth * 0.4).toInt()
            val chatAreaHeight = (screenHeight * 0.3).toInt()

            captureScreenArea(0, 0, chatAreaWidth, chatAreaHeight, density)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Capture a specific area of the screen
     */
    private suspend fun captureScreenArea(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        density: Int
    ): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            try {
                virtualDisplay?.release()
                imageReader?.close()

                // Create ImageReader for capturing frames
                imageReader = ImageReader.newInstance(
                    width,
                    height,
                    PixelFormat.RGBA_8888,
                    2
                )

                imageReader?.setOnImageAvailableListener({ reader ->
                    try {
                        val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener

                        val bitmap = imageToBitmap(image)
                        image.close()

                        continuation.resume(bitmap)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }, mainHandler)

                // Create virtual display
                virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenCapture",
                    width,
                    height,
                    density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface,
                    null,
                    null
                )
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Convert Image to Bitmap
     */
    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val w = image.width

        val bitmap = Bitmap.createBitmap(w, image.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)

        return bitmap
    }

    /**
     * Release resources
     */
    fun release() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
    }
}
