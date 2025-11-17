package com.example.myapplication.data

import com.example.myapplication.model.EmotionRecord
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreEmotionRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun userCollection(userId: String) =
        db.collection("users").document(userId).collection("emotions")

    suspend fun addEmotion(
        userId: String,
        emotion: String,
        note: String,
        score: Int? = null
    ) {
        val data = hashMapOf(
            "emotion" to emotion,
            "note" to note,
            "score" to score,
            "createdAt" to FieldValue.serverTimestamp()
        )
        userCollection(userId).add(data).await()
    }

    fun observeEmotions(userId: String, limit: Long = 100): Flow<List<EmotionRecord>> = callbackFlow {
        val registration = userCollection(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    EmotionRecord(
                        id = doc.id,
                        emotion = doc.getString("emotion") ?: "",
                        note = doc.getString("note") ?: "",
                        score = (doc.get("score") as? Number)?.toInt(),
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }
}
