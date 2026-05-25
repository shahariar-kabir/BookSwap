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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookswap.ui.theme.CyanMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookScreen(
    book: Book,
    viewModel: BookViewModel,
    onBack: () -> Unit,
    onBookUpdated: () -> Unit
) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var location by remember { mutableStateOf(book.location) }
    var description by remember { mutableStateOf(book.description) }
    var isAvailable by remember { mutableStateOf(book.isAvailable) }
    var isForRent by remember { mutableStateOf(book.isForRent) }
    var rentalPrice by remember { mutableStateOf(book.rentalPricePerDay?.toString() ?: "") }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val categories = listOf("Fiction", "Science", "Business", "History", "Arts")
    var selectedCategory by remember { mutableStateOf(book.category) }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val loading by viewModel.loading
    val error by viewModel.error
    val scrollState = rememberScrollState()

    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red) },
            title = { Text("Update Issue", fontWeight = FontWeight.Bold) },
            text = { Text(error ?: "An unexpected error occurred while updating the book.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Try Again", color = CyanMain, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            imageBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Book Info", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (book.imageUrl != null) {
                    AsyncImage(
                        model = book.imageUrl,
                        contentDescription = "Current Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                }
                
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text("Change Cover", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; viewModel.clearError() },
                label = { Text("Book Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = author,
                onValueChange = { author = it; viewModel.clearError() },
                label = { Text("Author") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it; viewModel.clearError() },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                                viewModel.clearError()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it; viewModel.clearError() },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Available for Swaps", fontWeight = FontWeight.Medium)
                Switch(
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = CyanMain)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isForRent, onCheckedChange = { isForRent = it })
                Text("Allow Renting", fontWeight = FontWeight.Medium)
            }

            if (isForRent) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rentalPrice,
                    onValueChange = { rentalPrice = it; viewModel.clearError() },
                    label = { Text("Rental Price per Day ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (loading) {
                CircularProgressIndicator(color = CyanMain)
            } else {
                Button(
                    onClick = {
                        book.id?.let { id ->
                            viewModel.updateBook(
                                bookId = id,
                                title = title,
                                author = author,
                                description = description,
                                category = selectedCategory,
                                location = location,
                                isForRent = isForRent,
                                isAvailable = isAvailable,
                                rentalPrice = rentalPrice.toDoubleOrNull(),
                                imageBitmap = imageBitmap,
                                existingImageUrl = book.imageUrl,
                                onSuccess = onBookUpdated
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanMain)
                ) {
                    Text("SAVE CHANGES", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}
