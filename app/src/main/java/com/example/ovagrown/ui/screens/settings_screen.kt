package com.example.ovagrown.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ovagrown.ui.components.AppBottomNavigationBar
import com.example.ovagrown.ui.components.BottomNavPage

data class SettingsUiState(
    val accessibilityEnabled: Boolean,
    val overlayPermissionEnabled: Boolean,
    val usageAccessEnabled: Boolean,
    val trackedApps: List<TrackedAppSetting>,
    val showOverlayWhileScrolling: Boolean,
    val blockAfterTimeLimit: Boolean,
    val showWarningAt25Minutes: Boolean,
    val selectedPlayerIconRes: Int? = null,
    val selectedPlayerIconName: String = "Current icon"
)

data class TrackedAppSetting(
    val name: String,
    val packageName: String,
    val enabled: Boolean
)

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onTrackedAppToggle: (packageName: String, enabled: Boolean) -> Unit = { _, _ -> },
    onShowOverlayWhileScrollingChange: (Boolean) -> Unit = {},
    onBlockAfterTimeLimitChange: (Boolean) -> Unit = {},
    onShowWarningAt25MinutesChange: (Boolean) -> Unit = {},
    onChangeIconClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onTimerClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF7F3E8),
        bottomBar = {
            AppBottomNavigationBar(
                selectedPage = BottomNavPage.Settings,
                onSettingsClick = onSettingsClick,
                onStatsClick = onStatsClick,
                onHomeClick = onHomeClick,
                onRewardsClick = onRewardsClick,
                onTimerClick = onTimerClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(
                top = 20.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4F2F)
                )
            }

            item {
                PermissionsSection(
                    accessibilityEnabled = uiState.accessibilityEnabled,
                    overlayPermissionEnabled = uiState.overlayPermissionEnabled,
                    usageAccessEnabled = uiState.usageAccessEnabled,
                    onAccessibilityManageClick = {
                        openAccessibilitySettings(context)
                    },
                    onOverlayManageClick = {
                        openOverlaySettings(context)
                    },
                    onUsageAccessManageClick = {
                        openUsageAccessSettings(context)
                    }
                )
            }

            item {
                TrackedAppsSection(
                    trackedApps = uiState.trackedApps,
                    onTrackedAppToggle = onTrackedAppToggle
                )
            }

            item {
                OverlayBehaviourSection(
                    showOverlayWhileScrolling = uiState.showOverlayWhileScrolling,
                    blockAfterTimeLimit = uiState.blockAfterTimeLimit,
                    showWarningAt25Minutes = uiState.showWarningAt25Minutes,
                    onShowOverlayWhileScrollingChange = onShowOverlayWhileScrollingChange,
                    onBlockAfterTimeLimitChange = onBlockAfterTimeLimitChange,
                    onShowWarningAt25MinutesChange = onShowWarningAt25MinutesChange
                )
            }

            item {
                ProfileSection(
                    selectedPlayerIconRes = uiState.selectedPlayerIconRes,
                    selectedPlayerIconName = uiState.selectedPlayerIconName,
                    onChangeIconClick = onChangeIconClick
                )
            }

            item {
                SignOutSection(
                    onSignOutClick = onSignOutClick
                )
            }
        }
    }
}

@Composable
fun PermissionsSection(
    accessibilityEnabled: Boolean,
    overlayPermissionEnabled: Boolean,
    usageAccessEnabled: Boolean,
    onAccessibilityManageClick: () -> Unit,
    onOverlayManageClick: () -> Unit,
    onUsageAccessManageClick: () -> Unit
) {
    SettingsCard {
        SectionTitle(title = "Permissions")

        Spacer(modifier = Modifier.height(12.dp))

        PermissionRow(
            enabled = accessibilityEnabled,
            title = "Accessibility Service",
            description = "Detects when you are using selected apps and scrolling.",
            onManageClick = onAccessibilityManageClick
        )

        SettingsDivider()

        PermissionRow(
            enabled = overlayPermissionEnabled,
            title = "Display over other apps",
            description = "Allows vines/flowers to appear on top of Instagram/TikTok.",
            onManageClick = onOverlayManageClick
        )

        SettingsDivider()

        PermissionRow(
            enabled = usageAccessEnabled,
            title = "Usage Access",
            description = "Lets Overgrown read app usage time summaries.",
            onManageClick = onUsageAccessManageClick
        )
    }
}

