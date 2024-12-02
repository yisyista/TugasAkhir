package com.example.tugasakhir

data class AverageAnxietyData(
    val avgTingkatAnxiety: Float, // Rata-rata tingkat anxiety
    val hour: Int? = null, // Kolom waktu dalam jam
    val day: String? = null, // Kolom waktu dalam hari (as String to handle date format)
    val week: String? = null, // Kolom waktu dalam minggu (as String to handle week number)
    val month: String? = null, // Kolom waktu dalam bulan (as String to handle month and year)
)
