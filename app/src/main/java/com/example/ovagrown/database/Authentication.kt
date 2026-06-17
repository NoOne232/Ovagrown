package com.example.ovagrown.database

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class Authentication {

    suspend fun signUp(
        email: String,
        password: String
    ) {
        SupabaseClient.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun login(
        email: String,
        password: String
    ):String? {
        SupabaseClient.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return getCurrentUserId()
    }

    suspend fun logout() {
        SupabaseClient.client.auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return SupabaseClient.client.auth.currentUserOrNull()?.id
    }

    fun isLoggedIn(): Boolean {
        return SupabaseClient.client.auth.currentSessionOrNull() != null
    }
}