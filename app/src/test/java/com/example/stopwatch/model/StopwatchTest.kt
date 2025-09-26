package com.example.stopwatch.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the [Stopwatch] data class.
 *
 * These tests verify the correct behavior of the Stopwatch model,
 * including time formatting, property initialization, and immutability.
 *
 * @author Sergio Octavio Mancebo
 * @since 1.0
 */
class StopwatchTest {

    @Test
    fun `stopwatch created with default values`() {
        // Given & When
        val stopwatch = Stopwatch()

        // Then
        assertNotNull("ID should not be null", stopwatch.id)
        assertTrue("ID should not be empty", stopwatch.id.isNotEmpty())
        assertEquals("Default name should be 'Cronómetro'", "Cronómetro", stopwatch.name)
        assertEquals("Current time should be 0", 0L, stopwatch.currentTime)
        assertFalse("Should not be running initially", stopwatch.isRunning)
        assertTrue("Laps list should be empty", stopwatch.laps.isEmpty())
        assertEquals("Start time should be 0", 0L, stopwatch.startTime)
        assertEquals("Paused time should be 0", 0L, stopwatch.pausedTime)
    }

    @Test
    fun `stopwatch created with custom values`() {
        // Given
        val customLaps = listOf(
            Lap(lapNumber = 1, lapTime = 30000, totalTime = 30000)
        )

        // When
        val stopwatch = Stopwatch(
            id = "test-id",
            name = "Test Stopwatch",
            currentTime = 60000L,
            isRunning = true,
            laps = customLaps,
            startTime = System.currentTimeMillis(),
            pausedTime = 5000L
        )

        // Then
        assertEquals("Custom ID should be set", "test-id", stopwatch.id)
        assertEquals("Custom name should be set", "Test Stopwatch", stopwatch.name)
        assertEquals("Current time should match", 60000L, stopwatch.currentTime)
        assertTrue("Should be running", stopwatch.isRunning)
        assertEquals("Laps should match", customLaps, stopwatch.laps)
        assertEquals("Paused time should match", 5000L, stopwatch.pausedTime)
    }

    @Test
    fun `formatTime returns correct format for zero time`() {
        // Given
        val timeInMillis = 0L

        // When
        val formatted = Stopwatch.formatTime(timeInMillis)

        // Then
        assertEquals("Zero time should format as 00:00.00", "00:00.00", formatted)
    }

    @Test
    fun `formatTime returns correct format for seconds only`() {
        // Given
        val timeInMillis = 45000L // 45 seconds

        // When
        val formatted = Stopwatch.formatTime(timeInMillis)

        // Then
        assertEquals("45 seconds should format as 00:45.00", "00:45.00", formatted)
    }

    @Test
    fun `formatTime returns correct format for minutes and seconds`() {
        // Given
        val timeInMillis = 125000L // 2 minutes 5 seconds

        // When
        val formatted = Stopwatch.formatTime(timeInMillis)

        // Then
        assertEquals("2:05 should format as 02:05.00", "02:05.00", formatted)
    }

    @Test
    fun `formatTime returns correct format with milliseconds`() {
        // Given
        val timeInMillis = 63450L // 1 minute 3.45 seconds

        // When
        val formatted = Stopwatch.formatTime(timeInMillis)

        // Then
        assertEquals("1:03.45 should format correctly", "01:03.45", formatted)
    }

    @Test
    fun `formatTime handles large time values`() {
        // Given
        val timeInMillis = 3661000L // 61 minutes 1 second

        // When
        val formatted = Stopwatch.formatTime(timeInMillis)

        // Then
        assertEquals("Large time should format correctly", "61:01.00", formatted)
    }

    @Test
    fun `formatTime handles edge cases with milliseconds`() {
        // Given
        val timeInMillis = 1999L // 1.999 seconds (should round down to 1.99)

        // When
        val formatted = Stopwatch.formatTime(timeInMillis)

        // Then
        assertEquals("Edge case milliseconds should format correctly", "00:01.99", formatted)
    }

    @Test
    fun `formattedTime property uses formatTime function`() {
        // Given
        val stopwatch = Stopwatch(currentTime = 78650L) // 1:18.65

        // When
        val formatted = stopwatch.formattedTime

        // Then
        assertEquals("Formatted time property should match formatTime result", "01:18.65", formatted)
    }

    @Test
    fun `stopwatch is immutable - copy creates new instance`() {
        // Given
        val original = Stopwatch(name = "Original", currentTime = 1000L)

        // When
        val modified = original.copy(name = "Modified", currentTime = 2000L)

        // Then
        assertNotSame("Copy should create new instance", original, modified)
        assertEquals("Original should be unchanged", "Original", original.name)
        assertEquals("Original time should be unchanged", 1000L, original.currentTime)
        assertEquals("Modified should have new name", "Modified", modified.name)
        assertEquals("Modified should have new time", 2000L, modified.currentTime)
    }

    @Test
    fun `unique IDs are generated for different instances`() {
        // Given & When
        val stopwatch1 = Stopwatch()
        val stopwatch2 = Stopwatch()

        // Then
        assertNotEquals("Different instances should have different IDs", stopwatch1.id, stopwatch2.id)
    }

    @Test
    fun `formatTime performance test`() {
        // Given
        val iterations = 10000
        val testTime = 123456L

        // When
        val startTime = System.currentTimeMillis()
        repeat(iterations) {
            Stopwatch.formatTime(testTime)
        }
        val endTime = System.currentTimeMillis()

        // Then
        val duration = endTime - startTime
        assertTrue("Format time should be efficient (< 1000ms for 10k operations)", duration < 1000)
    }
}