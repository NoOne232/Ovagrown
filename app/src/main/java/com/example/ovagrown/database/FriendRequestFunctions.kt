package com.example.ovagrown.database

import com.example.ovagrown.structures.UserProfile
import com.example.ovagrown.structures.Friend_requests
import com.example.ovagrown.structures.Friends
import io.github.jan.supabase.postgrest.from

class FriendRequestFunctions{

    // Helper function to check if request already exists
    private suspend fun requestExists(
        senderId: String,
        receiverFriendId: Long
    ): Boolean {
        val existingRequest = SupabaseClient.client.from("Friend_requests")
            .select {
                filter {
                    eq("sender_id", senderId)
                    eq("receiver_friend_id", receiverFriendId)
                    eq("status", "pending")
                }
            }
            .decodeSingleOrNull<Friend_requests>()

        return existingRequest != null
    }

    // Helper function to check if they're already friends
    private suspend fun alreadyFriends(
        senderId: String,
        receiverFriendId: Long
    ): Boolean {
        val friendship = SupabaseClient.client.from("Friends")
            .select {
                filter {
                    eq("user_id", senderId)
                    eq("friend_id", receiverFriendId)
                }
            }
            .decodeSingleOrNull<Friends>()

        return friendship != null
    }

    suspend fun sendRequest(
        senderId: String,
        receiverFriendId: Long
    ): String {
        // Check if users can't send request to themselves
        val senderProfile = SupabaseClient.client.from("UserProfiles")
            .select {
                filter { eq("user_id", senderId) }
            }
            .decodeSingleOrNull<UserProfile>()

        if (senderProfile?.friend_id == receiverFriendId) {
            return "You cannot send a friend request to yourself"
        }

        // Check if receiver exists
        val receiverExists = SupabaseClient.client.from("UserProfiles")
            .select {
                filter { eq("friend_id", receiverFriendId) }
            }
            .decodeSingleOrNull<UserProfile>()

        if (receiverExists == null) {
            return "User with this Friend ID not found"
        }

        // Check if already friends
        if (alreadyFriends(senderId, receiverFriendId)) {
            return "You are already friends with this user"
        }

        // Check for existing pending request
        if (requestExists(senderId, receiverFriendId)) {
            return "You already have a pending request to this user"
        }

        // All checks passed, create the request
        val request = Friend_requests(
            sender_id = senderId,
            receiver_friend_id = receiverFriendId,
            status = "pending"
        )
        SupabaseClient.client.from("Friend_requests").insert(request)

        return "Success"
    }

    suspend fun getPendingRequests(
        receiverFriendId: Long
    ): List<Friend_requests> {
        return SupabaseClient.client.from("Friend_requests")
            .select {
                filter {
                    eq("receiver_friend_id", receiverFriendId)
                    eq("status", "pending")
                }
            }
            .decodeList<Friend_requests>()
    }

    suspend fun acceptRequest(
        request: Friend_requests
    ) {
        // Update request status to accepted
        SupabaseClient.client.from("Friend_requests")
            .update({
                Friend_requests::status setTo "accepted"
            }) {
                filter {
                    eq("sender_id", request.sender_id)
                    eq("receiver_friend_id", request.receiver_friend_id)
                }
            }

        // Get sender's friend_id from user_profiles
        val senderProfile = SupabaseClient.client.from("UserProfiles")
            .select {
                filter { eq("user_id", request.sender_id) }
            }
            .decodeSingleOrNull<UserProfile>()

        // Get receiver's user_id from user_profiles
        val receiverProfile = SupabaseClient.client.from("UserProfiles")
            .select {
                filter { eq("friend_id", request.receiver_friend_id) }
            }
            .decodeSingleOrNull<UserProfile>()

        if (senderProfile != null && receiverProfile != null) {
            // Create bidirectional friendship entries

            // Entry 1: Sender -> Receiver
            SupabaseClient.client.from("Friends").insert(
                Friends(
                    user_id = request.sender_id,
                    friend_id = request.receiver_friend_id
                )
            )

            // Entry 2: Receiver -> Sender (reverse)
            SupabaseClient.client.from("Friends").insert(
                Friends(
                    user_id = receiverProfile.user_id,
                    friend_id = senderProfile.friend_id
                )
            )
        }
    }


    suspend fun rejectRequest(
        request: Friend_requests
    ) {
        SupabaseClient.client.from("Friend_requests")
            .update({
                Friend_requests::status setTo "rejected"
            }) {
                filter {
                    eq("sender_id", request.sender_id)
                    eq("receiver_friend_id", request.receiver_friend_id)
                }
            }
    }
}