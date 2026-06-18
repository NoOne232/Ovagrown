package com.example.overgrown.database

import com.example.overgrown.structures.Friends
import com.example.overgrown.structures.UserProfile
import io.github.jan.supabase.postgrest.from

class FriendFunctions {

    suspend fun removeFriend(
        userId: String,
        friendId: Long
    ) {
        SupabaseClient.client.from("Friends")
            .delete {
                filter {
                    eq("user_id", userId)
                    eq("friend_id", friendId)
                }
            }

        val friendProfile = SupabaseClient.client.from("UserProfiles")
            .select {
                filter {
                    eq("friend_id", friendId)
                }
            }
            .decodeSingleOrNull<UserProfile>()

        val currentUserProfile = SupabaseClient.client.from("UserProfiles")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<UserProfile>()

        val currentFriendId = currentUserProfile?.friend_id ?: return
        val friendUserId = friendProfile?.user_id ?: return

        SupabaseClient.client.from("Friends")
            .delete {
                filter {
                    eq("user_id", friendUserId)
                    eq("friend_id", currentFriendId)
                }
            }
    }

    suspend fun getFriends(
        userId: String
    ): List<Friends> {
        return SupabaseClient.client.from("Friends")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<Friends>()
    }
}
