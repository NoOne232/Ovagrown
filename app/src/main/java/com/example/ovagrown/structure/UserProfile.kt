package com.example.ovagrown.structures

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val user_id: String,
    val created_at: String? = null,
    val username: String,
    val friend_id: Long,
    val current_streak: Int,
    val total_achieved: Int,
    val equipped_cosmetic: String
)