package com.example.stopwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stopwatch.ui.StopwatchApp
import com.example.stopwatch.ui.theme.StopwatchTheme
import com.example.stopwatch.viewmodel.StopwatchViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StopwatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StopwatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StopwatchApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}