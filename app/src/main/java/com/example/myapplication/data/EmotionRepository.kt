package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow

class EmotionRepository(private val dao: EmotionDao) {
    
    suspend fun saveEmotionAnalysis(entry: EmotionEntry, foods: List<FoodSelection>) {
        // 기존 기록은 보존하고, 사용자가 선택한 순간의 결과만 추가로 저장한다
        dao.insertEntryWithFoods(entry, foods)
    }
    
    suspend fun cleanupEntriesWithoutSelection() {
        dao.deleteEntriesWithoutSelectedFood()
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

    fun getAllEntriesRecent(): Flow<List<EmotionEntry>> = dao.getAllEntriesRecent()
}
