package com.example.bookswap

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.util.UUID

class AuthViewModel : ViewModel() {
    private val auth = supabase.auth
    private val postgrest = supabase.postgrest
    private val storage = supabase.storage

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _user = mutableStateOf<UserInfo?>(auth.currentUserOrNull())
    val user: State<UserInfo?> = _user

    private val _profile = mutableStateOf<Profile?>(null)
    val profile: State<Profile?> = _profile

    private val _viewedProfile = mutableStateOf<Profile?>(null)
    val viewedProfile: State<Profile?> = _viewedProfile

    private var pendingAvatar: Bitmap? = null

    init {
        if (_user.value != null) {
            fetchProfile()
        }
    }

    fun fetchProfile() {
        val currentUser = auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                val result = postgrest["profiles"].select {
                    filter {
                        eq("id", currentUser.id)
                    }
                }.decodeSingle<Profile>()
                _profile.value = result
                
                if (pendingAvatar != null) {
                    uploadPendingAvatar()
                }
            } catch (e: Exception) {
                _error.value = "Failed to load profile: ${e.message}"
            }
        }
    }

    private fun uploadPendingAvatar() {
        val avatar = pendingAvatar ?: return
        val currentUser = auth.currentUserOrNull() ?: return
        
        viewModelScope.launch {
            try {
                val compressedBytes = compressImage(avatar)
                // Path changed to match SQL Policy: folder must be the UID
                val fileName = "${currentUser.id}/${UUID.randomUUID()}.jpg"
                val bucket = storage.from("avatars")
                bucket.upload(fileName, compressedBytes, upsert = true)
                val avatarUrl = bucket.publicUrl(fileName)

                postgrest["profiles"].update({
                    Profile::avatarUrl setTo avatarUrl
                }) {
                    filter {
                        eq("id", currentUser.id)
                    }
                }
                pendingAvatar = null
                fetchProfile()
            } catch (e: Exception) {
                println("Failed to upload pending avatar: ${e.message}")
            }
        }
    }

    fun fetchUserProfile(userId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val result = postgrest["profiles"].select {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingle<Profile>()
                _viewedProfile.value = result
            } catch (e: Exception) {
                _error.value = "Failed to load user profile: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearViewedProfile() {
        _viewedProfile.value = null
    }

    private suspend fun compressImage(bitmap: Bitmap): ByteArray = withContext(Dispatchers.Default) {
        var quality = 80
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        var compressedBytes = outputStream.toByteArray()
        
        // Strictly enforcing the 300 KB limit
        while (compressedBytes.size > 300 * 1024 && quality > 10) {
            quality -= 10
            val loopStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, loopStream)
            compressedBytes = loopStream.toByteArray()
        }
        compressedBytes
    }

    fun updateProfile(
        fullName: String, 
        username: String, 
        phone: String, 
        address: String, 
        avatarBitmap: Bitmap? = null,
        onSuccess: () -> Unit
    ) {
        val currentUser = auth.currentUserOrNull() ?: return
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                var avatarUrl = _profile.value?.avatarUrl

                if (avatarBitmap != null) {
                    val compressedBytes = compressImage(avatarBitmap)
                    // Path changed to match SQL Policy: folder must be the UID
                    val fileName = "${currentUser.id}/${UUID.randomUUID()}.jpg"
                    val bucket = storage.from("avatars")
                    bucket.upload(fileName, compressedBytes, upsert = true)
                    avatarUrl = bucket.publicUrl(fileName)
                }

                postgrest["profiles"].update({
                    Profile::fullName setTo fullName
                    Profile::username setTo username
                    Profile::phone setTo phone
                    Profile::address setTo address
                    Profile::avatarUrl setTo avatarUrl
                }) {
                    filter {
                        eq("id", currentUser.id)
                    }
                }
                fetchProfile()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to update profile: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

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
                    val response = postgrest["profiles"].select {
                        filter {
                            or {
                                eq("full_name", identifier)
                                eq("username", identifier)
                            }
                        }
                    }
                    val data = response.decodeList<Profile>()
                    if (data.isNotEmpty()) {
                        data[0].email
                    } else {
                        throw Exception("User not found")
                    }
                }

                auth.signInWith(Email) {
                    this.email = emailToUse
                    this.password = password
                }
                _user.value = auth.currentUserOrNull()
                fetchProfile()
                onSuccess()
            } catch (e: Exception) {
                val message = e.message ?: ""
                _error.value = when {
                    message.contains("Invalid login credentials", ignoreCase = true) -> 
                        "Incorrect email or password. Please try again."
                    message.contains("User not found", ignoreCase = true) -> 
                        "No account found with this name or email."
                    else -> "Login failed. Please check your connection."
                }
            } finally {
                _loading.value = false
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        name: String,
        username: String,
        phone: String,
        address: String,
        avatarBitmap: Bitmap? = null,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || name.isBlank() || username.isBlank() || phone.isBlank() || address.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }
        _loading.value = true
        _error.value = null
        
        pendingAvatar = avatarBitmap
        
        viewModelScope.launch {
            try {
                auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("full_name", name)
                        put("username", username)
                        put("phone", phone)
                        put("address", address)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                val message = e.message ?: ""
                _error.value = when {
                    message.contains("User already registered", ignoreCase = true) -> 
                        "An account with this email already exists."
                    else -> message.ifBlank { "Sign up failed" }
                }
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
                auth.signOut() 
                _user.value = null
                _profile.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Invalid or expired verification code."
            } finally {
                _loading.value = false
            }
        }
    }

    fun sendResetPassword(email: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            _error.value = "Please enter your email"
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                auth.resetPasswordForEmail(email)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to send reset link. Check your email address."
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            _user.value = null
            _profile.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }
}

@Serializable
data class Profile(
    val id: String? = null,
    @SerialName("full_name")
    val fullName: String,
    val username: String? = null,
    val email: String,
    val phone: String? = null,
    val address: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)
