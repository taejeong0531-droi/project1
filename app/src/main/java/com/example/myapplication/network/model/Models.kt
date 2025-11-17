package com.example.myapplication.network.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecommendRequest(
    val mood: String? = null,
    val preferences: List<String>? = null,
    val top_k: Int = 5
)

@JsonClass(generateAdapter = true)
data class FoodItem(
    val name: String,
    val kcal: Int? = null,
    val tags: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RecommendResponse(
    val items: List<FoodItem>
)
