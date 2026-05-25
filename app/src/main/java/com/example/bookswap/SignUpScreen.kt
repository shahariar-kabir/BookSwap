package com.example.bookswap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignUpSuccess: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    val loading by viewModel.loading
    val error by viewModel.error
    val scrollState = rememberScrollState()
    val windowSize = rememberWindowSize()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            imageBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    // Show Dialog when there is a sign-up error
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red) },
            title = { Text("Registration Issue", fontWeight = FontWeight.Bold) },
            text = { Text(error ?: "An unexpected error occurred during sign up.") },
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
            
            // Top Row
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

            Spacer(modifier = Modifier.height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 20.dp else 40.dp))

            Text(
                text = "Sign Up",
                fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 40.sp else 56.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture Picker
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = "Add Photo",
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Text("Add Profile Picture", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = name,
                onValueChange = { name = it; viewModel.clearError() },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = username,
                onValueChange = { username = it; viewModel.clearError() },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = phone,
                onValueChange = { phone = it; viewModel.clearError() },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = address,
                onValueChange = { address = it; viewModel.clearError() },
                label = { Text("Address") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            if (loading) {
                CircularProgressIndicator(color = CyanMain)
            } else {
                Button(
                    onClick = { 
                        viewModel.signUp(email, password, name, username, phone, address, imageBitmap) { 
                            onSignUpSuccess(email) 
                        } 
                    },
                    modifier = Modifier.fillMaxWidth().height(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 50.dp else 64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanMain)
                ) {
                    Text("SIGN UP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    append("Already have an account? ")
                    withStyle(style = SpanStyle(color = CyanMain, fontWeight = FontWeight.Bold)) {
                        append("Login")
                    }
                },
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding()
                    .clickable { onLoginClick() },
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        viewModel = AuthViewModel(),
        onSignUpSuccess = {},
        onLoginClick = {}
    )
}
