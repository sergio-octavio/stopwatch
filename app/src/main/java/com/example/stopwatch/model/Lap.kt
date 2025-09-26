package com.example.stopwatch.model

data class Lap(
    val lapNumber: Int,
    val lapTime: Long, // Time for this specific lap in milliseconds
    val totalTime: Long // Accumulated time up to this lap in milliseconds
)