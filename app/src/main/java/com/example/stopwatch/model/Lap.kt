package com.example.stopwatch.model

/**
 * Represents a single lap record within a stopwatch session.
 *
 * A lap captures the timing information for a specific segment of a stopwatch session,
 * storing both the individual lap time and the cumulative time from the start.
 *
 * @property lapNumber Sequential number of this lap (1-based indexing)
 * @property lapTime Time duration for this specific lap in milliseconds
 * @property totalTime Accumulated time from the start up to this lap in milliseconds
 *
 * @author Sergio Octavio Mancebo
 * @since 1.0
 *
 * @example
 * ```kotlin
 * // First lap after 30 seconds
 * val lap1 = Lap(lapNumber = 1, lapTime = 30000, totalTime = 30000)
 *
 * // Second lap taking 25 seconds (total time now 55 seconds)
 * val lap2 = Lap(lapNumber = 2, lapTime = 25000, totalTime = 55000)
 * ```
 */
data class Lap(
    val lapNumber: Int,
    val lapTime: Long,
    val totalTime: Long
)