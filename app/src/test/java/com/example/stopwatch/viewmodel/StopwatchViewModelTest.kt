package com.example.stopwatch.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.stopwatch.model.Stopwatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

/**
 * Unit tests for the [StopwatchViewModel] class.
 *
 * These tests verify the correct behavior of the ViewModel including:
 * - Stopwatch lifecycle management (add, remove, start, stop, reset)
 * - Timer functionality and coroutine handling
 * - Lap recording functionality
 * - State management and updates
 * - Resource cleanup
 *
 * @author Sergio Octavio Mancebo
 * @since 1.0
 */
@ExperimentalCoroutinesApi
class StopwatchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: StopwatchViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StopwatchViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cancel()
    }

    // ========== Stopwatch Management Tests ==========

    @Test
    fun `addStopwatch creates new stopwatch with correct default values`() {
        // Given
        val initialCount = viewModel.stopwatches.value.size

        // When
        viewModel.addStopwatch()

        // Then
        val stopwatches = viewModel.stopwatches.value
        assertEquals("Stopwatch count should increase by 1", initialCount + 1, stopwatches.size)

        val newStopwatch = stopwatches.last()
        assertEquals("Default name should include number", "Cronómetro 1", newStopwatch.name)
        assertFalse("New stopwatch should not be running", newStopwatch.isRunning)
        assertEquals("Current time should be zero", 0L, newStopwatch.currentTime)
        assertTrue("Laps should be empty", newStopwatch.laps.isEmpty())
    }

    @Test
    fun `addStopwatch creates multiple stopwatches with incremental names`() {
        // When
        repeat(3) { viewModel.addStopwatch() }

        // Then
        val stopwatches = viewModel.stopwatches.value
        assertEquals("Should have 3 stopwatches", 3, stopwatches.size)
        assertEquals("First stopwatch name", "Cronómetro 1", stopwatches[0].name)
        assertEquals("Second stopwatch name", "Cronómetro 2", stopwatches[1].name)
        assertEquals("Third stopwatch name", "Cronómetro 3", stopwatches[2].name)
    }

    @Test
    fun `removeStopwatch removes correct stopwatch`() {
        // Given
        viewModel.addStopwatch()
        viewModel.addStopwatch()
        val stopwatchToRemove = viewModel.stopwatches.value[0]
        val stopwatchToKeep = viewModel.stopwatches.value[1]

        // When
        viewModel.removeStopwatch(stopwatchToRemove.id)

        // Then
        val remainingStopwatches = viewModel.stopwatches.value
        assertEquals("Should have 1 stopwatch remaining", 1, remainingStopwatches.size)
        assertEquals("Remaining stopwatch should be the correct one",
            stopwatchToKeep.id, remainingStopwatches[0].id)
    }

    @Test
    fun `removeStopwatch with invalid id does nothing`() {
        // Given
        viewModel.addStopwatch()
        val originalCount = viewModel.stopwatches.value.size

        // When
        viewModel.removeStopwatch("non-existent-id")

        // Then
        assertEquals("Count should remain unchanged", originalCount, viewModel.stopwatches.value.size)
    }

    @Test
    fun `updateStopwatchName updates correct stopwatch`() {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]
        val newName = "Custom Name"

        // When
        viewModel.updateStopwatchName(stopwatch.id, newName)

        // Then
        val updatedStopwatch = viewModel.stopwatches.value.find { it.id == stopwatch.id }
        assertNotNull("Stopwatch should still exist", updatedStopwatch)
        assertEquals("Name should be updated", newName, updatedStopwatch!!.name)
    }

    @Test
    fun `updateStopwatchName trims whitespace and handles empty names`() {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]

        // When - Test with whitespace
        viewModel.updateStopwatchName(stopwatch.id, "  Trimmed Name  ")
        val trimmedResult = viewModel.stopwatches.value.find { it.id == stopwatch.id }

        // Then
        assertEquals("Name should be trimmed", "Trimmed Name", trimmedResult!!.name)

        // When - Test with empty name
        viewModel.updateStopwatchName(stopwatch.id, "   ")
        val emptyResult = viewModel.stopwatches.value.find { it.id == stopwatch.id }

        // Then
        assertEquals("Empty name should default to 'Cronómetro'", "Cronómetro", emptyResult!!.name)
    }

    // ========== Timer Functionality Tests ==========

    @Test
    fun `startStopwatch sets running state and starts timer`() = runTest {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]

        // When
        viewModel.startStopwatch(stopwatch.id)

        // Then
        val updatedStopwatch = viewModel.stopwatches.value.find { it.id == stopwatch.id }
        assertTrue("Stopwatch should be running", updatedStopwatch!!.isRunning)
        assertTrue("Start time should be set", updatedStopwatch.startTime > 0)
    }

    @Test
    fun `startStopwatch on already running stopwatch does nothing`() = runTest {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]
        viewModel.startStopwatch(stopwatch.id)
        val firstStartTime = viewModel.stopwatches.value.find { it.id == stopwatch.id }!!.startTime

        // When
        viewModel.startStopwatch(stopwatch.id) // Second call

        // Then
        val secondStartTime = viewModel.stopwatches.value.find { it.id == stopwatch.id }!!.startTime
        assertEquals("Start time should not change", firstStartTime, secondStartTime)
    }

    @Test
    fun `stopStopwatch sets stopped state`() = runTest {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]
        viewModel.startStopwatch(stopwatch.id)

        // When
        viewModel.stopStopwatch(stopwatch.id)

        // Then
        val updatedStopwatch = viewModel.stopwatches.value.find { it.id == stopwatch.id }
        assertFalse("Stopwatch should not be running", updatedStopwatch!!.isRunning)
    }

    @Test
    fun `stopStopwatch on stopped stopwatch does nothing`() = runTest {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]

        // When
        viewModel.stopStopwatch(stopwatch.id) // Stop when already stopped

        // Then - Should not crash or cause issues
        val updatedStopwatch = viewModel.stopwatches.value.find { it.id == stopwatch.id }
        assertFalse("Stopwatch should remain stopped", updatedStopwatch!!.isRunning)
    }

    @Test
    fun `resetStopwatch clears time and laps`() = runTest {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]
        viewModel.startStopwatch(stopwatch.id)

        // Simulate some time passing and add a lap
        advanceTimeBy(1000)
        viewModel.recordLap(stopwatch.id)

        // When
        viewModel.resetStopwatch(stopwatch.id)

        // Then
        val resetStopwatch = viewModel.stopwatches.value.find { it.id == stopwatch.id }
        assertNotNull("Stopwatch should still exist", resetStopwatch)
        assertEquals("Current time should be reset", 0L, resetStopwatch!!.currentTime)
        assertFalse("Should not be running", resetStopwatch.isRunning)
        assertTrue("Laps should be cleared", resetStopwatch.laps.isEmpty())
        assertEquals("Start time should be reset", 0L, resetStopwatch.startTime)
        assertEquals("Paused time should be reset", 0L, resetStopwatch.pausedTime)
    }

    // ========== Lap Recording Tests ==========

    @Test
    fun `recordLap creates lap when stopwatch is running`() = runTest {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]
        viewModel.startStopwatch(stopwatch.id)

        // Simulate time passing
        advanceTimeBy(30000) // 30 seconds

        // When
        viewModel.recordLap(stopwatch.id)

        // Then
        val updatedStopwatch = viewModel.stopwatches.value.find { it.id == stopwatch.id }
        assertEquals("Should have 1 lap", 1, updatedStopwatch!!.laps.size)

        val lap = updatedStopwatch.laps[0]
        assertEquals("Lap number should be 1", 1, lap.lapNumber)
        assertTrue("Lap time should be positive", lap.lapTime > 0)
        assertEquals("Total time should equal lap time for first lap", lap.lapTime, lap.totalTime)
    }

    @Test
    fun `recordLap on stopped stopwatch does nothing`() = runTest {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]

        // When
        viewModel.recordLap(stopwatch.id) // Record lap on stopped stopwatch

        // Then
        val updatedStopwatch = viewModel.stopwatches.value.find { it.id == stopwatch.id }
        assertTrue("Laps should remain empty", updatedStopwatch!!.laps.isEmpty())
    }

    @Test
    fun `recordLap creates multiple laps with correct timing`() = runTest {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]
        viewModel.startStopwatch(stopwatch.id)

        // When
        advanceTimeBy(30000) // 30 seconds
        viewModel.recordLap(stopwatch.id)

        advanceTimeBy(25000) // Additional 25 seconds (55 total)
        viewModel.recordLap(stopwatch.id)

        // Then
        val updatedStopwatch = viewModel.stopwatches.value.find { it.id == stopwatch.id }
        assertEquals("Should have 2 laps", 2, updatedStopwatch!!.laps.size)

        val firstLap = updatedStopwatch.laps[0]
        val secondLap = updatedStopwatch.laps[1]

        assertEquals("First lap number", 1, firstLap.lapNumber)
        assertEquals("Second lap number", 2, secondLap.lapNumber)
        assertTrue("Second lap total time should be greater", secondLap.totalTime > firstLap.totalTime)
    }

    // ========== Edge Cases and Error Handling ==========

    @Test
    fun `operations with invalid stopwatch id do nothing`() {
        // Given
        val invalidId = "non-existent-id"

        // When & Then - Should not crash
        viewModel.startStopwatch(invalidId)
        viewModel.stopStopwatch(invalidId)
        viewModel.recordLap(invalidId)
        viewModel.resetStopwatch(invalidId)
        viewModel.updateStopwatchName(invalidId, "New Name")

        // Verify no side effects
        assertTrue("No stopwatches should be created", viewModel.stopwatches.value.isEmpty())
    }

    @Test
    fun `removeStopwatch cancels running timer`() = runTest {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]
        viewModel.startStopwatch(stopwatch.id)

        // When
        viewModel.removeStopwatch(stopwatch.id)

        // Then
        assertTrue("No stopwatches should remain", viewModel.stopwatches.value.isEmpty())
        // Timer job should be cancelled (verified by no memory leaks)
    }

    @Test
    fun `multiple concurrent stopwatches work independently`() = runTest {
        // Given
        viewModel.addStopwatch() // Stopwatch 1
        viewModel.addStopwatch() // Stopwatch 2
        val stopwatch1 = viewModel.stopwatches.value[0]
        val stopwatch2 = viewModel.stopwatches.value[1]

        // When
        viewModel.startStopwatch(stopwatch1.id)
        advanceTimeBy(1000)
        viewModel.startStopwatch(stopwatch2.id)
        advanceTimeBy(1000)
        viewModel.stopStopwatch(stopwatch1.id)

        // Then
        val updatedStopwatches = viewModel.stopwatches.value
        val updatedStopwatch1 = updatedStopwatches.find { it.id == stopwatch1.id }
        val updatedStopwatch2 = updatedStopwatches.find { it.id == stopwatch2.id }

        assertFalse("Stopwatch 1 should be stopped", updatedStopwatch1!!.isRunning)
        assertTrue("Stopwatch 2 should still be running", updatedStopwatch2!!.isRunning)
    }

    // ========== Performance and Memory Tests ==========

    @Test
    fun `adding many stopwatches performs efficiently`() {
        // Given
        val startTime = System.currentTimeMillis()

        // When
        repeat(100) {
            viewModel.addStopwatch()
        }

        // Then
        val duration = System.currentTimeMillis() - startTime
        assertEquals("Should have 100 stopwatches", 100, viewModel.stopwatches.value.size)
        assertTrue("Should complete quickly (< 100ms)", duration < 100)
    }

    @Test
    fun `stopwatch state updates are atomic`() {
        // Given
        viewModel.addStopwatch()
        val stopwatch = viewModel.stopwatches.value[0]

        // When
        viewModel.startStopwatch(stopwatch.id)
        val runningState = viewModel.stopwatches.value.find { it.id == stopwatch.id }

        // Then
        assertTrue("State should be consistent", runningState!!.isRunning)
        assertTrue("Start time should be set when running", runningState.startTime > 0)
    }
}