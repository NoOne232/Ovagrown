package com.example.ovagrown.functional

import com.example.ovagrown.database.SupabaseClient
import com.example.ovagrown.structures.UserProfile
import io.github.jan.supabase.postgrest.from
import kotlin.random.Random

class Friend_Code_Gen {

    private fun generate(): Long {
        return Random.nextLong(
            10_000_000L,
            99_999_999L
        )
    }

    private suspend fun checkUnique( friendId: Long ): Boolean
    {
        val existingProfile = SupabaseClient.client.from("UserProfiles")
            .select {
                filter { eq("friend_id", friendId) }
            }
            .decodeSingleOrNull<UserProfile>()
        return (existingProfile == null)
    }

    suspend fun generateUnique(): Long {
        var friendId: Long
        var isUnique = false
        var attempts = 0
        val maxAttempts = 100 // Prevent too many attempts

        do {
            friendId = generate()

            // Check if this ID already exists
            isUnique = checkUnique(friendId)
            attempts++

            if (attempts >= maxAttempts && !isUnique) {
                throw Exception("Failed to generate unique Friend ID after maximum attempts. Please try again")
            }
        } while (!isUnique)

        return friendId
    }
}

