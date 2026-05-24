package com.example.bookswap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookswap.ui.theme.CyanMain
import com.example.bookswap.ui.theme.DarkTeal

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val loading by viewModel.loading
    val error by viewModel.error
    val scrollState = rememberScrollState()
    val windowSize = rememberWindowSize()

    // Show Dialog when there is an error
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red) },
            title = { Text("Login Issue", fontWeight = FontWeight.Bold) },
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
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 120.dp else 32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 20.dp else 40.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("BOOK", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("SWAP", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 24.dp))
                }
                
                Surface(
                    modifier = Modifier.size(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 60.dp else 80.dp),
                    shape = RoundedCornerShape(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 30.dp else 40.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 40.dp else 100.dp))

            Text(
                text = "Login",
                fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 40.sp else 56.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 20.dp else 40.dp))

            TextField(
                value = identifier,
                onValueChange = { identifier = it; viewModel.clearError() },
                label = { Text("Email or Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text("Remember me", fontSize = 12.sp)
                }
                TextButton(onClick = onForgotPasswordClick) {
                    Text("Forgot password?", fontSize = 12.sp, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 16.dp else 32.dp))

            if (loading) {
                CircularProgressIndicator(color = CyanMain)
            } else {
                Button(
                    onClick = { viewModel.login(identifier, password, onLoginSuccess) },
                    modifier = Modifier.fillMaxWidth().height(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 50.dp else 64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanMain)
                ) {
                    Text("LOGIN", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    append("Don't have an account? ")
                    withStyle(style = SpanStyle(color = CyanMain, fontWeight = FontWeight.Bold)) {
                        append("Sign Up")
                    }
                },
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding()
                    .clickable { onSignUpClick() },
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun HeaderBackground() {
    Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * 0.2f)
            
            cubicTo(
                size.width * 0.8f, size.height * 0.1f,
                size.width * 0.6f, size.height * 0.4f,
                size.width * 0.4f, size.height * 0.5f
            )
            cubicTo(
                size.width * 0.2f, size.height * 0.6f,
                0f, size.height * 0.8f,
                0f, size.height
            )
            close()
        }
        
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(DarkTeal, CyanMain)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        viewModel = AuthViewModel(),
        onLoginSuccess = {},
        onSignUpClick = {},
        onForgotPasswordClick = {}
    )
}
