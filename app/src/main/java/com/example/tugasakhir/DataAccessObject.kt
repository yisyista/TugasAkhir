package com.example.tugasakhir

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DataAccessObject {
    @Insert
    suspend fun insertTingkatAnxiety(tingkatAnxiety: TingkatAnxietyEntity)

    @Query("SELECT * FROM tingkat_anxiety")
    suspend fun getAllTingkatAnxiety(): List<TingkatAnxietyEntity>

    // Untuk DataSensorEntity
    @Insert
    suspend fun insertDataSensor(data: DataSensorEntity)

    @Query("SELECT * FROM data_sensor ORDER BY timestamp DESC LIMIT 1")
    fun getLatestDataSensor(): Flow<DataSensorEntity>
}
