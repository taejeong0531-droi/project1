package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_selections",
    foreignKeys = [
        ForeignKey(
            entity = EmotionEntry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FoodSelection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryId: Long,
    val name: String,
    val calories: Int?,
    val tags: String,  // CSV 형식으로 저장
    val isSelected: Boolean = false,  // 사용자가 실제로 선택한 음식인지 여부
    val chosenAt: Long = System.currentTimeMillis()
)
