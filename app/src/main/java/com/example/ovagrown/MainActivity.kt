package com.example.overgrown

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.canDrawOverlays
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.overgrown.navigation.AppNavigation
import com.example.overgrown.overlay.OverlayService
import com.example.overgrown.tracker.PermissionHelper
import com.example.overgrown.ui.theme.OVAgrownTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var refreshPermissionState: (() -> Unit)? = null

    override fun onResume() {
        super.onResume()
        refreshPermissionState?.invoke()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()


        setContent {
            var hasUsagePermission by remember {
                mutableStateOf(
                    PermissionHelper.hasUsageAccess(this)
                )
            }

            var showUsageDialog by remember {
                mutableStateOf(false)
            }

            refreshPermissionState = {
                hasUsagePermission =
                    PermissionHelper.hasUsageAccess(this)
            }

            OVAgrownTheme {
                AppNavigation(
                    onStartTracking = {
                        if (PermissionHelper.hasUsageAccess(this@MainActivity)) {
                            hasUsagePermission = true

                            if (canDrawOverlays(this@MainActivity)) {
                                startService(
                                    Intent(
                                        this@MainActivity,
                                        OverlayService::class.java
                                    )
                                )
                            } else {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                )
                                startActivity(intent)
                            }
                        } else {
                            showUsageDialog = true
                        }
                    },
                    onStopTracking = {
                        stopService(
                            Intent(
                                this@MainActivity,
                                OverlayService::class.java
                            )
                        )
                    },
                    hasUsagePermission = hasUsagePermission
                )

                if (showUsageDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showUsageDialog = false
                        },
                        title = {
                            Text("Usage Access Required")
                        },
                        text = {
                            Text(
                                "OVAgrown needs Usage Access so it can detect which app is being used and grow flowers when the app limit is exceeded."
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showUsageDialog = false

                                    PermissionHelper.openUsageAccessSettings(
                                        this@MainActivity
                                    )
                                }
                            ) {
                                Text("Open Settings")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showUsageDialog = false
                                }
                            ) {
                                Text("Later")
                            }
                        }
                    )
                }
            }
        }
    }
}
