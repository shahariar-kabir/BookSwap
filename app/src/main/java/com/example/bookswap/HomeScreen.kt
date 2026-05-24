package com.example.bookswap

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    userPhotoUrl: String?,
    onBookClick: (Book) -> Unit,
    onExploreClick: () -> Unit,
    onAddClick: () -> Unit,
    onChatClick: () -> Unit,
    onChatRequestClick: (Long) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    bookViewModel: BookViewModel,
    chatViewModel: ChatViewModel
) {
    val windowSize = rememberWindowSize()
    
    val books = bookViewModel.books
    val favoriteBookIds = bookViewModel.favorites
    val chatRequests = chatViewModel.chatRequests
    
    val activeSwaps = chatRequests.filter { it.status == "pending" || it.status == "accepted" }
    
    val recommendedBooks = remember(books, favoriteBookIds) {
        val favoriteCategories = books.filter { it.id in favoriteBookIds }.map { it.category }.distinct()
        if (favoriteCategories.isEmpty()) {
            books.shuffled().take(5)
        } else {
            books.filter { it.category in favoriteCategories && it.id !in favoriteBookIds }.take(5)
                .ifEmpty { books.shuffled().take(5) }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onExploreClick,
                    icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                    label = { Text("Explore") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onAddClick,
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(32.dp)) },
                    label = { Text("Add") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onChatClick,
                    icon = { Icon(Icons.Default.Message, contentDescription = null) },
                    label = { Text("Chats") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
        ) {
            // Header with constraints
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 48.dp else 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Welcome back,", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        text = "$userName!", 
                        color = Color.Black, 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFFE3F2FD),
                    onClick = onProfileClick
                ) {
                    if (userPhotoUrl != null) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp), tint = Color(0xFF1976D2))
                    }
                }
            }

            // 1. Active Swaps Section
            if (activeSwaps.isNotEmpty()) {
                SectionHeader(title = "Active Swaps", onSeeAll = onChatClick)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeSwaps) { request ->
                        ActiveSwapItem(request = request, onClick = { request.id?.let { onChatRequestClick(it) } })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. Recommended for You
            SectionHeader(title = "Recommended for You", onSeeAll = onExploreClick)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(recommendedBooks) { book ->
                    BookCard(
                        book = book,
                        isFavorite = favoriteBookIds.contains(book.id),
                        onFavoriteToggle = { book.id?.let { bookViewModel.toggleFavorite(it) } },
                        onClick = { onBookClick(book) },
                        backgroundColor = Color.White,
                        windowSize = windowSize,
                        width = 160.dp,
                        height = 240.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. New Arrivals
            SectionHeader(title = "New Arrivals", onSeeAll = onExploreClick)
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                books.take(5).forEach { book ->
                    RecentBookRow(book = book, onClick = { onBookClick(book) })
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, 
            fontSize = 20.sp, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        TextButton(onClick = onSeeAll, contentPadding = PaddingValues(start = 8.dp)) {
            Text("See All", color = Color(0xFF1976D2), softWrap = false)
        }
    }
}

@Composable
fun ActiveSwapItem(request: ChatRequest, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (request.status == "accepted") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (request.status == "accepted") Icons.Default.SwapHoriz else Icons.Default.Pending,
                    contentDescription = null,
                    tint = if (request.status == "accepted") Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.bookTitle ?: "Book Swap",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (request.status == "accepted") "Ongoing Chat" else "Pending Request",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
