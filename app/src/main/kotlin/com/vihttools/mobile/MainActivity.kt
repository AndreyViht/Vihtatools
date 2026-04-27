package com.vihttools.mobile

import android.Manifest
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vihttools.mobile.notification.NotificationManager
import com.vihttools.mobile.permission.PermissionManager
import com.vihttools.mobile.service.OCRMonitoringService
import com.vihttools.mobile.service.OverlayService
import com.vihttools.mobile.ui.screen.HomeScreen
import com.vihttools.mobile.ui.screen.PermissionScreen
import com.vihttools.mobile.ui.screen.SettingsScreen
import com.vihttools.mobile.ui.theme.VihtToolsMobileTheme

class MainActivity : ComponentActivity() {

    private val mediaProjectionManager by lazy {
        getSystemService(MediaProjectionManager::class.java)
    }

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Permission request completed, check status
            checkPermissions()
        }

    private val mediaProjectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                // MediaProjection permission granted
                startOverlayService(result.data!!)
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkPermissions()
            }
        }

    private var isOverlayRunning by mutableStateOf(false)
    private var activeReports by mutableStateOf(0)
    private var respondedReports by mutableStateOf(0)
    private var permissionRefreshToken by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationManager.createNotificationChannels(this)

        setContent {
            VihtToolsMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1A1A1A)
                ) {
                    val permissionRefresh = permissionRefreshToken
                    val missingPermissions = remember(permissionRefresh) {
                        PermissionManager.getMissingPermissions(this@MainActivity)
                    }
                    var permissionSkipped by remember { mutableStateOf(false) }
                    var currentScreen by remember { mutableStateOf("home") }

                    if (missingPermissions.isNotEmpty() && !permissionSkipped) {
                        PermissionScreen(
                            missingPermissions = missingPermissions,
                            onGoToSettings = { requestMissingPermissions() },
                            onSkip = { permissionSkipped = true }
                        )
                    } else {
                        when (currentScreen) {
                            "settings" -> SettingsScreen(
                                onBack = { currentScreen = "home" }
                            )

                            else -> HomeScreen(
                                isOverlayRunning = isOverlayRunning,
                                activeReports = activeReports,
                                respondedReports = respondedReports,
                                onStartOverlay = { startOverlay() },
                                onStopOverlay = { stopOverlay() },
                                onNavigateToSettings = { currentScreen = "settings" }
                            )
                        }
                    }
                }
            }
        }

        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
    }

    private fun checkPermissions() {
        refreshPermissionState()
        if (!PermissionManager.hasAllPermissions(this)) {
            requestMissingPermissions()
        }
    }

    private fun refreshPermissionState() {
        permissionRefreshToken++
    }

    private fun requestMissingPermissions() {
        val missing = PermissionManager.getMissingPermissions(this)

        if (missing.contains("OVERLAY")) {
            val intent = PermissionManager.getOverlayPermissionIntent(this)
            overlayPermissionLauncher.launch(intent)
        } else if (missing.contains("NOTIFICATION") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun startOverlay() {
        if (!PermissionManager.hasAllPermissions(this)) {
            requestMissingPermissions()
            return
        }

        val intent = mediaProjectionManager.createScreenCaptureIntent()
        mediaProjectionLauncher.launch(intent)
    }

    private fun startOverlayService(data: Intent) {
        val overlayIntent = Intent(this, OverlayService::class.java)
        val ocrIntent = Intent(this, OCRMonitoringService::class.java).apply {
            putExtra("media_projection_data", data)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(overlayIntent)
            startForegroundService(ocrIntent)
        } else {
            startService(overlayIntent)
            startService(ocrIntent)
        }
        isOverlayRunning = true
    }

    private fun stopOverlay() {
        stopService(Intent(this, OverlayService::class.java))
        stopService(Intent(this, OCRMonitoringService::class.java))
        isOverlayRunning = false
    }
}
