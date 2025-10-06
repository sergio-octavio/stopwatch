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

/**
 * ViewModel responsible for managing multiple stopwatch instances and their state.
 *
 * This ViewModel follows the MVVM architecture pattern and provides a centralized
 * management system for multiple independent stopwatches. It handles:
 * - Creating and removing stopwatch instances
 * - Starting, stopping, and resetting timers
 * - Recording lap times
 * - Managing coroutine-based timer updates
 * - Ensuring proper resource cleanup
 *
 * All timing operations are performed using coroutines with 10ms precision,
 * providing smooth real-time updates for the UI layer.
 *
 * @author Sergio Octavio Mancebo
 * @since 1.0
 */
class StopwatchViewModel : ViewModel() {

    /**
     * Private mutable state holding the list of all stopwatch instances.
     * This is the single source of truth for stopwatch data.
     */
    private val _stopwatches = mutableStateOf<List<Stopwatch>>(emptyList())

    /**
     * Public read-only state exposing the list of stopwatches to the UI.
     * UI components observe this state for automatic recomposition.
     */
    val stopwatches: State<List<Stopwatch>> = _stopwatches

    /**
     * Private mutable state holding the ID of the currently active stopwatch.
     * This determines which stopwatch responds to volume button controls.
     */
    private val _activeStopwatchId = mutableStateOf<String?>(null)

    /**
     * Public read-only state exposing the active stopwatch ID to the UI.
     */
    val activeStopwatchId: State<String?> = _activeStopwatchId

    /**
     * Map storing active coroutine jobs for running timers.
     * Key: Stopwatch ID, Value: Coroutine Job handling the timer updates.
     */
    private val timerJobs = mutableMapOf<String, Job>()

    /**
     * Creates and adds a new stopwatch to the collection.
     *
     * The new stopwatch is initialized with a default name including its sequence number
     * (e.g., "Cronómetro 1", "Cronómetro 2") and default values for all timing properties.
     *
     * @example
     * ```kotlin
     * viewModel.addStopwatch() // Creates "Cronómetro 1"
     * viewModel.addStopwatch() // Creates "Cronómetro 2"
     * ```
     */
    fun addStopwatch() {
        val newStopwatch = Stopwatch(name = "Cronómetro ${_stopwatches.value.size + 1}")
        _stopwatches.value = _stopwatches.value + newStopwatch

        // Set as active if no active stopwatch exists
        if (_activeStopwatchId.value == null) {
            _activeStopwatchId.value = newStopwatch.id
        }
    }

    /**
     * Removes a stopwatch from the collection and cleans up associated resources.
     *
     * This method ensures proper cleanup by:
     * 1. Cancelling any running timer coroutine for the stopwatch
     * 2. Removing the timer job from the jobs map
     * 3. Removing the stopwatch from the state list
     *
     * @param id The unique identifier of the stopwatch to remove
     *
     * @example
     * ```kotlin
     * viewModel.removeStopwatch("stopwatch-uuid-123")
     * ```
     */
    fun removeStopwatch(id: String) {
        timerJobs[id]?.cancel()
        timerJobs.remove(id)
        _stopwatches.value = _stopwatches.value.filter { it.id != id }

        // If removing the active stopwatch, set new active or clear
        if (_activeStopwatchId.value == id) {
            _activeStopwatchId.value = _stopwatches.value.firstOrNull()?.id
        }
    }

    /**
     * Starts a stopped stopwatch or resumes a paused one.
     *
     * This method initiates the timing process by:
     * 1. Setting the stopwatch state to running
     * 2. Calculating the adjusted start time to account for previous elapsed time
     * 3. Starting a coroutine-based timer for real-time updates
     *
     * The method is idempotent - calling it on an already running stopwatch has no effect.
     *
     * @param id The unique identifier of the stopwatch to start
     *
     * @example
     * ```kotlin
     * viewModel.startStopwatch("stopwatch-uuid-123")
     * ```
     */
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

    /**
     * Stops a running stopwatch, preserving the current elapsed time.
     *
     * This method pauses the timing process by:
     * 1. Cancelling the timer coroutine
     * 2. Removing the job from the active jobs map
     * 3. Setting the stopwatch state to stopped
     *
     * The elapsed time is preserved, allowing the stopwatch to be resumed later.
     *
     * @param id The unique identifier of the stopwatch to stop
     *
     * @example
     * ```kotlin
     * viewModel.stopStopwatch("stopwatch-uuid-123")
     * ```
     */
    fun stopStopwatch(id: String) {
        val stopwatch = _stopwatches.value.find { it.id == id } ?: return

        if (stopwatch.isRunning) {
            timerJobs[id]?.cancel()
            timerJobs.remove(id)

            val updatedStopwatch = stopwatch.copy(isRunning = false)
            updateStopwatch(updatedStopwatch)
        }
    }

    /**
     * Records a lap time for a running stopwatch.
     *
     * This method captures the current timing state and creates a new lap record
     * containing both the individual lap time and the cumulative total time.
     * Lap recording is only possible when the stopwatch is actively running.
     *
     * @param id The unique identifier of the stopwatch to record a lap for
     *
     * @example
     * ```kotlin
     * // After running for 30 seconds
     * viewModel.recordLap("stopwatch-uuid-123") // Creates lap with 30s lap time
     *
     * // After running for 25 more seconds (55s total)
     * viewModel.recordLap("stopwatch-uuid-123") // Creates lap with 25s lap time, 55s total
     * ```
     */
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

    /**
     * Sets the active stopwatch for volume button control.
     *
     * @param id The unique identifier of the stopwatch to set as active
     */
    fun setActiveStopwatch(id: String) {
        if (_stopwatches.value.any { it.id == id }) {
            _activeStopwatchId.value = id
        }
    }

    /**
     * Gets the currently active stopwatch for volume button control.
     *
     * @return The active stopwatch or null if none is active
     */
    fun getActiveStopwatch(): Stopwatch? {
        val activeId = _activeStopwatchId.value ?: return null
        return _stopwatches.value.find { it.id == activeId }
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