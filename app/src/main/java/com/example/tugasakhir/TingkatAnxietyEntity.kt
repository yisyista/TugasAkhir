package com.example.tugasakhir

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tingkat_anxiety")
data class TingkatAnxietyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tingkatAnxiety: Long
)