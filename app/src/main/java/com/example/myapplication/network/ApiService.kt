package com.example.myapplication.network

import com.example.myapplication.network.model.RecommendRequest
import com.example.myapplication.network.model.RecommendResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @GET("/health")
    suspend fun health(): Map<String, String>

    @POST("/recommend")
    suspend fun recommend(
        @Header("Authorization") authorization: String?,
        @Body req: RecommendRequest
    ): Response<RecommendResponse>
}
