package com.example.ovagrown.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ovagrown.R
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import com.example.ovagrown.ui.components.AppBottomNavigationBar
import com.example.ovagrown.ui.components.BottomNavPage

data class DayUsage(
    val dayName: String,
    val timeUsedMinutes: Int
)

data class PlayerIcon(
    val id: String,
    val name: String,
    val imageRes: Int,
    val owned: Boolean
)

data class SummaryUiState(
    val thisWeekUsage: List<DayUsage>,
    val lastWeekUsage: List<DayUsage>,
    val playerIcons: List<PlayerIcon>,
    val currentStreak: Int,
    val longestStreak: Int
)

@Composable
fun SummaryScreen(
    uiState: SummaryUiState,
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onTimerClick: () -> Unit = {}
) {
    val thisWeekAverage = averageUsage(uiState.thisWeekUsage)
    val lastWeekAverage = averageUsage(uiState.lastWeekUsage)
    val difference = thisWeekAverage - lastWeekAverage

    Scaffold(
        containerColor = Color(0xFFF7F3E8),
        bottomBar = {
            AppBottomNavigationBar(
                selectedPage = BottomNavPage.Stats,
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
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = "Weekly Summary",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4F2F)
                )
            }

            item {
                DailyUsageSection(
                    weekUsage = uiState.thisWeekUsage
                )
            }

            item {
                PlayerIconsOwnedSection(
                    icons = uiState.playerIcons
                )
            }

            item {
                AverageScrollTimeSection(
                    thisWeekAverage = thisWeekAverage,
                    lastWeekAverage = lastWeekAverage,
                    difference = difference
                )
            }

            item {
                StreakSection(
                    currentStreak = uiState.currentStreak,
                    longestStreak = uiState.longestStreak
                )
            }
        }
    }
}

@Composable
fun DailyUsageSection(
    weekUsage: List<DayUsage>
) {
    SummaryCard {
        Text(
            text = "Daily Usage",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4F2F)
        )

        Spacer(modifier = Modifier.height(12.dp))

        weekUsage.forEach { day ->
            DailyUsageRow(dayUsage = day)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun DailyUsageRow(
    dayUsage: DayUsage
) {
    val goalMinutes = 30
    val progress = (dayUsage.timeUsedMinutes / goalMinutes.toFloat()).coerceAtMost(1f)

    val usageStatusText = when {
        dayUsage.timeUsedMinutes < goalMinutes -> "Within limit ✓"
        dayUsage.timeUsedMinutes == goalMinutes -> "Limit reached !"
        else -> "Limit exceeded ×"
    }

    val statusColor = when {
        dayUsage.timeUsedMinutes < goalMinutes -> Color(0xFF2E7D32)
        dayUsage.timeUsedMinutes == goalMinutes -> Color(0xFFF57C00)
        else -> Color(0xFFB00020)
    }

    val progressColor = when {
        dayUsage.timeUsedMinutes < goalMinutes -> Color(0xFF81C784)
        dayUsage.timeUsedMinutes == goalMinutes -> Color(0xFFFFB74D)
        else -> Color(0xFFE57373)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayUsage.dayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF304830)
            )

            Text(
                text = usageStatusText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = progressColor,
            trackColor = Color(0xFFE6E0D4)
        )
    }
}

@Composable
fun PlayerIconsOwnedSection(
    icons: List<PlayerIcon>
) {
    val ownedIcons = icons.filter { it.owned }

    SummaryCard {
        Text(
            text = "Player Icons Owned",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4F2F)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(ownedIcons) { icon ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = icon.imageRes),
                        contentDescription = icon.name,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = icon.name,
                        fontSize = 12.sp,
                        color = Color(0xFF304830)
                    )
                }
            }
        }
    }
}

@Composable
fun AverageScrollTimeSection(
    thisWeekAverage: Int,
    lastWeekAverage: Int,
    difference: Int
) {
    val comparisonText = when {
        difference < 0 -> "You scrolled ${-difference} mins less than last week"
        difference > 0 -> "You scrolled $difference mins more than last week"
        else -> "Same average as last week"
    }

    SummaryCard {
        Text(
            text = "Average Scroll Time",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4F2F)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "This week: $thisWeekAverage mins",
            fontSize = 17.sp,
            color = Color(0xFF304830)
        )

        Text(
            text = "Last week: $lastWeekAverage mins",
            fontSize = 17.sp,
            color = Color(0xFF304830)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = comparisonText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (difference <= 0) Color(0xFF2E7D32) else Color(0xFFB00020)
        )
    }
}

@Composable
fun StreakSection(
    currentStreak: Int,
    longestStreak: Int
) {
    SummaryCard {
        Text(
            text = "Streaks",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4F2F)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StreakBox(
                title = "Current Streak",
                value = "$currentStreak days",
                modifier = Modifier.weight(1f)
            )

            StreakBox(
                title = "Longest Streak",
                value = "$longestStreak days",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StreakBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFDCEFD8),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFF4C644C)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4F2F)
            )
        }
    }
}

@Composable
fun SummaryCard(
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

fun averageUsage(
    weekUsage: List<DayUsage>
): Int {
    if (weekUsage.isEmpty()) return 0
    return weekUsage.map { it.timeUsedMinutes }.average().toInt()
}

@Preview(showBackground = true)
@Composable
fun SummaryScreenPreview() {
    SummaryScreen(
        uiState = SummaryUiState(
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
                PlayerIcon(
                    id = "pfp_flower1",
                    name = "Flower 1",
                    imageRes = R.drawable.pfp_flower1,
                    owned = true
                ),
                PlayerIcon(
                    id = "pfp_flower2",
                    name = "Flower 2",
                    imageRes = R.drawable.pfp_flower2,
                    owned = true
                ),
                PlayerIcon(
                    id = "pfp_flower3",
                    name = "Flower 3",
                    imageRes = R.drawable.pfp_flower3,
                    owned = false
                ),
                PlayerIcon(
                    id = "pfp_flower4",
                    name = "Flower 4",
                    imageRes = R.drawable.pfp_flower4,
                    owned = true
                ),
                PlayerIcon(
                    id = "pfp_clover",
                    name = "Clover",
                    imageRes = R.drawable.pfp_clover,
                    owned = true
                )
            ),
            currentStreak = 3,
            longestStreak = 8
        )
    )
}
