package com.example.tugasakhir

import android.os.Bundle
import android.util.Log
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
import com.example.tugasakhir.ui.theme.TugasAkhirTheme
import com.example.tugasakhir.ui.theme.Purple500
import java.util.Calendar  // Untuk bekerja dengan kalender dan mengambil informasi waktu dari timestamp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import java.util.Date
import java.util.Locale

class AnxietyGraphActivity : ComponentActivity() {
    private val viewModel: AnxietyLogViewModel by viewModels {
        AnxietyLogViewModelFactory(AppDatabase.getDatabase(applicationContext).dataAccessObject())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TugasAkhirTheme {
                Surface {
                    AnxietyGraphScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnxietyGraphScreen(viewModel: AnxietyLogViewModel) {
    val averageAnxietyData by viewModel.averageAnxietyData.collectAsState()
    val rangeOptions = listOf("Hour", "Day", "Week", "Month")
    var selectedRange by remember { mutableStateOf("Hour") }
    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var selectedMonthYear by remember { mutableStateOf(calendar.time) }

    // Refresh data saat range atau tanggal berubah
    LaunchedEffect(selectedRange, selectedDate, selectedMonthYear) {
        when (selectedRange) {
            "Hour" -> viewModel.loadAnxietyData(selectedRange, selectedDate)
            "Week" -> viewModel.loadAnxietyData(selectedRange, selectedMonthYear)
            else -> viewModel.loadAnxietyData(selectedRange)
        }
    }

    // Data untuk X-Axis
    val xAxisScaleData = when (selectedRange) {
        "Hour" -> averageAnxietyData.map { it.hour.toString() }
        "Day" -> averageAnxietyData.map { it.day.toString() }
        "Week" -> averageAnxietyData.map { it.week.toString() }
        "Month" -> averageAnxietyData.map { it.month.toString() }
        else -> emptyList()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Tombol untuk memilih rentang waktu
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            rangeOptions.forEach { range ->
                RangeSelectorButton(
                    text = range,
                    isSelected = range == selectedRange,
                    onClick = { selectedRange = range }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Kontrol tambahan untuk Hour dan Week
        if (selectedRange == "Hour") {
            DateNavigation(
                date = selectedDate,
                onPrevious = {
                    calendar.time = selectedDate
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    selectedDate = calendar.time
                },
                onNext = {
                    calendar.time = selectedDate
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    selectedDate = calendar.time
                }
            )
        } else if (selectedRange == "Week") {
            DateNavigation(
                date = selectedMonthYear,
                onPrevious = {
                    calendar.time = selectedMonthYear
                    calendar.add(Calendar.MONTH, -1)
                    selectedMonthYear = calendar.time
                },
                onNext = {
                    calendar.time = selectedMonthYear
                    calendar.add(Calendar.MONTH, 1)
                    selectedMonthYear = calendar.time
                },
                format = "MMMM yyyy" // Format bulan dan tahun
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menampilkan Bar Graph jika data tersedia
        if (averageAnxietyData.isNotEmpty()) {
            BarGraph(
                graphBarData = averageAnxietyData.map { it.avgTingkatAnxiety },
                xAxisScaleData = xAxisScaleData,
                barData_ = averageAnxietyData.map { it.avgTingkatAnxiety.toFloat() },
                height = 300.dp,
                roundType = BarType.TOP_CURVED,
                barWidth = 20.dp,
                barColor = Purple500,
                barArrangement = Arrangement.SpaceEvenly
            )
        } else {
            Text("Data not available for the selected range", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun DateNavigation(date: Date, onPrevious: () -> Unit, onNext: () -> Unit, format: String = "dd/MM/yyyy") {
    val dateFormat = java.text.SimpleDateFormat(format, Locale.getDefault())
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Previous")
        }
        Text(
            text = dateFormat.format(date),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
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
