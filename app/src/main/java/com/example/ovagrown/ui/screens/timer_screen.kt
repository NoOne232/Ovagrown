package com.example.ovagrown.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ovagrown.R
import com.example.ovagrown.overlay.OverlayService
import com.example.ovagrown.timer.DailyScrollRepository
import com.example.ovagrown.ui.components.AppBottomNavigationBar
import com.example.ovagrown.ui.components.BottomNavPage
import com.example.ovagrown.ui.theme.OVAgrownTheme
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(
    onStartMonitoring: () -> Unit = {},
    onStopMonitoring: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onTimerClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContext = context.applicationContext

    val dailyScrollRepository = remember {
        DailyScrollRepository(appContext)
    }

    var monitoringOn by remember {
        mutableStateOf(dailyScrollRepository.isSessionRunning())
    }

    fun stopMonitoringSession() {
        dailyScrollRepository.stopSession()

        OverlayService.stop(
            context = appContext
        )

        monitoringOn = false
        onStopMonitoring()
    }

    if (monitoringOn) {
        TimerOnScreen(
            getRemainingSeconds = {
                dailyScrollRepository.getRemainingSecondsToday()
            },
            onTimerFinished = {
                stopMonitoringSession()
            },
            onStopClick = {
                stopMonitoringSession()
            },
            onTick = { remainingSeconds ->
                val progress = getOverlayProgressFromRemainingSeconds(
                    remainingSeconds = remainingSeconds
                )

                OverlayService.updateProgress(
                    context = appContext,
                    progress = progress
                )
            }
        )
    } else {
        TimerStartScreen(
            onStartClick = {
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )

                    context.startActivity(intent)
                } else {
                    if (!dailyScrollRepository.hasReachedDailyLimit()) {
                        dailyScrollRepository.startSession()

                        val remainingSeconds =
                            dailyScrollRepository.getRemainingSecondsToday()

                        val progress = getOverlayProgressFromRemainingSeconds(
                            remainingSeconds = remainingSeconds
                        )

                        OverlayService.start(
                            context = appContext,
                            progress = progress
                        )

                        monitoringOn = true
                        onStartMonitoring()
                    }
                }
            },
            onSettingsClick = onSettingsClick,
            onStatsClick = onStatsClick,
            onHomeClick = onHomeClick,
            onRewardsClick = onRewardsClick,
            onTimerClick = onTimerClick
        )
    }
}

@Composable
fun TimerStartScreen(
    onStartClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onTimerClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color(0xFFF7F3E8),
        bottomBar = {
            AppBottomNavigationBar(
                selectedPage = BottomNavPage.Timer,
                onSettingsClick = onSettingsClick,
                onStatsClick = onStatsClick,
                onHomeClick = onHomeClick,
                onRewardsClick = onRewardsClick,
                onTimerClick = onTimerClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.timer_screen),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimerStatusCard(
                    monitoringOn = false
                )

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .size(width = 290.dp, height = 92.dp)
                        .clickable {
                            onStartClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.timer_button),
                        contentDescription = "Start monitoring",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "Start monitoring",
                        fontFamily = TropiLandFont,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TimerOnScreen(
    getRemainingSeconds: () -> Int,
    onTimerFinished: () -> Unit,
    onStopClick: () -> Unit,
    onTick: (remainingSeconds: Int) -> Unit = {}
) {
    var remainingSeconds by remember {
        mutableStateOf(getRemainingSeconds())
    }

    LaunchedEffect(Unit) {
        while (true) {
            remainingSeconds = getRemainingSeconds()

            onTick(remainingSeconds)

            if (remainingSeconds <= 0) {
                onTimerFinished()
                break
            }

            delay(1000)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.timer_on),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "touching grass...",
                fontFamily = TropiLandFont,
                fontSize = 28.sp,
                color = Color(0xFF15E832)
            )

            Spacer(modifier = Modifier.height(90.dp))

            Text(
                text = formatTimerTime(remainingSeconds),
                fontFamily = TropiLandFont,
                fontSize = 82.sp,
                color = Color(0xFF15E832)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStopClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.92f)
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(
                    horizontal = 42.dp,
                    vertical = 18.dp
                ),
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Stop",
                    fontFamily = TropiLandFont,
                    fontSize = 26.sp,
                    color = Color(0xFF17721D)
                )
            }
        }
    }
}

@Composable
fun TimerStatusCard(
    monitoringOn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.78f)
        ),
        border = BorderStroke(
            width = 2.dp,
            color = Color(0xFF17721D)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Timer",
                fontFamily = TropiLandFont,
                fontSize = 36.sp,
                color = Color(0xFF2F4F2F)
            )

            Text(
                text = "Controls app detection and the flower overlay.",
                fontSize = 16.sp,
                color = Color(0xFF304830)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Detection status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF304830)
                )

                MonitoringStatusPill(
                    monitoringOn = monitoringOn
                )
            }

            Text(
                text = if (monitoringOn) {
                    "Overgrown is now detecting tracked apps. The flower overlay can appear."
                } else {
                    "Detection is paused. The flower overlay should stay hidden."
                },
                fontSize = 15.sp,
                color = Color(0xFF5D705D)
            )
        }
    }
}

@Composable
fun MonitoringStatusPill(
    monitoringOn: Boolean
) {
    val statusText = if (monitoringOn) "ON" else "OFF"

    val statusColor = if (monitoringOn) {
        Color(0xFF17721D)
    } else {
        Color(0xFF9E2A2B)
    }

    Row(
        modifier = Modifier
            .background(
                color = statusColor.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}

fun getOverlayProgressFromRemainingSeconds(
    remainingSeconds: Int
): Float {
    val totalSeconds = DailyScrollRepository.DAILY_LIMIT_SECONDS
    val usedSeconds = totalSeconds - remainingSeconds

    return usedSeconds
        .toFloat()
        .div(totalSeconds.toFloat())
        .coerceIn(0f, 1f)
}

fun formatTimerTime(
    totalSeconds: Int
): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    val minutesText = minutes.toString().padStart(2, '0')
    val secondsText = seconds.toString().padStart(2, '0')

    return "$minutesText:$secondsText"
}

@Preview(showBackground = true)
@Composable
fun TimerStartScreenPreview() {
    OVAgrownTheme {
        TimerStartScreen(
            onStartClick = {}
        )
    }
}



