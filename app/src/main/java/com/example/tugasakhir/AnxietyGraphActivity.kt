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
    var selectedDateTimestamp by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }

    // Memicu pembaruan data setiap kali rentang atau tanggal berubah
    LaunchedEffect(selectedRange, selectedDateTimestamp) {
        if (selectedRange == "Hour") {
            viewModel.loadAnxietyData(selectedRange, selectedDateTimestamp)
        } else {
            viewModel.loadAnxietyData(selectedRange, null)
        }
    }

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

        // Tombol navigasi tanggal hanya untuk "Hour"
        if (selectedRange == "Hour") {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    // Kurangi 1 hari
                    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateTimestamp }
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    selectedDateTimestamp = calendar.timeInMillis
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous Day"
                    )
                }

                Text(
                    text = android.text.format.DateFormat.format("dd/MM/yyyy", selectedDateTimestamp).toString(),
                    style = MaterialTheme.typography.bodyLarge
                )

                IconButton(onClick = {
                    // Tambah 1 hari
                    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateTimestamp }
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    selectedDateTimestamp = calendar.timeInMillis
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Day"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

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
