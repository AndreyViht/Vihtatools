package com.vihttools.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vihttools.mobile.permission.PermissionManager
import com.vihttools.mobile.service.OverlayService
import com.vihttools.mobile.ui.screen.HomeScreen
import com.vihttools.mobile.ui.screen.PermissionScreen
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VihtToolsMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1A1A1A)
                ) {
                    val missingPermissions = PermissionManager.getMissingPermissions(this@MainActivity)

                    if (missingPermissions.isNotEmpty()) {
                        PermissionScreen(
                            missingPermissions = missingPermissions,
                            onGoToSettings = { requestMissingPermissions() },
                            onSkip = { /* Continue anyway */ }
                        )
                    } else {
                        HomeScreen(
                            isOverlayRunning = isOverlayRunning,
                            activeReports = activeReports,
                            respondedReports = respondedReports,
                            onStartOverlay = { startOverlay() },
                            onStopOverlay = { stopOverlay() },
                            onNavigateToSettings = { /* Navigate to settings */ }
                        )
                    }
                }
            }
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (!PermissionManager.hasAllPermissions(this)) {
            requestMissingPermissions()
        }
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
        if (!PermissionManager.hasOverlayPermission(this)) {
            requestMissingPermissions()
            return
        }

        val mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        mediaProjectionLauncher.launch(intent)
    }

    private fun startOverlayService(data: Intent) {
        val serviceIntent = Intent(this, OverlayService::class.java)
        serviceIntent.putExtra("media_projection_data", data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        isOverlayRunning = true
    }

    private fun stopOverlay() {
        val serviceIntent = Intent(this, OverlayService::class.java)
        stopService(serviceIntent)
        isOverlayRunning = false
    }
}
