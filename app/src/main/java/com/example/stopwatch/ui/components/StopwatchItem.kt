package com.example.stopwatch.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stopwatch.R
import com.example.stopwatch.model.Stopwatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchItem(
    stopwatch: Stopwatch,
    isActive: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
    onRemove: () -> Unit,
    onNameChange: (String) -> Unit,
    onSetActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSetActive() }
            .then(
                if (isActive) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 8.dp else 4.dp),
        colors = if (isActive) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var isEditing by remember { mutableStateOf(false) }
                var nameText by remember { mutableStateOf(stopwatch.name) }
                val focusRequester = remember { FocusRequester() }

                // Sync the text field when stopwatch name changes externally
                LaunchedEffect(stopwatch.name) {
                    nameText = stopwatch.name
                }

                // Auto-focus when entering edit mode
                LaunchedEffect(isEditing) {
                    if (isEditing) {
                        focusRequester.requestFocus()
                    }
                }

                if (isEditing) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && isEditing) {
                                    // Lost focus - save and exit edit mode
                                    onNameChange(nameText)
                                    isEditing = false
                                }
                            },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onNameChange(nameText)
                                isEditing = false
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    Text(
                        text = stopwatch.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isEditing = true }
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Cronómetro activo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stopwatch.formattedTime,
                fontSize = 48.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (stopwatch.isRunning)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )

            if (stopwatch.isRunning || stopwatch.laps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Vuelta: ${stopwatch.formattedCurrentLapTime}",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (stopwatch.isRunning) {
                    Button(
                        onClick = onStop,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.stop))
                    }

                    Button(
                        onClick = onLap,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.lap))
                    }
                } else {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.start))
                    }

                    Button(
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(stringResource(R.string.reset))
                    }
                }
            }

            if (stopwatch.laps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(stopwatch.laps.reversed()) { lap ->
                        LapItem(lap = lap)
                    }
                }
            }
        }
    }
}