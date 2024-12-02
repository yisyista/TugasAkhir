package com.example.tugasakhir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AnxietyLogViewModel(private val dao: DataAccessObject) : ViewModel() {
    private val _averageAnxietyData = MutableStateFlow<List<AverageAnxietyData>>(emptyList())
    val averageAnxietyData: StateFlow<List<AverageAnxietyData>> = _averageAnxietyData

    fun loadAnxietyData(range: String) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val endTimestamp = calendar.timeInMillis

            val startTimestamp = when (range) {
                "Day" -> {
                    // Set waktu ke jam 00:00 pada hari ini
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                "Week" -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    calendar.timeInMillis
                }
                "Month" -> {
                    calendar.add(Calendar.MONTH, -1)
                    calendar.timeInMillis
                }
                "Year" -> {
                    calendar.add(Calendar.YEAR, -1)
                    calendar.timeInMillis
                }
                else -> endTimestamp
            }

            // Menggunakan withContext untuk memastikan query dijalankan di thread latar belakang
            _averageAnxietyData.value = withContext(Dispatchers.IO) {
                when (range) {
                    "Day" -> dao.getAverageAnxietyByHour(startTimestamp, endTimestamp)
                    "Week" -> dao.getAverageAnxietyByDay(startTimestamp, endTimestamp)
                    "Month" -> dao.getAverageAnxietyByWeek(startTimestamp, endTimestamp)
                    "Year" -> dao.getAverageAnxietyByMonth(startTimestamp, endTimestamp)
                    else -> emptyList()
                }
            }
        }
    }
}
