package com.example.bookswap

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

    // Show Dialog when there is an error
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red) },
            title = { Text("Reset Issue", fontWeight = FontWeight.Bold) },
            text = { Text(error ?: "An unexpected error occurred.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Try Again", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    BookSwapScaffold(
        title = "Reset Password",
        showBack = true,
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = if (resetSent) "Check Your Email" else "Forgot Password?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (resetSent) {
                Text(
                    text = "We have sent a password reset link to $email. Please check your inbox and follow the instructions to reset your password.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                BookSwapButton(
                    text = "BACK TO LOGIN",
                    onClick = onBack
                )
            } else {
                Text(
                    text = "Enter your email address below and we'll send you a link to reset your password.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(40.dp))

                BookSwapTextField(
                    value = email,
                    onValueChange = { email = it; viewModel.clearError() },
                    label = "Email Address",
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                BookSwapButton(
                    text = "SEND RESET LINK",
                    onClick = { 
                        viewModel.sendResetPassword(email) {
                            resetSent = true
                        }
                    },
                    isLoading = loading
                )
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
