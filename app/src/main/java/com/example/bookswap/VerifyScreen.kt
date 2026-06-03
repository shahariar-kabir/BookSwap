package com.example.bookswap

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun VerifyScreen(
    email: String,
    viewModel: AuthViewModel,
    onVerificationSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    val loading by viewModel.loading
    val error by viewModel.error

    // Show Dialog when there is a verification error
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red) },
            title = { Text("Verification Issue", fontWeight = FontWeight.Bold) },
            text = { Text(error ?: "Invalid or expired verification code.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Try Again", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    BookSwapScaffold(
        title = "Verification",
        showBack = true,
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Verify Email",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Text(
                text = "Enter the verification code sent to $email",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            BookSwapTextField(
                value = code,
                onValueChange = { if (it.length <= 8) code = it; viewModel.clearError() },
                label = "Verification Code",
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(40.dp))

            BookSwapButton(
                text = "VERIFY",
                onClick = { viewModel.verifyCode(email, code, onVerificationSuccess) },
                isLoading = loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onBack) {
                Text("Back to Sign Up", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
