package com.example.ovagrown.database

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://rcgyceojtbimoqkbbvrx.supabase.co",
        supabaseKey = "sb_publishable_e1BrBK_VgxIsQF1-2sXHsQ_TvSSgFRP"
    ) {
        install(Auth)
        install(Postgrest)
    }
}