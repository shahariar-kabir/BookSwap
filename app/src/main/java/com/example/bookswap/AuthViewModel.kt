package com.example.bookswap

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthViewModel : ViewModel() {
    private val auth = supabase.auth
    private val postgrest = supabase.postgrest

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _user = mutableStateOf<UserInfo?>(auth.currentUserOrNull())
    val user: State<UserInfo?> = _user

    fun login(identifier: String, password: String, onSuccess: () -> Unit) {
        if (identifier.isBlank() || password.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val emailToUse = if (identifier.contains("@")) {
                    identifier
                } else {
                    // Try to find email by full_name in profiles table
                    val response = postgrest["profiles"].select {
                        filter {
                            eq("full_name", identifier)
                        }
                    }
                    val data = response.decodeList<Profile>()
                    if (data.isNotEmpty()) {
                        data[0].email
                    } else {
                        throw Exception("User not found with this name")
                    }
                }

                auth.signInWith(Email) {
                    this.email = emailToUse
                    this.password = password
                }
                _user.value = auth.currentUserOrNull()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String,
        address: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || name.isBlank() || phone.isBlank() || address.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("full_name", name)
                        put("phone", phone)
                        put("address", address)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Sign up failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun verifyCode(email: String, code: String, onSuccess: () -> Unit) {
        if (code.isBlank() || code.length < 6) {
            _error.value = "Please enter the verification code"
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                auth.verifyEmailOtp(
                    type = OtpType.Email.SIGNUP,
                    email = email,
                    token = code
                )
                auth.signOut() // Sign out after verification so they have to login
                _user.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Verification failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            _user.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }
}

@kotlinx.serialization.Serializable
data class Profile(
    val email: String
)
