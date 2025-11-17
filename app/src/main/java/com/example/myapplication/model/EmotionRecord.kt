package com.example.myapplication.model

import com.google.firebase.Timestamp

// Firestore 문서 모델
// createdAt은 서버타임스탬프로 저장되며, 수신 시 null일 수 있어 nullable 처리
// id는 문서 ID를 별도로 보관하기 위해 필드로 포함

data class EmotionRecord(
    val id: String = "",
    val emotion: String = "",
    val note: String = "",
    val score: Int? = null,
    val createdAt: Timestamp? = null,
)
