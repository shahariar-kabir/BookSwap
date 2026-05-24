package com.example.bookswap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookswap.ui.theme.CyanMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var resetSent by remember { mutableStateOf(false) }

    val loading by viewModel.loading
    val error by viewModel.error
    val scrollState = rememberScrollState()
    val windowSize = rememberWindowSize()

    // Show Dialog when there is an error
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red) },
            title = { Text("Reset Issue", fontWeight = FontWeight.Bold) },
            text = { Text(error ?: "An unexpected error occurred.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Try Again", color = CyanMain, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        HeaderBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 120.dp else 32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 20.dp else 40.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Forgot Password", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 40.dp else 100.dp))

            Text(
                text = if (resetSent) "Check Email" else "Reset Password",
                fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 32.sp else 48.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 20.dp else 40.dp))

            if (resetSent) {
                Text(
                    text = "We have sent a password reset link to $email. Please check your inbox.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 50.dp else 64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanMain)
                ) {
                    Text("BACK TO LOGIN", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            } else {
                Text(
                    text = "Enter your email address and we'll send you a link to get back into your account.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                TextField(
                    value = email,
                    onValueChange = { email = it; viewModel.clearError() },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (loading) {
                    CircularProgressIndicator(color = CyanMain)
                } else {
                    Button(
                        onClick = { 
                            viewModel.sendResetPassword(email) {
                                resetSent = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 50.dp else 64.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanMain)
                    ) {
                        Text("SEND RESET LINK", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(
        viewModel = AuthViewModel(),
        onBack = {}
    )
}
