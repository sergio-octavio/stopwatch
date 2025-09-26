package com.example.stopwatch.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stopwatch.model.Lap
import com.example.stopwatch.model.Stopwatch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StopwatchViewModel : ViewModel() {
    private val _stopwatches = mutableStateOf<List<Stopwatch>>(emptyList())
    val stopwatches: State<List<Stopwatch>> = _stopwatches

    private val timerJobs = mutableMapOf<String, Job>()

    fun addStopwatch() {
        val newStopwatch = Stopwatch(name = "Cronómetro ${_stopwatches.value.size + 1}")
        _stopwatches.value = _stopwatches.value + newStopwatch
    }

    fun removeStopwatch(id: String) {
        timerJobs[id]?.cancel()
        timerJobs.remove(id)
        _stopwatches.value = _stopwatches.value.filter { it.id != id }
    }

    fun startStopwatch(id: String) {
        val stopwatch = _stopwatches.value.find { it.id == id } ?: return

        if (!stopwatch.isRunning) {
            val currentTime = System.currentTimeMillis()
            val updatedStopwatch = stopwatch.copy(
                isRunning = true,
                startTime = currentTime - stopwatch.currentTime
            )
            updateStopwatch(updatedStopwatch)
            startTimer(id)
        }
    }

    fun stopStopwatch(id: String) {
        val stopwatch = _stopwatches.value.find { it.id == id } ?: return

        if (stopwatch.isRunning) {
            timerJobs[id]?.cancel()
            timerJobs.remove(id)

            val updatedStopwatch = stopwatch.copy(isRunning = false)
            updateStopwatch(updatedStopwatch)
        }
    }

    fun recordLap(id: String) {
        val stopwatch = _stopwatches.value.find { it.id == id } ?: return

        if (stopwatch.isRunning) {
            val currentTime = stopwatch.currentTime
            val previousLapTime = if (stopwatch.laps.isEmpty()) 0L else stopwatch.laps.last().totalTime
            val lapTime = currentTime - previousLapTime

            val newLap = Lap(
                lapNumber = stopwatch.laps.size + 1,
                lapTime = lapTime,
                totalTime = currentTime
            )

            val updatedStopwatch = stopwatch.copy(
                laps = stopwatch.laps + newLap
            )
            updateStopwatch(updatedStopwatch)
        }
    }

    fun resetStopwatch(id: String) {
        val stopwatch = _stopwatches.value.find { it.id == id } ?: return

        timerJobs[id]?.cancel()
        timerJobs.remove(id)

        val updatedStopwatch = stopwatch.copy(
            currentTime = 0L,
            isRunning = false,
            laps = emptyList(),
            startTime = 0L,
            pausedTime = 0L
        )
        updateStopwatch(updatedStopwatch)
    }

    fun updateStopwatchName(id: String, newName: String) {
        val stopwatch = _stopwatches.value.find { it.id == id } ?: return
        val updatedStopwatch = stopwatch.copy(name = newName.trim().ifEmpty { "Cronómetro" })
        updateStopwatch(updatedStopwatch)
    }

    private fun startTimer(id: String) {
        timerJobs[id] = viewModelScope.launch {
            while (true) {
                delay(10) // Update every 10ms for smooth animation
                val stopwatch = _stopwatches.value.find { it.id == id }
                if (stopwatch != null && stopwatch.isRunning) {
                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = currentTime - stopwatch.startTime
                    val updatedStopwatch = stopwatch.copy(currentTime = elapsedTime)
                    updateStopwatch(updatedStopwatch)
                } else {
                    break
                }
            }
        }
    }

    private fun updateStopwatch(updatedStopwatch: Stopwatch) {
        _stopwatches.value = _stopwatches.value.map { stopwatch ->
            if (stopwatch.id == updatedStopwatch.id) updatedStopwatch else stopwatch
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
    }
}