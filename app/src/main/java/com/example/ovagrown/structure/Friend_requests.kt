package com.example.ovagrown.structures

import kotlinx.serialization.Serializable

@Serializable
data class Friend_requests(
    val sender_id: String,
    val sent_on: String? = null,
    val receiver_friend_id: Long,
    val status: String = "pending"
)

