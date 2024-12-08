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

    fun loadAnxietyData(range: String, date: Date? = null) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
            }
            val endTimestamp: Long
            val startTimestamp: Long

            when (range) {
                "Hour" -> {
                    // Set to start of the selected day at 00:00
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    startTimestamp = calendar.timeInMillis

                    // Set endTimestamp to 24 hours later (next day)
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    endTimestamp = calendar.timeInMillis
                }
                "Week" -> {
                    // Set to start of the selected month (1st day of the month)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    startTimestamp = calendar.timeInMillis

                    // Set endTimestamp to the start of the next month
                    calendar.add(Calendar.MONTH, 1)
                    endTimestamp = calendar.timeInMillis
                }
                "Day" -> {
                    // Set to 7 days before the current date
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    startTimestamp = calendar.timeInMillis
                    endTimestamp = System.currentTimeMillis()
                }
                "Month" -> {
                    // Set to start of the selected year (January 1st)
                    calendar.set(Calendar.MONTH, Calendar.JANUARY)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    startTimestamp = calendar.timeInMillis

                    // Set to the end of the selected year (December 31st)
                    calendar.add(Calendar.YEAR, 1)
                    endTimestamp = calendar.timeInMillis
                }
                else -> {
                    // Default behavior (if range is not recognized)
                    endTimestamp = System.currentTimeMillis()
                    startTimestamp = endTimestamp
                }
            }

            // Query the database for data in the selected range
            _averageAnxietyData.value = withContext(Dispatchers.IO) {
                when (range) {
                    "Hour" -> dao.getAverageAnxietyByHour(startTimestamp, endTimestamp) // For Hour, get data for the full day
                    "Week" -> dao.getAverageAnxietyByWeek(startTimestamp, endTimestamp) // For Week, group data by week
                    "Day" -> dao.getAverageAnxietyByDay(startTimestamp, endTimestamp) // For Day, get data for the past 7 days
                    "Month" -> dao.getAverageAnxietyByMonth(startTimestamp, endTimestamp) // For Month, group data by month
                    else -> emptyList()
                }
            }
        }
    }
}
