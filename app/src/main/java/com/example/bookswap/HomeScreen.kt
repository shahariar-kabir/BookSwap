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

    BookSwapScaffold(
        bottomBar = {
            BookSwapNavigationBar {
                BookSwapNavItem(selected = true, onClick = {}, icon = Icons.Default.Home, label = "Home")
                BookSwapNavItem(selected = false, onClick = onExploreClick, icon = Icons.Default.Explore, label = "Explore")
                BookSwapNavItem(selected = false, onClick = onAddClick, icon = Icons.Default.AddCircle, label = "Add")
                BookSwapNavItem(selected = false, onClick = onChatClick, icon = Icons.Default.Message, label = "Chats")
                BookSwapNavItem(selected = false, onClick = onProfileClick, icon = Icons.Default.Person, label = "Profile")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome back,", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$userName!", 
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .floatingElement(shape = CircleShape),
                    shape = CircleShape,
                    color = Color.Transparent,
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
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.primary)
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
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
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
            
            Spacer(modifier = Modifier.height(100.dp)) // Added safety space
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, 
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onSeeAll, contentPadding = PaddingValues(start = 8.dp)) {
            Text(
                "See All", 
                color = MaterialTheme.colorScheme.primary, 
                style = MaterialTheme.typography.labelLarge,
                softWrap = false
            )
        }
    }
}

@Composable
fun ActiveSwapItem(request: ChatRequest, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(240.dp)
            .clickable { onClick() }
            .floatingElement(shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (request.status == "accepted") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (request.status == "accepted") Icons.Default.SwapHoriz else Icons.Default.Pending,
                    contentDescription = null,
                    tint = if (request.status == "accepted") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.bookTitle ?: "Book Swap",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (request.status == "accepted") "Ongoing Chat" else "Pending Request",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
