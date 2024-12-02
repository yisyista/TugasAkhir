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

    LaunchedEffect(selectedRange) {
        viewModel.loadAnxietyData(selectedRange)  // Refresh data when range changes
    }

    val xAxisScaleData = when (selectedRange) {
        "Hour" -> averageAnxietyData.map {
            Log.d("AnxietyGraph", "Average Anxiety Data: $averageAnxietyData")
            Log.d("AnxietyGraph", "Hour: ${it.hour}")  // Ganti timestamp dengan hour
            it.hour.toString()  // Gunakan hour sebagai label
        }
        "Day" -> averageAnxietyData.map {
            Log.d("AnxietyGraph", "Week: ${it.day}")
            it.day.toString()  // Gunakan week sebagai label
        }
        "Week" -> averageAnxietyData.map {
            Log.d("AnxietyGraph", "Month: ${it.week}")
            it.week.toString()  // Gunakan month sebagai label
        }
        "Month" -> averageAnxietyData.map {
            Log.d("AnxietyGraph", "Month: ${it.month}")
            it.month.toString()  // Gunakan month untuk year (untuk data yang ada di bulan Desember)
        }
        else -> emptyList()
    }



    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Tombol untuk memilih rentang waktu
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            rangeOptions.forEach { range ->
                RangeSelectorButton(
                    text = range,
                    isSelected = range == selectedRange,
                    onClick = {
                        selectedRange = range
                        viewModel.loadAnxietyData(range)  // Reload data when range is selected
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menampilkan Bar Graph jika data tersedia
        if (averageAnxietyData.isNotEmpty()) {
            BarGraph(
                graphBarData = averageAnxietyData.map { it.avgTingkatAnxiety },
                xAxisScaleData = xAxisScaleData,  // Use modified x-axis labels
                barData_ = averageAnxietyData.map { it.avgTingkatAnxiety.toFloat() },
                height = 300.dp,
                roundType = BarType.TOP_CURVED,
                barWidth = 20.dp,
                barColor = Purple500,
                barArrangement = Arrangement.SpaceEvenly
            )
        } else {
            // Menampilkan pesan jika data kosong
            Text("Data not available for the selected range", style = MaterialTheme.typography.bodyLarge)
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
