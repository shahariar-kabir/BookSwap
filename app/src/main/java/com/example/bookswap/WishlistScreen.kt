package com.example.bookswap

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WishlistScreen(
    viewModel: BookViewModel,
    onBookClick: (Book) -> Unit,
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onAddClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val wishlistIds = viewModel.wishlist
    val allBooks = viewModel.books
    val wishlistBooks = allBooks.filter { wishlistIds.contains(it.id) }

    BookSwapScaffold(
        title = "Wishlist",
        bottomBar = {
            BookSwapNavigationBar {
                BookSwapNavItem(selected = false, onClick = onHomeClick, icon = Icons.Default.Home, label = "Home")
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
        ) {
            if (wishlistBooks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your wishlist is empty",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Save books you want to read later",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        BookSwapButton(
                            text = "Explore Books",
                            onClick = onExploreClick,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(wishlistBooks) { book ->
                        RecentBookRow(book = book, onClick = { onBookClick(book) })
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(100.dp))
            }
        }
    }
}
