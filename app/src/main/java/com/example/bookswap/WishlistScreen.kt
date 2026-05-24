package com.example.bookswap

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = false,
                    onClick = onHomeClick,
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
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wishlist",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                if (wishlistBooks.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFF3E5F5),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "${wishlistBooks.size} items",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                    }
                }
            }

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
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your wishlist is empty",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Save books you want to read later",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onExploreClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                        ) {
                            Text("Explore Books")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(wishlistBooks) { book ->
                        RecentBookRow(book = book, onClick = { onBookClick(book) })
                    }
                }
            }
        }
    }
}
