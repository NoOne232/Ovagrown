package com.example.ovagrown.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ovagrown.R
import com.example.ovagrown.ui.screens.DayUsage
import com.example.ovagrown.ui.screens.HomeRoute
import com.example.ovagrown.ui.screens.LoginScreen
import com.example.ovagrown.ui.screens.PlayerIcon
import com.example.ovagrown.ui.screens.SettingsScreen
import com.example.ovagrown.ui.screens.SettingsUiState
import com.example.ovagrown.ui.screens.SummaryScreen
import com.example.ovagrown.ui.screens.SummaryUiState
import com.example.ovagrown.ui.screens.TimerScreen
import com.example.ovagrown.ui.screens.TrackedAppSetting

enum class AppPage {
    Login,
    Home,
    Settings,
    Summary,
    Rewards,
    Timer
}

@Composable
fun AppNavigation() {
    var currentPage by remember { mutableStateOf(AppPage.Login) }

    val goLogin = { currentPage = AppPage.Login }
    val goSettings = { currentPage = AppPage.Settings }
    val goStats = { currentPage = AppPage.Summary }
    val goHome = { currentPage = AppPage.Home }
    val goRewards = { currentPage = AppPage.Rewards }
    val goTimer = { currentPage = AppPage.Timer }

    when (currentPage) {
        AppPage.Login -> {
            LoginScreen(
                onLoginSuccess = {
                    currentPage = AppPage.Home
                }
            )
        }

        AppPage.Home -> {
            HomeRoute(
                onSettingsClick = goSettings,
                onStatsClick = goStats,
                onHomeClick = goHome,
                onRewardsClick = goRewards,
                onTimerClick = goTimer
            )
        }

        AppPage.Settings -> {
            SettingsScreen(
                uiState = dummySettingsUiState(),
                onSignOutClick = {
                    // This sends the user back to the login screen.
                    // Later, you can also call Supabase logout here.
                    goLogin()
                },
                onSettingsClick = goSettings,
                onStatsClick = goStats,
                onHomeClick = goHome,
                onRewardsClick = goRewards,
                onTimerClick = goTimer
            )
        }

        AppPage.Summary -> {
            SummaryScreen(
                uiState = dummySummaryUiState(),
                onSettingsClick = goSettings,
                onStatsClick = goStats,
                onHomeClick = goHome,
                onRewardsClick = goRewards,
                onTimerClick = goTimer
            )
        }

        AppPage.Rewards -> {
            // Temporary placeholder until you make a rewards_screen.kt
            SummaryScreen(
                uiState = dummySummaryUiState(),
                onSettingsClick = goSettings,
                onStatsClick = goStats,
                onHomeClick = goHome,
                onRewardsClick = goRewards,
                onTimerClick = goTimer
            )
        }

        AppPage.Timer -> {
            TimerScreen(
                onStartMonitoring = {
                    println("Monitoring started")
                },
                onStopMonitoring = {
                    println("Monitoring stopped")
                },
                onSettingsClick = goSettings,
                onStatsClick = goStats,
                onHomeClick = goHome,
                onRewardsClick = goRewards,
                onTimerClick = goTimer
            )
        }
    }
}

fun dummySettingsUiState(): SettingsUiState {
    return SettingsUiState(
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
        selectedPlayerIconRes = R.drawable.pfp_flower1,
        selectedPlayerIconName = "Flower 1"
    )
}

fun dummySummaryUiState(): SummaryUiState {
    return SummaryUiState(
        thisWeekUsage = listOf(
            DayUsage("Monday", 25),
            DayUsage("Tuesday", 41),
            DayUsage("Wednesday", 18),
            DayUsage("Thursday", 29),
            DayUsage("Friday", 22),
            DayUsage("Saturday", 35),
            DayUsage("Sunday", 20)
        ),
        lastWeekUsage = listOf(
            DayUsage("Monday", 40),
            DayUsage("Tuesday", 33),
            DayUsage("Wednesday", 31),
            DayUsage("Thursday", 28),
            DayUsage("Friday", 37),
            DayUsage("Saturday", 42),
            DayUsage("Sunday", 30)
        ),
        playerIcons = listOf(
            PlayerIcon("pfp_flower1", "Flower 1", R.drawable.pfp_flower1, true),
            PlayerIcon("pfp_flower2", "Flower 2", R.drawable.pfp_flower2, true),
            PlayerIcon("pfp_flower3", "Flower 3", R.drawable.pfp_flower3, false),
            PlayerIcon("pfp_flower4", "Flower 4", R.drawable.pfp_flower4, true),
            PlayerIcon("pfp_clover", "Clover", R.drawable.pfp_clover, true)
        ),
        currentStreak = 3,
        longestStreak = 8
    )
}