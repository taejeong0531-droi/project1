package com.example.myapplication.util

import android.content.Context
import java.util.UUID

object UserIdProvider {
    private const val PREFS = "app_prefs"
    private const val KEY_UID = "uid"

    fun getOrCreate(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_UID, null)
        if (existing != null) return existing
        val newId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_UID, newId).apply()
        return newId
    }
}
