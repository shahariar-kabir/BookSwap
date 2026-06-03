package com.example.bookswap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun BookDetailsScreen(
    bookId: Long,
    currentUserId: String,
    chatViewModel: ChatViewModel,
    bookViewModel: BookViewModel,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onRequestSent: () -> Unit
) {
    val book = bookViewModel.books.find { it.id == bookId } ?: return
    
    val loading by chatViewModel.loading
    val bookLoading by bookViewModel.loading
    val error by bookViewModel.error
    
    val wishlist = bookViewModel.wishlist
    val isWishlisted = wishlist.contains(bookId)
    
    val windowSize = rememberWindowSize()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    val isOwner = book.ownerId == currentUserId
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = if (isOwner) listOf("Overview", "Details", "Reviews", "Edit Info") else listOf("Overview", "Details", "Reviews")

    val reviews = bookViewModel.bookReviews

    LaunchedEffect(bookId) {
        bookViewModel.fetchBookReviews(bookId)
    }

    if (error != null) {
        AlertDialog(
            onDismissRequest = { bookViewModel.clearError() },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red) },
            title = { Text("Update Issue", fontWeight = FontWeight.Bold) },
            text = { Text(error ?: "An unexpected error occurred.") },
            confirmButton = {
                TextButton(onClick = { bookViewModel.clearError() }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showReviewDialog) {
        ReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                book.id?.let { bookViewModel.submitReview(it, rating, comment) {} }
                showReviewDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!bookLoading) showDeleteDialog = false },
            title = { Text("Delete Book") },
            text = { Text("Are you sure you want to delete this book? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        book.id?.let { id ->
                            bookViewModel.deleteBook(id) {}
                            showDeleteDialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, enabled = !bookLoading) {
                    Text("Cancel")
                }
            }
        )
    }

    BookSwapScaffold(
        bottomBar = {
            if (isOwner) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .floatingElement(shape = RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = book.isAvailable,
                                onCheckedChange = { book.id?.let { id -> bookViewModel.updateBookAvailability(id, it) } },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (book.isAvailable) "Available" else "Busy", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(40.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    if (book.isAvailable) {
                        BookSwapButton(
                            text = "Swap Now",
                            onClick = { 
                                scope.launch {
                                    chatViewModel.sendChatRequest(
                                        receiverId = book.ownerId ?: "",
                                        bookId = book.id ?: 0,
                                        type = "swap",
                                        onSuccess = onRequestSent
                                    )
                                }
                            },
                            isLoading = loading
                        )
                    }

                    if (book.isForRent && book.rentalPricePerDay != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { 
                                scope.launch {
                                    chatViewModel.sendChatRequest(
                                        receiverId = book.ownerId ?: "",
                                        bookId = book.id ?: 0,
                                        type = "rent",
                                        onSuccess = onRequestSent
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = !loading,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = "Rent for $${book.rentalPricePerDay}/day", 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.Bold, 
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 300.dp else 420.dp).padding(16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    if (book.imageUrl != null) {
                        AsyncImage(model = book.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    if (!book.isAvailable) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                            Surface(color = Color.White, shape = RoundedCornerShape(8.dp)) {
                                Text("BUSY", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape).size(40.dp)) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White)
                    }
                    
                    IconButton(
                        onClick = { 
                            if (isOwner) {
                                showDeleteDialog = true 
                            } else {
                                book.id?.let { bookViewModel.toggleWishlist(it) }
                            }
                        }, 
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape).size(40.dp)
                    ) {
                        if (isOwner) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        } else {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, 
                                contentDescription = "Wishlist", 
                                tint = if (isWishlisted) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(), 
                    shape = RoundedCornerShape(20.dp), 
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = book.title, 
                                color = Color.White, 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOwner) "You (Owner)" else book.owner, 
                                    color = Color.White.copy(alpha = 0.8f), 
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Swaps", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                            Text(text = book.swaps.toString(), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(
                        icon = if (book.isAvailable) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        text = if (book.isAvailable) "Available" else "Busy",
                        bgColor = if (book.isAvailable) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                    InfoChip(
                        icon = Icons.Default.Sell,
                        text = if (book.isForRent) "Rentable" else "Swap Only",
                        bgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                    RatingChip(
                        rating = book.averageRating ?: 0.0,
                        reviewCount = book.reviewCount,
                        onClick = { selectedTabIndex = 2 }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Column(
                            modifier = Modifier
                                .clickable { selectedTabIndex = index },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                                softWrap = false
                            )
                            if (selectedTabIndex == index) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(3.dp)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                )
                            } else {
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (selectedTabIndex) {
                    0 -> { // Overview
                        Text(
                            text = book.description, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.onSurface, 
                            lineHeight = 22.sp
                        )
                    }
                    1 -> { // Details
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DetailRow(label = "Author", value = book.author)
                            DetailRow(label = "Location", value = book.location)
                            DetailRow(label = "Category", value = book.category)
                            DetailRow(label = "Rental Price", value = if (book.isForRent) "$${book.rentalPricePerDay}/day" else "Not for rent")
                            DetailRow(label = "Book ID", value = "#${book.id}")
                        }
                    }
                    2 -> { // Reviews
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (reviews.isEmpty()) "No Reviews Yet" else "Customer Reviews",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (!isOwner) {
                                    val hasReviewed = reviews.any { it.userId == currentUserId }
                                    if (!hasReviewed) {
                                        TextButton(onClick = { showReviewDialog = true }) {
                                            Text("Write a Review", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text(
                                            "You've reviewed this",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                }
                            }
                            
                            if (reviews.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Be the first to review this book!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                reviews.forEach { review ->
                                    ReviewItem(review = review)
                                }
                            }
                        }
                    }
                    3 -> { // Edit Info Tab (Owner only)
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            BookSwapButton(
                                text = "Modify Listing Details",
                                onClick = onEditClick
                            )
                            OutlinedButton(
                                onClick = { showDeleteDialog = true }, 
                                modifier = Modifier.fillMaxWidth().height(56.dp), 
                                shape = RoundedCornerShape(16.dp), 
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.error), 
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error) 
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete This Book", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
