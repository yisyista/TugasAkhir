package com.example.tugasakhir

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.tugasakhir.ui.theme.TugasAkhirTheme
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import com.example.tugasakhir.ui.theme.Purple500

class AnxietyGraphActivity : ComponentActivity() {
    private val hrvViewModel: HrvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TugasAkhirTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnxietyGraphScreen(hrvViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnxietyGraphScreen(hrvViewModel: HrvViewModel) {
    var selectedRange by remember { mutableStateOf("Day") }
    var selectedDate by remember { mutableStateOf("2024-11-27") } // Example: Default date
    var dataList by remember { mutableStateOf(listOf<Float>()) } // Example: List of anxiety data

    val context = LocalContext.current

    // Update data based on selected range
    LaunchedEffect(selectedRange) {
        // Simulate data fetching, replace with actual data fetching logic
        dataList = fetchDataForRange(selectedRange)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anxiety Log") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back button */ }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Range Selector (Day, Week, Month, Year)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RangeSelectorButton(text = "Day", isSelected = selectedRange == "Day") { selectedRange = "Day" }
                RangeSelectorButton(text = "Week", isSelected = selectedRange == "Week") { selectedRange = "Week" }
                RangeSelectorButton(text = "Month", isSelected = selectedRange == "Month") { selectedRange = "Month" }
                RangeSelectorButton(text = "Year", isSelected = selectedRange == "Year") { selectedRange = "Year" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Date and Navigation for Day
            if (selectedRange == "Day") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { selectedDate = previousDay(selectedDate) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Previous Day")
                    }
                    Text(text = "Date: $selectedDate")
                    IconButton(onClick = { selectedDate = nextDay(selectedDate) }) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Next Day")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Bar Graph for Selected Range
            if (dataList.isNotEmpty()) {
                BarGraph(
                    graphBarData = dataList,
                    barData_ = listOf(1L, 2L, 3L, 4L, 5L),
                    xAxisScaleData = (1..dataList.size).toList(),
                    height = 300.dp,
                    roundType = BarType.TOP_CURVED,
                    barArrangement = Arrangement.SpaceEvenly,
                    barColor = Purple500,
                    barWidth = 20.dp
                )
            } else {
                Text(text = "No data available", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun RangeSelectorButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text = text)
    }
}

fun fetchDataForRange(range: String): List<Float> {
    return when (range) {
        "Day" -> listOf(0.2f, 0.4f, 0.3f, 0.5f, 0.7f) // Example: Replace with actual logic
        "Week" -> listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.8f)
        "Month" -> listOf(0.4f, 0.5f, 0.6f, 0.7f)
        "Year" -> listOf(0.5f, 0.7f, 0.8f, 0.9f)
        else -> emptyList()
    }
}

fun previousDay(currentDate: String): String {
    // Logic to calculate previous day
    return "2024-11-26"
}

fun nextDay(currentDate: String): String {
    // Logic to calculate next day
    return "2024-11-28"
}
