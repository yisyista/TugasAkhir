package com.example.tugasakhir
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_sensor")
data class DataSensorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val nn20: Int,
    val scrFreq: Float,
    val scrRisetimeMax: Float,
    val scrRisetimeMin: Float,
    val scrRisetimeStd: Float
)
