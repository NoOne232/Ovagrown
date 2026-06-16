package com.example.ovagrown.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ovagrown.ui.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            usedMinutesToday = 12,
            dailyLimitMinutes = 30,
            monitoringOn = true,
            trackedApps = listOf("Instagram", "TikTok", "YouTube Shorts")
        )
    )

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow() //homeviewmodel holds current ui state

    fun updateUsedMinutes(newUsedMinutes: Int) {
        _uiState.value = _uiState.value.copy(
            usedMinutesToday = newUsedMinutes
        )
    }

    fun updateMonitoringStatus(isMonitoringOn: Boolean) {
        _uiState.value = _uiState.value.copy(
            monitoringOn = isMonitoringOn
        )
    }

    fun updateTrackedApps(newTrackedApps: List<String>) {
        _uiState.value = _uiState.value.copy(
            trackedApps = newTrackedApps
        )
    }

    fun updateDailyLimit(newDailyLimitMinutes: Int) {
        _uiState.value = _uiState.value.copy(
            dailyLimitMinutes = newDailyLimitMinutes
        )
    }
}

