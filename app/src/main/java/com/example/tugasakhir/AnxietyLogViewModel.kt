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

    fun loadAnxietyData(range: String, selectedDateMillis: Long?) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val startTimestamp: Long
            val endTimestamp: Long

            if (range == "Hour" && selectedDateMillis != null) {
                calendar.timeInMillis = selectedDateMillis
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTimestamp = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_YEAR, 1)
                endTimestamp = calendar.timeInMillis
            } else {
                val endTimestampDefault = calendar.timeInMillis
                startTimestamp = when (range) {
                    "Day" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -7)
                        calendar.timeInMillis
                    }
                    "Week" -> {
                        calendar.add(Calendar.MONTH, -1)
                        calendar.timeInMillis
                    }
                    "Month" -> {
                        calendar.add(Calendar.YEAR, -1)
                        calendar.timeInMillis
                    }
                    else -> endTimestampDefault
                }
                endTimestamp = endTimestampDefault
            }

            _averageAnxietyData.value = withContext(Dispatchers.IO) {
                when (range) {
                    "Hour" -> dao.getAverageAnxietyByHour(startTimestamp, endTimestamp)
                    "Day" -> dao.getAverageAnxietyByDay(startTimestamp, endTimestamp)
                    "Week" -> dao.getAverageAnxietyByWeek(startTimestamp, endTimestamp)
                    "Month" -> dao.getAverageAnxietyByMonth(startTimestamp, endTimestamp)
                    else -> emptyList()
                }
            }
        }
    }


}
