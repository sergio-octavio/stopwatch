package com.example.stopwatch.model

import java.util.*

/**
 * Represents a stopwatch with timing capabilities and lap recording functionality.
 *
 * This data class encapsulates all the state and behavior of a single stopwatch instance,
 * including timing information, running state, and lap history.
 *
 * @property id Unique identifier for this stopwatch instance, generated automatically
 * @property name User-customizable name for the stopwatch (default: "Cronómetro")
 * @property currentTime Current elapsed time in milliseconds since the stopwatch started
 * @property isRunning Boolean flag indicating whether the stopwatch is currently running
 * @property laps List of recorded lap times for this stopwatch
 * @property startTime System timestamp when the stopwatch was started (for calculations)
 * @property pausedTime Total accumulated paused time in milliseconds
 *
 * @author Sergio Octavio Mancebo
 * @since 1.0
 */
data class Stopwatch(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Cronómetro",
    val currentTime: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<Lap> = emptyList(),
    val startTime: Long = 0L,
    val pausedTime: Long = 0L
) {
    /**
     * Returns the current time formatted as a human-readable string.
     * Format: MM:SS.CC (minutes:seconds.centiseconds)
     *
     * @return Formatted time string (e.g., "01:23.45")
     */
    val formattedTime: String
        get() = formatTime(currentTime)

    companion object {
        /**
         * Formats a time value in milliseconds to a human-readable string format.
         *
         * The format is MM:SS.CC where:
         * - MM: Minutes (00-99)
         * - SS: Seconds (00-59)
         * - CC: Centiseconds (00-99)
         *
         * @param timeInMillis Time value in milliseconds to format
         * @return Formatted time string in MM:SS.CC format
         *
         * @example
         * ```kotlin
         * formatTime(63450) // Returns "01:03.45"
         * formatTime(0) // Returns "00:00.00"
         * formatTime(3661000) // Returns "61:01.00"
         * ```
         */
        fun formatTime(timeInMillis: Long): String {
            val totalSeconds = timeInMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val milliseconds = (timeInMillis % 1000) / 10
            return String.format("%02d:%02d.%02d", minutes, seconds, milliseconds)
        }
    }
}