package com.example.tugasakhir

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DataAccessObject {
    @Insert
    suspend fun insertTingkatAnxiety(tingkatAnxiety: TingkatAnxietyEntity)

    @Query("SELECT * FROM tingkat_anxiety")
    suspend fun getAllTingkatAnxiety(): List<TingkatAnxietyEntity>
}
