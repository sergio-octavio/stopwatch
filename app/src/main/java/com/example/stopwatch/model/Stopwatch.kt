package com.example.stopwatch.model

import java.util.*

data class Stopwatch(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Cronómetro",
    val currentTime: Long = 0L, // Current elapsed time in milliseconds
    val isRunning: Boolean = false,
    val laps: List<Lap> = emptyList(),
    val startTime: Long = 0L, // System time when started
    val pausedTime: Long = 0L // Total paused time
) {
    val formattedTime: String
        get() = formatTime(currentTime)

    companion object {
        fun formatTime(timeInMillis: Long): String {
            val totalSeconds = timeInMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val milliseconds = (timeInMillis % 1000) / 10
            return String.format("%02d:%02d.%02d", minutes, seconds, milliseconds)
        }
    }
}