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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    viewModel: BookViewModel,
    onBack: () -> Unit,
    onBookAdded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isForRent by remember { mutableStateOf(false) }
    var rentalPrice by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val categories = listOf("Fiction", "Science", "Business", "History", "Arts")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val loading by viewModel.loading
    val error by viewModel.error
    val scrollState = rememberScrollState()

    // Show Dialog when there is an error
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red) },
            title = { Text("Publishing Issue", fontWeight = FontWeight.Bold) },
            text = { Text(error ?: "An unexpected error occurred while adding the book.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Try Again", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            imageBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    BookSwapScaffold(
        title = "Add New Book",
        showBack = true,
        onBack = onBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Image Upload Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { launcher.launch("image/*") }
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            "Change",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Upload Book Cover", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "Note: Images will be automatically compressed for optimal performance.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            BookSwapTextField(
                value = title,
                onValueChange = { title = it; viewModel.clearError() },
                label = "Book Title",
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BookSwapTextField(
                value = author,
                onValueChange = { author = it; viewModel.clearError() },
                label = "Author",
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BookSwapTextField(
                value = location,
                onValueChange = { location = it; viewModel.clearError() },
                label = "Location (e.g., City)",
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
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
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
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

            BookSwapTextField(
                value = description,
                onValueChange = { description = it; viewModel.clearError() },
                label = "Description",
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.height(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isForRent,
                    onCheckedChange = { isForRent = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Text(
                    "Allow Renting", 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isForRent) {
                Spacer(modifier = Modifier.height(8.dp))
                BookSwapTextField(
                    value = rentalPrice,
                    onValueChange = { rentalPrice = it; viewModel.clearError() },
                    label = "Rental Price per Day ($)",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            BookSwapButton(
                text = "PUBLISH BOOK",
                onClick = {
                    viewModel.addBook(
                        title = title,
                        author = author,
                        description = description,
                        category = selectedCategory,
                        location = location,
                        isForRent = isForRent,
                        rentalPrice = rentalPrice.toDoubleOrNull(),
                        imageBitmap = imageBitmap,
                        onSuccess = onBookAdded
                    )
                },
                isLoading = loading
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
