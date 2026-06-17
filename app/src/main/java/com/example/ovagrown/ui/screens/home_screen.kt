package com.example.ovagrown.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovagrown.R
import com.example.ovagrown.ui.components.AppBottomNavigationBar
import com.example.ovagrown.ui.components.BottomNavPage
import com.example.ovagrown.ui.state.HomeUiState
import com.example.ovagrown.ui.theme.OVAgrownTheme
import com.example.ovagrown.ui.viewmodel.HomeViewModel

@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel = viewModel(),
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onTimerClick: () -> Unit = {}
) {
    val uiState by homeViewModel.uiState.collectAsState()

    HomeScreen(
        uiState = uiState,
        onSettingsClick = onSettingsClick,
        onStatsClick = onStatsClick,
        onHomeClick = onHomeClick,
        onRewardsClick = onRewardsClick,
        onTimerClick = onTimerClick
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onTimerClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 52.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            HeaderCard(
                profileImageResId = uiState.profileImageResId
            )

            TodayGrowthCard(
                overgrownPercent = uiState.overgrownPercent,
                minutesLeft = uiState.minutesLeft
            )

            MonitoringStatusCard(
                monitoringOn = uiState.monitoringOn
            )

            AppsTrackedCard(
                trackedApps = uiState.trackedApps
            )
        }

        AppBottomNavigationBar(
            selectedPage = BottomNavPage.Home,
            onSettingsClick = onSettingsClick,
            onStatsClick = onStatsClick,
            onHomeClick = onHomeClick,
            onRewardsClick = onRewardsClick,
            onTimerClick = onTimerClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun HeaderCard(
    profileImageResId: Int
) {
    Surface(
        color = Color.White.copy(alpha = 0.72f),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(
            width = 2.dp,
            color = Color(0xFF17721D)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Welcome back",
                fontFamily = TropiLandFont,
                fontSize = 32.sp,
                color = Color.Black
            )

            ProfileImage(
                profileImageResId = profileImageResId
            )
        }
    }
}

@Composable
fun ProfileImage(
    profileImageResId: Int
) {
    Image(
        painter = painterResource(id = profileImageResId),
        contentDescription = "Profile image",
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .border(
                width = 3.dp,
                color = Color(0xFF17721D),
                shape = CircleShape
            ),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun TodayGrowthCard(
    overgrownPercent: Int,
    minutesLeft: Int
) {
    Surface(
        color = Color.White.copy(alpha = 0.74f),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(
            width = 2.dp,
            color = Color(0xFF17721D)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Today you are:",
                fontFamily = TropiLandFont,
                fontSize = 28.sp,
                color = Color.Black
            )

            Text(
                text = "$overgrownPercent% overgrown",
                fontFamily = TropiLandFont,
                fontSize = 34.sp,
                color = Color(0xFF17721D)
            )

            Text(
                text = "You have $minutesLeft minutes of scrolling left",
                fontFamily = TropiLandFont,
                fontSize = 22.sp,
                color = Color.Black
            )

            OvergrownProgressBar(
                progress = overgrownPercent / 100f
            )
        }
    }
}

@Composable
fun MonitoringStatusCard(
    monitoringOn: Boolean
) {
    Surface(
        color = Color.White.copy(alpha = 0.74f),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(
            width = 2.dp,
            color = Color(0xFF17721D)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Monitoring",
                fontFamily = TropiLandFont,
                fontSize = 28.sp,
                color = Color.Black
            )

            StatusPill(
                monitoringOn = monitoringOn
            )
        }
    }
}

@Composable
fun StatusPill(
    monitoringOn: Boolean
) {
    val statusText = if (monitoringOn) "ON" else "OFF"

    val statusColor = if (monitoringOn) {
        Color(0xFF17721D)
    } else {
        Color(0xFF9E2A2B)
    }

    Surface(
        color = statusColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(
            width = 1.5.dp,
            color = statusColor
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = statusColor,
                        shape = CircleShape
                    )
            )

            Text(
                text = statusText,
                fontSize = 18.sp,
                color = statusColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppsTrackedCard(
    trackedApps: List<String>
) {
    Surface(
        color = Color.White.copy(alpha = 0.74f),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(
            width = 2.dp,
            color = Color(0xFF17721D)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Apps tracked:",
                fontFamily = TropiLandFont,
                fontSize = 28.sp,
                color = Color.Black
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                trackedApps.forEach { appName ->
                    AppTrackedChip(appName = appName)
                }
            }
        }
    }
}

@Composable
fun OvergrownProgressBar(
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(
                color = Color.White.copy(alpha = 0.55f),
                shape = RoundedCornerShape(50.dp)
            )
            .border(
                width = 2.dp,
                color = Color(0xFF17721D),
                shape = RoundedCornerShape(50.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(
                    color = Color(0xFF17721D).copy(alpha = 0.75f),
                    shape = RoundedCornerShape(50.dp)
                )
        )
    }
}

@Composable
fun AppTrackedChip(
    appName: String
) {
    Surface(
        color = Color.White.copy(alpha = 0.82f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(
            width = 1.5.dp,
            color = Color(0xFF17721D)
        )
    ) {
        Text(
            text = appName,
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenOnlyPreview() {
    OVAgrownTheme {
        HomeScreen(
            uiState = HomeUiState(
                usedMinutesToday = 10,
                dailyLimitMinutes = 30,
                monitoringOn = true,
                trackedApps = listOf("Instagram", "TikTok", "YouTube Shorts", "lol"),
                profileImageResId = R.drawable.app_icon
            )
        )
    }
}