package com.example.ovagrown.timer

import android.content.Context

class TimerRepository(
    context: Context
) {
    private val prefs = context.getSharedPreferences(
        "overgrown_timer_prefs",
        Context.MODE_PRIVATE
    )

    fun startTimer(
        durationMillis: Long = 30 * 60 * 1000L
    ) {
        val endTimeMillis = System.currentTimeMillis() + durationMillis

        prefs.edit()
            .putLong(KEY_END_TIME_MILLIS, endTimeMillis)
            .apply()
    }

    fun stopTimer() {
        prefs.edit()
            .remove(KEY_END_TIME_MILLIS)
            .apply()
    }

    fun isTimerRunning(): Boolean {
        val endTimeMillis = prefs.getLong(KEY_END_TIME_MILLIS, 0L)

        if (endTimeMillis == 0L) {
            return false
        }

        val stillRunning = System.currentTimeMillis() < endTimeMillis

        if (!stillRunning) {
            stopTimer()
        }

        return stillRunning
    }

    fun getRemainingSeconds(): Int {
        val endTimeMillis = prefs.getLong(KEY_END_TIME_MILLIS, 0L)

        if (endTimeMillis == 0L) {
            return 0
        }

        val remainingMillis = endTimeMillis - System.currentTimeMillis()
        val remainingSeconds = (remainingMillis / 1000L).toInt().coerceAtLeast(0)

        if (remainingSeconds == 0) {
            stopTimer()
        }

        return remainingSeconds
    }

    companion object {
        private const val KEY_END_TIME_MILLIS = "end_time_millis"
    }
}

