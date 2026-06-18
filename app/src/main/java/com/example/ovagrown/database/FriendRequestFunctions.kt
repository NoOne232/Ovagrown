package com.example.overgrown.database

import com.example.overgrown.structures.UserProfile
import com.example.overgrown.structures.Friend_requests
import com.example.overgrown.structures.Friends
import io.github.jan.supabase.postgrest.from

class FriendRequestFunctions {

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
        val senderProfile = SupabaseClient.client.from("UserProfiles")
            .select {
                filter {
                    eq("user_id", senderId)
                }
            }
            .decodeSingleOrNull<UserProfile>()

        if (senderProfile == null) {
            return "Current user profile not found"
        }

        val senderFriendId = senderProfile.friend_id
            ?: return "Current user does not have a Friend ID yet"

        if (senderFriendId == receiverFriendId) {
            return "You cannot send a friend request to yourself"
        }

        val receiverExists = SupabaseClient.client.from("UserProfiles")
            .select {
                filter {
                    eq("friend_id", receiverFriendId)
                }
            }
            .decodeSingleOrNull<UserProfile>()

        if (receiverExists == null) {
            return "User with this Friend ID not found"
        }

        if (alreadyFriends(senderId, receiverFriendId)) {
            return "You are already friends with this user"
        }

        if (requestExists(senderId, receiverFriendId)) {
            return "You already have a pending request to this user"
        }

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
        SupabaseClient.client.from("Friend_requests")
            .update({
                Friend_requests::status setTo "accepted"
            }) {
                filter {
                    eq("sender_id", request.sender_id)
                    eq("receiver_friend_id", request.receiver_friend_id)
                }
            }

        val senderProfile = SupabaseClient.client.from("UserProfiles")
            .select {
                filter {
                    eq("user_id", request.sender_id)
                }
            }
            .decodeSingleOrNull<UserProfile>()

        val receiverProfile = SupabaseClient.client.from("UserProfiles")
            .select {
                filter {
                    eq("friend_id", request.receiver_friend_id)
                }
            }
            .decodeSingleOrNull<UserProfile>()

        if (senderProfile == null || receiverProfile == null) {
            return
        }

        val senderFriendId = senderProfile.friend_id ?: return

        SupabaseClient.client.from("Friends").insert(
            Friends(
                user_id = request.sender_id,
                friend_id = request.receiver_friend_id
            )
        )

        SupabaseClient.client.from("Friends").insert(
            Friends(
                user_id = receiverProfile.user_id,
                friend_id = senderFriendId
            )
        )
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
