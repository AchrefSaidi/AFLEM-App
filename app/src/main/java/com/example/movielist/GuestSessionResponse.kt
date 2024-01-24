package com.example.movielist

data class GuestSessionResponse(
    val success: Boolean,
    val guest_session_id: String,
    val expires_at: String
)
