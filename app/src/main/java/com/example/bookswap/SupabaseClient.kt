package com.example.bookswap

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://mlfrxshjpnxnjzuloxhk.supabase.co",
    supabaseKey = "sb_publishable_aD8Cw9PhpB05FMtE4-aE2g_k8PKhbH_"
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
}
