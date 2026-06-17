package com.example.ovagrown.database

import com.example.ovagrown.structures.UserProfile
import com.example.ovagrown.database.SupabaseClient
import com.example.ovagrown.functional.Friend_Code_Gen
import io.github.jan.supabase.postgrest.from

class ProfileFunctions{

    suspend fun createProfile(
        userId: String,
        username: String
    ): UserProfile {
        val uniqueFriendId = Friend_Code_Gen().generateUnique()

        val profile = UserProfile(
            user_id = userId,
            created_at = null,
            username = username,
            friend_id = uniqueFriendId,
            current_streak = 0,
            total_achieved = 0,
            equipped_cosmetic = "default"
        )

        SupabaseClient.client.from("UserProfiles")
            .insert(profile)

        return profile
    }


    suspend fun getProfile(
        userId: String
    ): UserProfile? {
        return SupabaseClient.client.from("UserProfiles")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<UserProfile>()
    }

    suspend fun findUserByFriendId(
        friendId: Long
    ): UserProfile? {
        return SupabaseClient.client.from("UserProfiles")
            .select {
                filter {
                    eq("friend_id", friendId)
                }
            }
            .decodeSingleOrNull<UserProfile>()
    }

    suspend fun updateStreak(
        userId: String,
        streak: Int
    ) {
        SupabaseClient.client.from("UserProfiles")
            .update({
                UserProfile::current_streak setTo streak
            }) {
                filter {
                    eq("user_id", userId)
                }
            }
    }

    suspend fun updateCosmetic(
        userId: String,
        cosmetic: String
    ) {
        SupabaseClient.client.from("UserProfiles")
            .update({
                UserProfile::equipped_cosmetic setTo cosmetic
            }) {
                filter {
                    eq("user_id", userId)
                }
            }
    }
}
