package com.example.stopwatch.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stopwatch.R
import com.example.stopwatch.ui.components.StopwatchItem
import com.example.stopwatch.viewmodel.StopwatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchApp(
    viewModel: StopwatchViewModel,
    modifier: Modifier = Modifier
) {
    val stopwatches by viewModel.stopwatches
    val activeStopwatchId by viewModel.activeStopwatchId

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { /* Title removed as requested */ },
            actions = {
                val activeStopwatch = stopwatches.find { it.id == activeStopwatchId }

                // Conditionally display Start/Stop and Lap buttons
                if (activeStopwatch != null) {
                    IconButton(
                        onClick = {
                            if (activeStopwatch.isRunning) {
                                viewModel.stopStopwatch(activeStopwatch.id)
                            } else {
                                viewModel.startStopwatch(activeStopwatch.id)
                            }
                        }
                    ) {
                        if (activeStopwatch.isRunning) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pausar"
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Iniciar"
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (activeStopwatch.isRunning) {
                                viewModel.recordLap(activeStopwatch.id)
                            }
                        },
                        enabled = activeStopwatch.isRunning
                    ) {
                        Text(
                            text = "L",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                // Add Stopwatch button
                IconButton(onClick = { viewModel.addStopwatch() }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_stopwatch)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (stopwatches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Presiona + para añadir un cronómetro",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(stopwatches, key = { it.id }) { stopwatch ->
                    StopwatchItem(
                        stopwatch = stopwatch,
                        isActive = stopwatch.id == activeStopwatchId,
                        onStart = { viewModel.startStopwatch(stopwatch.id) },
                        onStop = { viewModel.stopStopwatch(stopwatch.id) },
                        onLap = { viewModel.recordLap(stopwatch.id) },
                        onReset = { viewModel.resetStopwatch(stopwatch.id) },
                        onRemove = { viewModel.removeStopwatch(stopwatch.id) },
                        onNameChange = { newName -> viewModel.updateStopwatchName(stopwatch.id, newName) },
                        onSetActive = { viewModel.setActiveStopwatch(stopwatch.id) }
                    )
                }
            }
        }
    }
}
