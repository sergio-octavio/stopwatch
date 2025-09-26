package com.example.stopwatch.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the [Lap] data class.
 *
 * These tests verify the correct behavior of the Lap model,
 * including property initialization, immutability, and data integrity.
 *
 * @author Sergio Octavio Mancebo
 * @since 1.0
 */
class LapTest {

    @Test
    fun `lap created with correct properties`() {
        // Given
        val lapNumber = 3
        val lapTime = 25000L // 25 seconds
        val totalTime = 75000L // 1 minute 15 seconds

        // When
        val lap = Lap(
            lapNumber = lapNumber,
            lapTime = lapTime,
            totalTime = totalTime
        )

        // Then
        assertEquals("Lap number should match", lapNumber, lap.lapNumber)
        assertEquals("Lap time should match", lapTime, lap.lapTime)
        assertEquals("Total time should match", totalTime, lap.totalTime)
    }

    @Test
    fun `first lap has correct timing relationship`() {
        // Given
        val lapTime = 30000L // 30 seconds
        val totalTime = 30000L // Same as lap time for first lap

        // When
        val firstLap = Lap(
            lapNumber = 1,
            lapTime = lapTime,
            totalTime = totalTime
        )

        // Then
        assertEquals("First lap: lap time should equal total time", lapTime, firstLap.totalTime)
        assertEquals("Lap number should be 1", 1, firstLap.lapNumber)
    }

    @Test
    fun `subsequent lap has correct timing relationship`() {
        // Given
        val previousTotalTime = 30000L // Previous lap ended at 30s
        val currentLapTime = 25000L // This lap took 25s
        val currentTotalTime = 55000L // Total is now 55s

        // When
        val secondLap = Lap(
            lapNumber = 2,
            lapTime = currentLapTime,
            totalTime = currentTotalTime
        )

        // Then
        assertEquals("Lap time should be individual segment time", currentLapTime, secondLap.lapTime)
        assertEquals("Total time should be cumulative", currentTotalTime, secondLap.totalTime)
        assertEquals("Total should equal previous total + lap time",
            previousTotalTime + currentLapTime, secondLap.totalTime)
    }

    @Test
    fun `lap with zero times`() {
        // Given & When
        val lap = Lap(
            lapNumber = 1,
            lapTime = 0L,
            totalTime = 0L
        )

        // Then
        assertEquals("Lap time can be zero", 0L, lap.lapTime)
        assertEquals("Total time can be zero", 0L, lap.totalTime)
        assertEquals("Lap number should be set", 1, lap.lapNumber)
    }

    @Test
    fun `lap with large time values`() {
        // Given
        val lapNumber = 10
        val lapTime = 3600000L // 1 hour
        val totalTime = 36000000L // 10 hours

        // When
        val lap = Lap(
            lapNumber = lapNumber,
            lapTime = lapTime,
            totalTime = totalTime
        )

        // Then
        assertEquals("Should handle large lap times", lapTime, lap.lapTime)
        assertEquals("Should handle large total times", totalTime, lap.totalTime)
        assertEquals("Should handle large lap numbers", lapNumber, lap.lapNumber)
    }

    @Test
    fun `lap is immutable - copy creates new instance`() {
        // Given
        val original = Lap(
            lapNumber = 1,
            lapTime = 30000L,
            totalTime = 30000L
        )

        // When
        val modified = original.copy(
            lapNumber = 2,
            lapTime = 25000L,
            totalTime = 55000L
        )

        // Then
        assertNotSame("Copy should create new instance", original, modified)
        assertEquals("Original lap number unchanged", 1, original.lapNumber)
        assertEquals("Original lap time unchanged", 30000L, original.lapTime)
        assertEquals("Original total time unchanged", 30000L, original.totalTime)
        assertEquals("Modified has new lap number", 2, modified.lapNumber)
        assertEquals("Modified has new lap time", 25000L, modified.lapTime)
        assertEquals("Modified has new total time", 55000L, modified.totalTime)
    }

    @Test
    fun `lap equality works correctly`() {
        // Given
        val lap1 = Lap(lapNumber = 1, lapTime = 30000L, totalTime = 30000L)
        val lap2 = Lap(lapNumber = 1, lapTime = 30000L, totalTime = 30000L)
        val lap3 = Lap(lapNumber = 2, lapTime = 30000L, totalTime = 60000L)

        // Then
        assertEquals("Identical laps should be equal", lap1, lap2)
        assertNotEquals("Different laps should not be equal", lap1, lap3)
        assertEquals("Equal laps should have same hash code", lap1.hashCode(), lap2.hashCode())
    }

    @Test
    fun `lap toString provides useful information`() {
        // Given
        val lap = Lap(lapNumber = 3, lapTime = 45000L, totalTime = 135000L)

        // When
        val toString = lap.toString()

        // Then
        assertTrue("toString should contain lap number", toString.contains("3"))
        assertTrue("toString should contain lap time", toString.contains("45000"))
        assertTrue("toString should contain total time", toString.contains("135000"))
    }

    @Test
    fun `lap creation with negative values`() {
        // Given & When
        val lap = Lap(
            lapNumber = -1,
            lapTime = -1000L,
            totalTime = -2000L
        )

        // Then
        // Data class allows negative values - validation should be at business logic level
        assertEquals("Negative lap number accepted", -1, lap.lapNumber)
        assertEquals("Negative lap time accepted", -1000L, lap.lapTime)
        assertEquals("Negative total time accepted", -2000L, lap.totalTime)
    }

    @Test
    fun `lap timing consistency validation example`() {
        // Given
        val lap1 = Lap(lapNumber = 1, lapTime = 30000L, totalTime = 30000L)
        val lap2 = Lap(lapNumber = 2, lapTime = 25000L, totalTime = 55000L)
        val lap3 = Lap(lapNumber = 3, lapTime = 20000L, totalTime = 75000L)

        // When - Simulating a series of laps
        val laps = listOf(lap1, lap2, lap3)

        // Then - Verify timing consistency
        assertEquals("First lap total equals lap time", lap1.lapTime, lap1.totalTime)
        assertEquals("Second lap total is cumulative",
            lap1.totalTime + lap2.lapTime, lap2.totalTime)
        assertEquals("Third lap total is cumulative",
            lap2.totalTime + lap3.lapTime, lap3.totalTime)

        // Verify sequence
        for (i in laps.indices) {
            assertEquals("Lap numbers should be sequential", i + 1, laps[i].lapNumber)
        }
    }
}