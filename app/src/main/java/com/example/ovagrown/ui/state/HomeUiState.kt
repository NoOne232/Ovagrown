package com.example.ovagrown.ui.state

import com.example.ovagrown.R

data class HomeUiState(
    val usedMinutesToday: Int = 20,
    val dailyLimitMinutes: Int = 30,
    val monitoringOn: Boolean = false,
    val trackedApps: List<String> = emptyList(),

    // This stores which profile picture the user selected.
    // Since your profile pictures are PNG files in res/drawable,
    // we store the drawable resource ID as an Int.
    val profileImageResId: Int = R.drawable.pfp_flower3
) {
    val overgrownPercent: Int
        get() {
            if (dailyLimitMinutes <= 0) return 0

            return ((usedMinutesToday.toFloat() / dailyLimitMinutes) * 100)
                .toInt()
                .coerceIn(0, 100)
        }

    val minutesLeft: Int
        get() {
            return (dailyLimitMinutes - usedMinutesToday)
                .coerceAtLeast(0)
        }
}
