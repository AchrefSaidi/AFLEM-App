package com.example.movielist

import android.content.Context

object SessionManager {
    private const val PREFS_NAME = "movie_app_prefs"
    private const val SESSION_ID = "guest_session_id"

    fun saveSessionId(context: Context, sessionId: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(SESSION_ID, sessionId).apply()
    }

    fun getSessionId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(SESSION_ID, null)
    }
}
