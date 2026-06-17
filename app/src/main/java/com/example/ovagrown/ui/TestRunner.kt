package com.example.ovagrown

import android.util.Log
import com.example.ovagrown.database.Authentication
import com.example.ovagrown.database.FriendFunctions
import com.example.ovagrown.database.FriendRequestFunctions
import com.example.ovagrown.database.ProfileFunctions
import com.example.ovagrown.functional.Friend_Code_Gen

object TestRunner {

    private const val TAG = "TEST"

    suspend fun runFullSystemTest() {

        Log.d("TEST", "🔥 TEST RUNNER WORKS")

        val auth = Authentication()
        val profileFunctions = ProfileFunctions()
        val friendRequests = FriendRequestFunctions()
        val friendFunctions = FriendFunctions()

        try {

            Log.d(TAG, "========== STARTING FULL SYSTEM TEST ==========")

            // Generate unique emails for every run
            val timestamp = System.currentTimeMillis()

            val user1Email = "user1_$timestamp@test.com"
            val user2Email = "user2_$timestamp@test.com"

            // =====================================================
            // STEP 1 - CREATE USER 1
            // =====================================================

            Log.d(TAG, "\n--- Step 1: Creating User 1 ---")

            auth.signUp(user1Email, "password123")

            val user1Id = auth.getCurrentUserId()

            require(user1Id != null) {
                "User 1 UUID was null after signup"
            }

            val user1FriendId = Friend_Code_Gen().generateUnique()

            profileFunctions.createProfile(
                userId = user1Id,
                username = "Alice"
            )

            Log.d(
                TAG,
                "✓ User 1 created: Alice (Friend ID: $user1FriendId)"
            )

            // =====================================================
            // STEP 2 - CREATE USER 2
            // =====================================================

            Log.d(TAG, "\n--- Step 2: Creating User 2 ---")

            auth.logout()

            auth.signUp(user2Email, "password123")

            val user2Id = auth.getCurrentUserId()

            require(user2Id != null) {
                "User 2 UUID was null after signup"
            }

            val user2FriendId = Friend_Code_Gen().generateUnique()

            require(user1FriendId != user2FriendId) {
                "Friend code collision detected"
            }

            profileFunctions.createProfile(
                userId = user2Id,
                username = "Bob"
            )

            Log.d(
                TAG,
                "✓ User 2 created: Bob (Friend ID: $user2FriendId)"
            )

            // =====================================================
            // STEP 3 - SEND FRIEND REQUEST
            // =====================================================

            Log.d(TAG, "\n--- Step 3: Sending Friend Request ---")

            auth.logout()

            auth.login(
                user1Email,
                "password123"
            )

            FriendRequestFunctions().sendRequest(
                user1Id,
                user2FriendId
            )

            Log.d(
                TAG,
                "✓ Alice sent friend request to Bob"
            )

            // =====================================================
            // STEP 4 - CHECK PENDING REQUESTS
            // =====================================================

            Log.d(TAG, "\n--- Step 4: Checking Pending Requests ---")

            val pending =
                FriendRequestFunctions().getPendingRequests(
                    user2FriendId
                )

            require(pending.isNotEmpty()) {
                "Expected pending friend request but found none"
            }

            Log.d(
                TAG,
                "✓ Bob has ${pending.size} pending request(s)"
            )

            // =====================================================
            // STEP 5 - ACCEPT REQUEST
            // =====================================================

            Log.d(TAG, "\n--- Step 5: Accepting Friend Request ---")

            FriendRequestFunctions().acceptRequest(
                pending.first()
            )

            Log.d(
                TAG,
                "✓ Bob accepted Alice's friend request"
            )

            // =====================================================
            // STEP 6 - VERIFY FRIENDSHIP
            // =====================================================

            Log.d(TAG, "\n--- Step 6: Verifying Friendship ---")

            val user1Friends =
                friendFunctions.getFriends(user1Id)

            val user2Friends =
                friendFunctions.getFriends(user2Id)

            require(user1Friends.isNotEmpty()) {
                "Alice should have at least one friend"
            }

            require(user2Friends.isNotEmpty()) {
                "Bob should have at least one friend"
            }

            Log.d(
                TAG,
                "✓ Alice has ${user1Friends.size} friend(s)"
            )

            Log.d(
                TAG,
                "✓ Bob has ${user2Friends.size} friend(s)"
            )

            // =====================================================
            // STEP 7 - UPDATE PROFILES
            // =====================================================

            Log.d(TAG, "\n--- Step 7: Updating Profiles ---")

            profileFunctions.updateStreak(
                user1Id,
                10
            )

            profileFunctions.updateCosmetic(
                user2Id,
                "golden_skin"
            )

            Log.d(
                TAG,
                "✓ Profiles updated"
            )

            // =====================================================
            // STEP 8 - REMOVE FRIENDSHIP
            // =====================================================

            Log.d(TAG, "\n--- Step 8: Removing Friendship ---")

            friendFunctions.removeFriend(
                user1Id,
                user2FriendId
            )

            val user1FriendsAfter =
                friendFunctions.getFriends(user1Id)

            Log.d(
                TAG,
                "✓ Friendship removed. Alice now has ${user1FriendsAfter.size} friend(s)"
            )

            // =====================================================
            // SUCCESS
            // =====================================================

            Log.d(
                TAG,
                "\n========== TEST COMPLETED SUCCESSFULLY =========="
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "========== TEST FAILED =========="
            )

            Log.e(
                TAG,
                "Error: ${e.message}",
                e
            )
        }
    }
}
