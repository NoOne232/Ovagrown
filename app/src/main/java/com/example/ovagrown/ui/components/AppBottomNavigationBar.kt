package com.example.ovagrown.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ovagrown.R

enum class BottomNavPage {
    Settings,
    Stats,
    Home,
    Rewards,
    Timer
}

@Composable
fun AppBottomNavigationBar(
    selectedPage: BottomNavPage,
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onTimerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(Color(0xFF0B6E13))
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavIcon(
            icon = R.drawable.ic_settings,
            contentDescription = "Settings",
            selected = selectedPage == BottomNavPage.Settings,
            onClick = onSettingsClick
        )

        BottomNavIcon(
            icon = R.drawable.ic_stats,
            contentDescription = "Stats",
            selected = selectedPage == BottomNavPage.Stats,
            onClick = onStatsClick
        )

        BottomNavIcon(
            icon = R.drawable.ic_home,
            contentDescription = "Home",
            selected = selectedPage == BottomNavPage.Home,
            onClick = onHomeClick
        )

        BottomNavIcon(
            icon = R.drawable.ic_rewards,
            contentDescription = "Rewards",
            selected = selectedPage == BottomNavPage.Rewards,
            onClick = onRewardsClick
        )

        BottomNavIcon(
            icon = R.drawable.ic_timer,
            contentDescription = "Timer",
            selected = selectedPage == BottomNavPage.Timer,
            onClick = onTimerClick
        )
    }
}

@Composable
private fun BottomNavIcon(
    icon: Int,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val normalIconSize = if (selected) 38.dp else 31.dp
    val pressedIconSize = if (selected) 34.dp else 28.dp

    val animatedIconSize by animateDpAsState(
        targetValue = if (isPressed) pressedIconSize else normalIconSize,
        animationSpec = tween(durationMillis = 120),
        label = "navIconPressAnimation"
    )

    val iconColor = if (selected) {
        Color.White
    } else {
        Color(0xFFB8E6B8)
    }

    Box(
        modifier = Modifier
            .size(54.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(animatedIconSize)
        )
    }
}