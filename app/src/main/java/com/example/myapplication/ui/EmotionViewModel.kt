package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirestoreEmotionRepository
import com.example.myapplication.model.EmotionRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EmotionViewModel(
    private val repo: FirestoreEmotionRepository = FirestoreEmotionRepository()
) : ViewModel() {

    private val _emotions = MutableStateFlow<List<EmotionRecord>>(emptyList())
    val emotions: StateFlow<List<EmotionRecord>> = _emotions

    fun startObserving(userId: String) {
        viewModelScope.launch {
            repo.observeEmotions(userId).collectLatest { list ->
                _emotions.value = list
            }
        }
    }

    fun addEmotion(userId: String, emotion: String, note: String, score: Int? = null) {
        viewModelScope.launch {
            runCatching { repo.addEmotion(userId, emotion, note, score) }
        }
    }
}
