package com.example.myapplication.network.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherReq(
    val temp_c: Double? = null,
    val status: String? = null,
    val lat: Double? = null,
    val lon: Double? = null
)

@JsonClass(generateAdapter = true)
data class PreferencesReq(
    val likes: List<String>? = null,
    val dislikes: List<String>? = null,
    val sensitive_spicy: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class RecentLogReq(
    val food: String,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class RecommendRequest(
    val user_id: String,
    val text: String,
    val weather: WeatherReq? = null,
    val recent_logs: List<RecentLogReq>? = null,
    val preferences: PreferencesReq? = null,
    val emotion_label: String? = null,
    val emotion_vector: EmotionVector? = null,
    val score_intensity: Double? = null
)

@JsonClass(generateAdapter = true)
data class FoodScore(
    val food: String,
    val score: Double
)

@JsonClass(generateAdapter = true)
data class RecommendResponse(
    val emotion: String,
    val score: Double? = null,
    val items: List<FoodScore>
)

@JsonClass(generateAdapter = true)
data class EmotionVector(
    val joy: Int,
    val energy: Int,
    val social: Int,
    val calm: Int,
    val focus: Int
)
