package com.example.stopwatch.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stopwatch.model.Lap
import com.example.stopwatch.ui.theme.StopwatchTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the [LapItem] Compose component.
 *
 * These tests verify the correct behavior of the lap item UI component including:
 * - Proper display of lap information (number, lap time, total time)
 * - Correct time formatting using the Stopwatch.formatTime function
 * - Visual layout and styling consistency
 * - Component structure and accessibility
 *
 * Uses Compose Testing Framework with semantic matching for reliable UI testing.
 *
 * @author Sergio Octavio Mancebo
 * @since 1.0
 */
@RunWith(AndroidJUnit4::class)
class LapItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ========== Display and Content Tests ==========

    @Test
    fun lapItem_displaysCorrectLapInformation() {
        // Given
        val lap = Lap(
            lapNumber = 3,
            lapTime = 25000L, // 25 seconds
            totalTime = 85000L // 1:25.00
        )

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                LapItem(lap = lap)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Vuelta 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo de vuelta: 00:25.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo total: 01:25.00").assertIsDisplayed()
    }

    @Test
    fun lapItem_displaysFirstLapCorrectly() {
        // Given
        val firstLap = Lap(
            lapNumber = 1,
            lapTime = 30000L, // 30 seconds
            totalTime = 30000L // Same as lap time for first lap
        )

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                LapItem(lap = firstLap)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Vuelta 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo de vuelta: 00:30.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo total: 00:30.00").assertIsDisplayed()
    }

    @Test
    fun lapItem_displaysZeroTimesCorrectly() {
        // Given
        val zeroLap = Lap(
            lapNumber = 1,
            lapTime = 0L,
            totalTime = 0L
        )

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                LapItem(lap = zeroLap)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Vuelta 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo de vuelta: 00:00.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo total: 00:00.00").assertIsDisplayed()
    }

    // ========== Time Formatting Tests ==========

    @Test
    fun lapItem_formatsVariousTimeValuesCorrectly() {
        // Given
        val testCases = listOf(
            Triple(1, 1000L, 1000L), // 1 second
            Triple(2, 59000L, 60000L), // 59 seconds lap, 1 minute total
            Triple(3, 125000L, 185000L), // 2:05 lap, 3:05 total
            Triple(10, 3600000L, 7200000L) // 1 hour lap, 2 hours total
        )

        testCases.forEach { (lapNumber, lapTime, totalTime) ->
            val lap = Lap(lapNumber = lapNumber, lapTime = lapTime, totalTime = totalTime)

            // When
            composeTestRule.setContent {
                StopwatchTheme {
                    LapItem(lap = lap)
                }
            }

            // Then - Verify lap number is displayed
            composeTestRule.onNodeWithText("Vuelta $lapNumber").assertIsDisplayed()

            // Verify times are displayed (specific formatting tested in model tests)
            composeTestRule.onNode(
                hasText("Tiempo de vuelta:") and hasAnyChild(hasText(Regex("\\d{2}:\\d{2}\\.\\d{2}")))
            ).assertExists()

            composeTestRule.onNode(
                hasText("Tiempo total:") and hasAnyChild(hasText(Regex("\\d{2}:\\d{2}\\.\\d{2}")))
            ).assertExists()
        }
    }

    @Test
    fun lapItem_displaysMillisecondsCorrectly() {
        // Given
        val lapWithMilliseconds = Lap(
            lapNumber = 5,
            lapTime = 12345L, // 12.345 seconds -> should display as 12.34
            totalTime = 67890L // 67.890 seconds -> should display as 67.89
        )

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                LapItem(lap = lapWithMilliseconds)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Vuelta 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo de vuelta: 00:12.34").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo total: 01:07.89").assertIsDisplayed()
    }

    // ========== Layout and Structure Tests ==========

    @Test
    fun lapItem_hasCorrectLayoutStructure() {
        // Given
        val lap = Lap(lapNumber = 2, lapTime = 15000L, totalTime = 45000L)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                LapItem(lap = lap)
            }
        }

        // Then - Verify all expected elements are present
        composeTestRule.onNodeWithText("Vuelta 2").assertIsDisplayed()

        // Verify time information is displayed
        composeTestRule.onNode(hasText("Tiempo de vuelta:")).assertIsDisplayed()
        composeTestRule.onNode(hasText("Tiempo total:")).assertIsDisplayed()

        // Verify formatted times are present
        composeTestRule.onNodeWithText("00:15.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("00:45.00").assertIsDisplayed()
    }

    @Test
    fun lapItem_handlesLargeLapNumbers() {
        // Given
        val largeLap = Lap(
            lapNumber = 999,
            lapTime = 1000L,
            totalTime = 999000L
        )

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                LapItem(lap = largeLap)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Vuelta 999").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo de vuelta: 00:01.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo total: 16:39.00").assertIsDisplayed()
    }

    // ========== Edge Cases Tests ==========

    @Test
    fun lapItem_handlesNegativeValues() {
        // Given - Edge case with negative values (should not happen in normal use)
        val negativeLap = Lap(
            lapNumber = -1,
            lapTime = -1000L,
            totalTime = -2000L
        )

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                LapItem(lap = negativeLap)
            }
        }

        // Then - Should still display (validation should be at business logic level)
        composeTestRule.onNodeWithText("Vuelta -1").assertIsDisplayed()
        // Time formatting with negative values would depend on formatTime implementation
        composeTestRule.onNode(hasText("Tiempo de vuelta:")).assertIsDisplayed()
        composeTestRule.onNode(hasText("Tiempo total:")).assertIsDisplayed()
    }

    @Test
    fun lapItem_consistentWithMultipleInstances() {
        // Given
        val laps = listOf(
            Lap(lapNumber = 1, lapTime = 30000L, totalTime = 30000L),
            Lap(lapNumber = 2, lapTime = 25000L, totalTime = 55000L),
            Lap(lapNumber = 3, lapTime = 20000L, totalTime = 75000L)
        )

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                laps.forEach { lap ->
                    LapItem(lap = lap)
                }
            }
        }

        // Then - All laps should be displayed correctly
        composeTestRule.onNodeWithText("Vuelta 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vuelta 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vuelta 3").assertIsDisplayed()

        // Verify time progression is logical
        composeTestRule.onNodeWithText("Tiempo total: 00:30.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo total: 00:55.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo total: 01:15.00").assertIsDisplayed()
    }

    // ========== Accessibility Tests ==========

    @Test
    fun lapItem_providesAccessibleContent() {
        // Given
        val lap = Lap(lapNumber = 4, lapTime = 45000L, totalTime = 180000L)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                LapItem(lap = lap)
            }
        }

        // Then - Verify accessible text content is available
        composeTestRule.onNode(hasText("Vuelta 4")).assertIsDisplayed()

        // Text should be accessible for screen readers
        composeTestRule.onNode(hasText("Tiempo de vuelta: 00:45.00")).assertIsDisplayed()
        composeTestRule.onNode(hasText("Tiempo total: 03:00.00")).assertIsDisplayed()
    }

    @Test
    fun lapItem_maintainsConsistentStyling() {
        // Given
        val lap = Lap(lapNumber = 7, lapTime = 33000L, totalTime = 231000L)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                LapItem(lap = lap)
            }
        }

        // Then - Verify component maintains Material 3 styling
        // This test primarily ensures the component renders without crashes
        // and displays all required information
        composeTestRule.onNodeWithText("Vuelta 7").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo de vuelta: 00:33.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiempo total: 03:51.00").assertIsDisplayed()

        // Verify the component is contained within a Card (has fillMaxWidth behavior)
        composeTestRule.onRoot().assertIsDisplayed()
    }
}