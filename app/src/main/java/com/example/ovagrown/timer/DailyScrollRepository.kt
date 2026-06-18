package com.example.overgrown.timer

import android.content.Context
import java.time.LocalDate

class DailyScrollRepository(
    context: Context
) {
    private val prefs = context.getSharedPreferences(
        "daily_scroll_prefs",
        Context.MODE_PRIVATE
    )

    private val todayKey: String
        get() = LocalDate.now().toString()

    fun startSession() {
        ensureTodayIsCorrect()

        val alreadyStarted = prefs.getLong(KEY_SESSION_START_MILLIS, 0L)

        if (alreadyStarted == 0L) {
            prefs.edit()
                .putLong(KEY_SESSION_START_MILLIS, System.currentTimeMillis())
                .apply()
        }
    }

    fun stopSession() {
        ensureTodayIsCorrect()

        val sessionStartMillis = prefs.getLong(KEY_SESSION_START_MILLIS, 0L)

        if (sessionStartMillis == 0L) return

        val now = System.currentTimeMillis()
        val sessionSeconds = ((now - sessionStartMillis) / 1000L).toInt()

        val currentUsedSeconds = prefs.getInt(KEY_USED_SECONDS_TODAY, 0)
        val newUsedSeconds = currentUsedSeconds + sessionSeconds

        prefs.edit()
            .putInt(KEY_USED_SECONDS_TODAY, newUsedSeconds)
            .remove(KEY_SESSION_START_MILLIS)
            .apply()
    }

    fun resetSessionWithoutSaving() {
        prefs.edit()
            .remove(KEY_SESSION_START_MILLIS)
            .apply()
    }

    fun getUsedSecondsToday(): Int {
        ensureTodayIsCorrect()

        val savedUsedSeconds = prefs.getInt(KEY_USED_SECONDS_TODAY, 0)
        val sessionStartMillis = prefs.getLong(KEY_SESSION_START_MILLIS, 0L)

        if (sessionStartMillis == 0L) {
            return savedUsedSeconds
        }

        val activeSessionSeconds =
            ((System.currentTimeMillis() - sessionStartMillis) / 1000L).toInt()

        return savedUsedSeconds + activeSessionSeconds
    }

    fun getRemainingSecondsToday(): Int {
        return getUsedSecondsToday()
    }

    fun hasReachedDailyLimit(): Boolean {
        return false
    }

    fun isSessionRunning(): Boolean {
        ensureTodayIsCorrect()

        return prefs.getLong(KEY_SESSION_START_MILLIS, 0L) != 0L
    }

    private fun ensureTodayIsCorrect() {
        val savedDate = prefs.getString(KEY_DATE, null)
        val currentDate = todayKey

        if (savedDate != currentDate) {
            prefs.edit()
                .putString(KEY_DATE, currentDate)
                .putInt(KEY_USED_SECONDS_TODAY, 0)
                .remove(KEY_SESSION_START_MILLIS)
                .apply()
        }
    }

    companion object {
        const val DAILY_LIMIT_SECONDS = 30 * 60

        private const val KEY_DATE = "date"
        private const val KEY_USED_SECONDS_TODAY = "used_seconds_today"
        private const val KEY_SESSION_START_MILLIS = "session_start_millis"
    }
}
