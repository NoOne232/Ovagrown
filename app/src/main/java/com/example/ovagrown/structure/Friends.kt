package com.example.ovagrown.structures

import kotlinx.serialization.Serializable

@Serializable
data class Friends(
    val user_id: String,
    val friends_since: String? = null,
    val friend_id: Long
)