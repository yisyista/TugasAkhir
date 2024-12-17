package com.example.tugasakhir

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
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
import com.example.tugasakhir.ui.theme.errorContainerLight
import com.example.tugasakhir.ui.theme.errorLight
import com.example.tugasakhir.ui.theme.onErrorContainerLight
import com.example.tugasakhir.ui.theme.onErrorLight
import com.example.tugasakhir.ui.theme.onPrimaryDark
import com.example.tugasakhir.ui.theme.onSurfaceVariantDark
import com.example.tugasakhir.ui.theme.outlineDark
import com.example.tugasakhir.ui.theme.secondaryContainerLight
import com.example.tugasakhir.ui.theme.secondaryLight
import com.example.tugasakhir.ui.theme.surfaceContainerHighestLight
import com.example.tugasakhir.ui.theme.tertiaryContainerDark
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
    var selectedYear by remember { mutableStateOf(calendar.time) }
    var dayRangeText by remember { mutableStateOf(getDayRange(calendar)) }

    // Refresh data saat range atau tanggal berubah
    LaunchedEffect(selectedRange, selectedDate, selectedMonthYear, selectedYear) {
        when (selectedRange) {
            "Hour" -> viewModel.loadAnxietyData(selectedRange, selectedDate)
            "Week" -> viewModel.loadAnxietyData(selectedRange, selectedMonthYear)
            "Month" -> viewModel.loadAnxietyData(selectedRange, selectedYear)
            "Day" -> viewModel.loadAnxietyData(selectedRange, selectedDate)
            //else -> viewModel.loadAnxietyData(selectedRange)
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

    // Cek apakah ada 3 hari berturut-turut dengan anxiety > 0.5
    val showWarningMessage = if (selectedRange == "Day") {
        val anxietyOverThreshold = averageAnxietyData.filter { it.avgTingkatAnxiety > 0.5 }
        anxietyOverThreshold.size >= 3 &&
                anxietyOverThreshold.zipWithNext { a, b ->
                    // Pastikan nilai `a.day` dan `b.day` tidak null sebelum melakukan operasi
                    a.day?.toIntOrNull()?.let { dayA ->
                        b.day?.toIntOrNull()?.let { dayB ->
                            dayA + 1 == dayB
                        } ?: false  // Jika b.day null, kembalikan false
                    } ?: false  // Jika a.day null, kembalikan false
                }.count { it } >= 2
    } else {
        false
    }


    // Scaffold untuk menata tampilan dengan BottomNavigationBar di bawah
    Scaffold(
        bottomBar = { BottomNavigationBar(currentScreen = "Graph") }  // Menambahkan BottomNavigationBar
    ) { paddingValues ->
        // Konten utama, seperti grafik dan kontrol waktu
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = paddingValues.calculateBottomPadding())
            .verticalScroll(rememberScrollState()) // Menambahkan scroll pada Column
        ) {

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

            // Kontrol tambahan untuk Hour, Week, dan Month
            when (selectedRange) {
                "Hour" -> {
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
                }
                "Week" -> {
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
                        format = "MMMM yyyy"
                    )
                }
                "Month" -> {
                    DateNavigation(
                        date = selectedYear,
                        onPrevious = {
                            calendar.time = selectedYear
                            calendar.add(Calendar.YEAR, -1)
                            selectedYear = calendar.time
                        },
                        onNext = {
                            calendar.time = selectedYear
                            calendar.add(Calendar.YEAR, 1)
                            selectedYear = calendar.time
                        },
                        format = "yyyy"
                    )
                }
                "Day" -> {
                    DayRangeNavigation(
                        rangeText = dayRangeText,
                        onPrevious = {
                            calendar.time = selectedDate
                            calendar.add(Calendar.DAY_OF_YEAR, -7)
                            selectedDate = calendar.time
                            dayRangeText = getDayRange(calendar)
                        },
                        onNext = {
                            calendar.time = selectedDate
                            calendar.add(Calendar.DAY_OF_YEAR, 7)
                            selectedDate = calendar.time
                            dayRangeText = getDayRange(calendar)
                        }
                    )
                }
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
                    barColor = secondaryContainerLight,
                    barArrangement = Arrangement.SpaceEvenly
                )
            } else {
                Text("Data not available for the selected range", style = MaterialTheme.typography.bodyLarge)
            }

            // Tampilkan pesan jika perlu
            if (showWarningMessage) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            color = errorLight, // Gunakan errorContainerLight jika ada
                            shape = MaterialTheme.shapes.medium // Bentuk rounded rectangle
                        )
                        .padding(16.dp) // Memberikan padding di dalam box
                ) {
                    Text(
                        text = "You have experienced high levels of anxiety over the past few days. You should seek professional help if you are experiencing any of the following:\n\n" +
                                "• Difficulty in personal or professional relationships\n" +
                                "• Persistent sleep problems\n" +
                                "• Trouble concentrating\n" +
                                "• Difficulty completing daily tasks\n" +
                                "• Physical discomfort\n" +
                                "• A sense of self-loathing or feelings of worthlessness\n" +
                                "• Social isolation\n" +
                                "• Suicidal thoughts",
                        style = MaterialTheme.typography.bodyLarge,
                        color = onErrorLight // Warna teks di dalam box
                    )
                }
            }

        }
    }
}


@Composable
fun DayRangeNavigation(rangeText: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Previous")
        }
        Text(
            text = rangeText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
        }
    }
}

fun getDayRange(calendar: Calendar): String {
    val startDate = Calendar.getInstance().apply { time = calendar.time; add(Calendar.DAY_OF_YEAR, -7) }
    val endDate = Calendar.getInstance().apply { time = calendar.time }
    val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return "${dateFormat.format(startDate.time)} - ${dateFormat.format(endDate.time)}"
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
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else onSurfaceVariantDark
        ),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text = text)
    }
}
