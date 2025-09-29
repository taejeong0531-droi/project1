package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emotion_entries")
data class EmotionEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,  // LocalDate.toEpochDay()
    val emotion: String,     // happy, angry, neutral
    val score: Float,        // 0.0 ~ 1.0
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
