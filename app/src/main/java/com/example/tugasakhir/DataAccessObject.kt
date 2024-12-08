package com.example.tugasakhir

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DataAccessObject {

    @Insert
    suspend fun insertTingkatAnxiety(tingkatAnxiety: TingkatAnxietyEntity)

    @Query("SELECT * FROM tingkat_anxiety")
    fun getAllTingkatAnxiety(): Flow<List<TingkatAnxietyEntity>>

    // Untuk DataSensorEntity
    @Insert
    suspend fun insertDataSensor(data: DataSensorEntity)

    @Query("SELECT * FROM data_sensor ORDER BY timestamp DESC LIMIT 1")
    fun getLatestDataSensor(): Flow<DataSensorEntity>

    // Rata-rata tingkat anxiety untuk 24 jam terakhir, dikelompokkan per jam
    @Query("""
        SELECT 
            AVG(tingkatAnxiety) AS avgTingkatAnxiety, 
            strftime('%H', timestamp / 1000, 'unixepoch', 'localtime') AS hour
        FROM tingkat_anxiety 
        WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp 
        GROUP BY hour 
        ORDER BY hour
    """)
    fun getAverageAnxietyByHour(
        startTimestamp: Long,
        endTimestamp: Long
    ): List<AverageAnxietyData>

    // Rata-rata tingkat anxiety untuk 7 hari terakhir, dikelompokkan per hari
    @Query("""
        SELECT 
            AVG(tingkatAnxiety) AS avgTingkatAnxiety, 
            strftime('%d', timestamp / 1000, 'unixepoch', 'localtime') AS day
        FROM tingkat_anxiety 
        WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp 
        GROUP BY day 
        ORDER BY day
    """)
    fun getAverageAnxietyByDay(
        startTimestamp: Long,
        endTimestamp: Long
    ): List<AverageAnxietyData>

    // Rata-rata tingkat anxiety untuk 1 bulan terakhir, dikelompokkan per pekan
    @Query("""
    SELECT AVG(tingkatAnxiety) AS avgTingkatAnxiety, 
           strftime('%W', timestamp / 1000, 'unixepoch', 'localtime') AS week
    FROM tingkat_anxiety 
    WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp
    GROUP BY week
    ORDER BY week
""")
    suspend fun getAverageAnxietyByWeek(startTimestamp: Long, endTimestamp: Long): List<AverageAnxietyData>

    // Rata-rata tingkat anxiety untuk 1 tahun terakhir, dikelompokkan per bulan
    @Query("""
        SELECT 
            AVG(tingkatAnxiety) AS avgTingkatAnxiety, 
            strftime('%m', timestamp / 1000, 'unixepoch', 'localtime') AS month
        FROM tingkat_anxiety 
        WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp 
        GROUP BY month 
        ORDER BY month

    """)
    fun getAverageAnxietyByMonth(
        startTimestamp: Long,
        endTimestamp: Long
    ): List<AverageAnxietyData>
}
