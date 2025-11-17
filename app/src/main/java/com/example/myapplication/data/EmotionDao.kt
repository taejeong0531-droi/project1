package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmotionDao {
    @Insert
    suspend fun insertEntry(entry: EmotionEntry): Long
    
    @Insert
    suspend fun insertFoods(foods: List<FoodSelection>)
    
    @Transaction
    suspend fun insertEntryWithFoods(entry: EmotionEntry, foods: List<FoodSelection>) {
        val entryId = insertEntry(entry)
        val foodsWithEntryId = foods.map { it.copy(entryId = entryId) }
        insertFoods(foodsWithEntryId)
    }

    @Query("DELETE FROM emotion_entries WHERE dateEpochDay = :dateEpochDay")
    suspend fun deleteEntriesByDate(dateEpochDay: Long)
    
    @Query("SELECT * FROM emotion_entries WHERE dateEpochDay = :dateEpochDay ORDER BY createdAt DESC")
    fun getEntriesByDate(dateEpochDay: Long): Flow<List<EmotionEntry>>
    
    @Query("SELECT * FROM emotion_entries WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay DESC, createdAt DESC")
    fun getEntriesBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<EmotionEntry>>
    
    @Query("SELECT * FROM food_selections WHERE entryId = :entryId ORDER BY chosenAt")
    suspend fun getFoodsByEntry(entryId: Long): List<FoodSelection>
    
    @Query("SELECT * FROM emotion_entries ORDER BY createdAt DESC LIMIT 50")
    fun getAllEntriesRecent(): Flow<List<EmotionEntry>>

    // 선택된 음식이 하나도 연결되지 않은 엔트리 정리(과거 자동저장 잔재 제거)
    @Query(
        "DELETE FROM emotion_entries WHERE id IN (" +
                "SELECT e.id FROM emotion_entries e LEFT JOIN food_selections f ON f.entryId = e.id " +
                "GROUP BY e.id HAVING COALESCE(SUM(CASE WHEN f.isSelected = 1 THEN 1 ELSE 0 END), 0) = 0"
                + ")"
    )
    suspend fun deleteEntriesWithoutSelectedFood()
}
