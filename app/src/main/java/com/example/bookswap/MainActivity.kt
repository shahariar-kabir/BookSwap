package com.example.bookswap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.bookswap.ui.theme.BookSwapTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookSwapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("splash") }
                    var selectedBook by remember { mutableStateOf<Book?>(null) }
                    var tempEmail by remember { mutableStateOf("") }

                    LaunchedEffect(key1 = currentScreen) {
                        if (currentScreen == "splash") {
                            delay(2000)
                            if (authViewModel.user.value != null) {
                                currentScreen = "home"
                            } else {
                                currentScreen = "login"
                            }
                        }
                    }

                    val currentUser = authViewModel.user.value
                    val userName = currentUser?.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
                        ?: currentUser?.email?.substringBefore("@") ?: "User"
                    val userPhotoUrl = null

                    when (currentScreen) {
                        "splash" -> SplashScreen()
                        "login" -> LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { currentScreen = "home" },
                            onSignUpClick = { currentScreen = "signup" }
                        )
                        "signup" -> SignUpScreen(
                            viewModel = authViewModel,
                            onSignUpSuccess = { email -> 
                                tempEmail = email
                                currentScreen = "verify" 
                            },
                            onLoginClick = { currentScreen = "login" }
                        )
                        "verify" -> VerifyScreen(
                            email = tempEmail,
                            viewModel = authViewModel,
                            onVerificationSuccess = { currentScreen = "login" },
                            onBack = { currentScreen = "signup" }
                        )
                        "home" -> HomeScreen(
                            userName = userName,
                            userPhotoUrl = userPhotoUrl,
                            onBookClick = { book ->
                                selectedBook = book
                                currentScreen = "details"
                            },
                            onProfileClick = {
                                currentScreen = "profile"
                            },
                            onLogout = {
                                authViewModel.logout()
                                currentScreen = "login"
                            }
                        )
                        "details" -> selectedBook?.let { book ->
                            BookDetailsScreen(
                                book = book,
                                onBack = { currentScreen = "home" }
                            )
                        }
                        "profile" -> ProfileScreen(
                            userName = userName,
                            userPhotoUrl = userPhotoUrl,
                            onBack = { currentScreen = "home" },
                            onLogout = {
                                authViewModel.logout()
                                currentScreen = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}
