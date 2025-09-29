package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow

class EmotionRepository(private val dao: EmotionDao) {
    
    suspend fun saveEmotionAnalysis(entry: EmotionEntry, foods: List<FoodSelection>) {
        dao.insertEntryWithFoods(entry, foods)
    }
    
    fun getEntriesByDate(dateEpochDay: Long): Flow<List<EmotionEntry>> {
        return dao.getEntriesByDate(dateEpochDay)
    }
    
    fun getEntriesBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<EmotionEntry>> {
        return dao.getEntriesBetween(startEpochDay, endEpochDay)
    }
    
    suspend fun getFoodsByEntry(entryId: Long): List<FoodSelection> {
        return dao.getFoodsByEntry(entryId)
    }
    
    fun getAllEntriesRecent(): Flow<List<EmotionEntry>> {
        return dao.getAllEntriesRecent()
    }
}