@Composable
fun PermissionRow(
    enabled: Boolean,
    title: String,
    description: String,
    onManageClick: () -> Unit
) {
    val statusText = if (enabled) "✓" else "!"
    val statusColor = if (enabled) Color(0xFF2E7D32) else Color(0xFFF57C00)
    val statusBackground = if (enabled) Color(0xFFDCEFD8) else Color(0xFFFFE0B2)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    color = statusBackground,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF304830)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF5D705D)
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedButton(
            onClick = onManageClick
        ) {
            Text(text = "Manage")
        }
    }
}

@Composable
fun TrackedAppsSection(
    trackedApps: List<TrackedAppSetting>,
    onTrackedAppToggle: (packageName: String, enabled: Boolean) -> Unit
) {
    SettingsCard {
        SectionTitle(title = "Tracked Apps")

        Spacer(modifier = Modifier.height(12.dp))

        trackedApps.forEachIndexed { index, app ->
            TrackedAppRow(
                app = app,
                onCheckedChange = { enabled ->
                    onTrackedAppToggle(app.packageName, enabled)
                }
            )

            if (index != trackedApps.lastIndex) {
                SettingsDivider()
            }
        }
    }
}

@Composable
fun TrackedAppRow(
    app: TrackedAppSetting,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = app.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF304830),
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = app.enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun OverlayBehaviourSection(
    showOverlayWhileScrolling: Boolean,
    blockAfterTimeLimit: Boolean,
    showWarningAt25Minutes: Boolean,
    onShowOverlayWhileScrollingChange: (Boolean) -> Unit,
    onBlockAfterTimeLimitChange: (Boolean) -> Unit,
    onShowWarningAt25MinutesChange: (Boolean) -> Unit
) {
    SettingsCard {
        SectionTitle(title = "Overlay Behaviour")

        Spacer(modifier = Modifier.height(12.dp))

        OverlayBehaviourRow(
            title = "Show overlay while scrolling",
            checked = showOverlayWhileScrolling,
            onCheckedChange = onShowOverlayWhileScrollingChange
        )

        SettingsDivider()

        OverlayBehaviourRow(
            title = "Block screen after time limit",
            checked = blockAfterTimeLimit,
            onCheckedChange = onBlockAfterTimeLimitChange
        )

        SettingsDivider()

        OverlayBehaviourRow(
            title = "Show warning at 25 minutes",
            checked = showWarningAt25Minutes,
            onCheckedChange = onShowWarningAt25MinutesChange
        )
    }
}

@Composable
fun OverlayBehaviourRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF304830),
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ProfileSection(
    selectedPlayerIconRes: Int?,
    selectedPlayerIconName: String,
    onChangeIconClick: () -> Unit
) {
    SettingsCard {
        SectionTitle(title = "Profile")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Selected player icon:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF304830)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCEFD8)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedPlayerIconRes != null) {
                    Image(
                        painter = painterResource(id = selectedPlayerIconRes),
                        contentDescription = selectedPlayerIconName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "Icon",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = selectedPlayerIconName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF304830)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Change icon >",
                    fontSize = 14.sp,
                    color = Color(0xFF5D705D)
                )
            }

            OutlinedButton(
                onClick = onChangeIconClick
            ) {
                Text(text = "Change")
            }
        }
    }
}

@Composable
fun SignOutSection(
    onSignOutClick: () -> Unit
) {
    SettingsCard {
        SectionTitle(title = "Account")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Sign out of your Overgrown account.",
            fontSize = 15.sp,
            color = Color(0xFF5D705D)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onSignOutClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9E2A2B)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sign out",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF0)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2F4F2F)
    )
}

@Composable
fun SettingsDivider() {
    Spacer(modifier = Modifier.height(12.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE6E0D4))
    )

    Spacer(modifier = Modifier.height(12.dp))
}

private fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}

private fun openOverlaySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    context.startActivity(intent)
}

private fun openUsageAccessSettings(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        uiState = SettingsUiState(
            accessibilityEnabled = true,
            overlayPermissionEnabled = true,
            usageAccessEnabled = false,
            trackedApps = listOf(
                TrackedAppSetting(
                    name = "Instagram",
                    packageName = "com.instagram.android",
                    enabled = true
                ),
                TrackedAppSetting(
                    name = "TikTok",
                    packageName = "com.zhiliaoapp.musically",
                    enabled = true
                ),
                TrackedAppSetting(
                    name = "YouTube Shorts",
                    packageName = "com.google.android.youtube",
                    enabled = false
                ),
                TrackedAppSetting(
                    name = "Reddit",
                    packageName = "com.reddit.frontpage",
                    enabled = false
                )
            ),
            showOverlayWhileScrolling = true,
            blockAfterTimeLimit = true,
            showWarningAt25Minutes = true,
            selectedPlayerIconRes = null,
            selectedPlayerIconName = "Current icon"
        )
    )
}

