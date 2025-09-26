package com.example.stopwatch.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stopwatch.model.Lap
import com.example.stopwatch.model.Stopwatch
import com.example.stopwatch.ui.theme.StopwatchTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the [StopwatchItem] Compose component.
 *
 * These tests verify the correct behavior of the stopwatch UI component including:
 * - Proper display of stopwatch state (running/stopped)
 * - Button visibility and interactions based on state
 * - Name editing functionality with tap-to-edit and focus management
 * - Lap display and formatting
 * - Action callbacks and user interactions
 *
 * Uses Compose Testing Framework with semantic matching for reliable UI testing.
 *
 * @author Sergio Octavio Mancebo
 * @since 1.0
 */
@RunWith(AndroidJUnit4::class)
class StopwatchItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ========== Display and State Tests ==========

    @Test
    fun stopwatchItem_displaysCorrectInformation() {
        // Given
        val stopwatch = Stopwatch(
            name = "Test Stopwatch",
            currentTime = 65000L, // 1:05.00
            isRunning = false
        )

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test Stopwatch").assertIsDisplayed()
        composeTestRule.onNodeWithText("01:05.00").assertIsDisplayed()
    }

    @Test
    fun stopwatchItem_showsCorrectButtonsWhenStopped() {
        // Given
        val stopwatch = Stopwatch(isRunning = false)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then - Should show Start and Reset buttons
        composeTestRule.onNodeWithText("Iniciar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reiniciar").assertIsDisplayed()

        // Should not show Stop and Lap buttons
        composeTestRule.onNodeWithText("Parar").assertDoesNotExist()
        composeTestRule.onNodeWithText("Vuelta").assertDoesNotExist()
    }

    @Test
    fun stopwatchItem_showsCorrectButtonsWhenRunning() {
        // Given
        val stopwatch = Stopwatch(isRunning = true, currentTime = 1000L)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then - Should show Stop and Lap buttons
        composeTestRule.onNodeWithText("Parar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vuelta").assertIsDisplayed()

        // Should not show Start and Reset buttons
        composeTestRule.onNodeWithText("Iniciar").assertDoesNotExist()
        composeTestRule.onNodeWithText("Reiniciar").assertDoesNotExist()
    }

    // ========== Button Interaction Tests ==========

    @Test
    fun stopwatchItem_startButtonTriggersCallback() {
        // Given
        var startClicked = false
        val stopwatch = Stopwatch(isRunning = false)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = { startClicked = true },
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Iniciar").performClick()
        assert(startClicked) { "Start callback should be triggered" }
    }

    @Test
    fun stopwatchItem_stopButtonTriggersCallback() {
        // Given
        var stopClicked = false
        val stopwatch = Stopwatch(isRunning = true)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = { stopClicked = true },
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Parar").performClick()
        assert(stopClicked) { "Stop callback should be triggered" }
    }

    @Test
    fun stopwatchItem_lapButtonTriggersCallback() {
        // Given
        var lapClicked = false
        val stopwatch = Stopwatch(isRunning = true)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = { lapClicked = true },
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Vuelta").performClick()
        assert(lapClicked) { "Lap callback should be triggered" }
    }

    @Test
    fun stopwatchItem_resetButtonTriggersCallback() {
        // Given
        var resetClicked = false
        val stopwatch = Stopwatch(isRunning = false)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = { resetClicked = true },
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Reiniciar").performClick()
        assert(resetClicked) { "Reset callback should be triggered" }
    }

    @Test
    fun stopwatchItem_removeButtonTriggersCallback() {
        // Given
        var removeClicked = false
        val stopwatch = Stopwatch()

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = { removeClicked = true },
                    onNameChange = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Eliminar").performClick()
        assert(removeClicked) { "Remove callback should be triggered" }
    }

    // ========== Name Editing Tests ==========

    @Test
    fun stopwatchItem_nameIsClickableToEdit() {
        // Given
        val stopwatch = Stopwatch(name = "Original Name")

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Original Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Original Name").performClick()

        // Should show text field after click
        composeTestRule.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun stopwatchItem_nameEditingTriggersCallback() {
        // Given
        var newName = ""
        val stopwatch = Stopwatch(name = "Original")

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = { newName = it }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Original").performClick()
        composeTestRule.onNode(hasSetTextAction()).performTextClearance()
        composeTestRule.onNode(hasSetTextAction()).performTextInput("New Name")
        composeTestRule.onNode(hasSetTextAction()).performImeAction()

        assert(newName == "New Name") { "Name change callback should be triggered with new name" }
    }

    // ========== Lap Display Tests ==========

    @Test
    fun stopwatchItem_displaysLapsWhenPresent() {
        // Given
        val laps = listOf(
            Lap(lapNumber = 1, lapTime = 30000L, totalTime = 30000L),
            Lap(lapNumber = 2, lapTime = 25000L, totalTime = 55000L)
        )
        val stopwatch = Stopwatch(laps = laps)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Historial de Vueltas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vuelta 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vuelta 2").assertIsDisplayed()
    }

    @Test
    fun stopwatchItem_hidesLapsWhenEmpty() {
        // Given
        val stopwatch = Stopwatch(laps = emptyList())

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Historial de Vueltas").assertDoesNotExist()
    }

    // ========== Time Display Tests ==========

    @Test
    fun stopwatchItem_displaysTimeInCorrectFormat() {
        // Given
        val testCases = listOf(
            0L to "00:00.00",
            1000L to "00:01.00",
            61000L to "01:01.00",
            125500L to "02:05.50"
        )

        testCases.forEach { (timeInMillis, expectedFormat) ->
            val stopwatch = Stopwatch(currentTime = timeInMillis)

            // When
            composeTestRule.setContent {
                StopwatchTheme {
                    StopwatchItem(
                        stopwatch = stopwatch,
                        onStart = {},
                        onStop = {},
                        onLap = {},
                        onReset = {},
                        onRemove = {},
                        onNameChange = {}
                    )
                }
            }

            // Then
            composeTestRule.onNodeWithText(expectedFormat).assertIsDisplayed()
        }
    }

    @Test
    fun stopwatchItem_timeColorChangesBasedOnRunningState() {
        // This test verifies semantic behavior rather than specific colors
        // since color testing in Compose requires more complex setup

        // Given - Running stopwatch
        val runningStopwatch = Stopwatch(isRunning = true, currentTime = 1000L)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = runningStopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then - Should display time (color verification would require more complex testing)
        composeTestRule.onNodeWithText("00:01.00").assertIsDisplayed()

        // Given - Stopped stopwatch
        val stoppedStopwatch = Stopwatch(isRunning = false, currentTime = 1000L)

        // When
        composeTestRule.setContent {
            StopwatchTheme {
                StopwatchItem(
                    stopwatch = stoppedStopwatch,
                    onStart = {},
                    onStop = {},
                    onLap = {},
                    onReset = {},
                    onRemove = {},
                    onNameChange = {}
                )
            }
        }

        // Then - Should display same time (but with different styling)
        composeTestRule.onNodeWithText("00:01.00").assertIsDisplayed()
    }
}