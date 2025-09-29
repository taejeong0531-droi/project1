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
    
    @Query("SELECT * FROM emotion_entries WHERE dateEpochDay = :dateEpochDay ORDER BY createdAt DESC")
    fun getEntriesByDate(dateEpochDay: Long): Flow<List<EmotionEntry>>
    
    @Query("SELECT * FROM emotion_entries WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay DESC, createdAt DESC")
    fun getEntriesBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<EmotionEntry>>
    
    @Query("SELECT * FROM food_selections WHERE entryId = :entryId ORDER BY chosenAt")
    suspend fun getFoodsByEntry(entryId: Long): List<FoodSelection>
    
    @Query("SELECT * FROM emotion_entries ORDER BY createdAt DESC LIMIT 50")
    fun getAllEntriesRecent(): Flow<List<EmotionEntry>>
}
