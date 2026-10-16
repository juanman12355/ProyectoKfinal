package com.example.proyecto_desarrollo.data.repository

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("session", Context.MODE_PRIVATE)

    fun saveUser(userId: Long) = prefs.edit { putLong(KEY_USER_ID, userId) }

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, NO_USER)

    fun isLoggedIn(): Boolean = getUserId() != NO_USER

    fun clear() = prefs.edit { clear() }

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val NO_USER = -1L
    }
}