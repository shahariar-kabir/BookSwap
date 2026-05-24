package com.example.bookswap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String = "David",
    userPhotoUrl: String? = null,
    onBookClick: (Book) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val backgroundColor = Color(0xFFF5F9FF) // Light blueish hue

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                tonalElevation = 8.dp,
                color = backgroundColor
            ) {
                NavigationBar(
                    containerColor = backgroundColor,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE57373),
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.Gray
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { },
                        icon = { Icon(Icons.Outlined.History, contentDescription = null) },
                        colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { },
                        icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
                        colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onProfileClick,
                        icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height(24.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hi, $userName 👋",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Got Any Books To share?",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                    // Profile Image
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, CircleShape),
                        shape = CircleShape,
                        color = backgroundColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onProfileClick() }
                        ) {
                            if (userPhotoUrl != null) {
                                AsyncImage(
                                    model = userPhotoUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(30.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Search Bar with Shadow
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = backgroundColor
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search books", color = Color.Gray) },
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFFE57373),
                            unfocusedContainerColor = backgroundColor,
                            focusedContainerColor = backgroundColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Popular Books Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular Books",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    TextButton(onClick = { /* View all */ }) {
                        Text("View all", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryTab("Most Viewed", isSelected = true, backgroundColor)
                    CategoryTab("Latest", isSelected = false, backgroundColor)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Book List
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
                ) {
                    items(sampleBooks) { book ->
                        BookCard(book = book, onClick = { onBookClick(book) }, backgroundColor)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTab(text: String, isSelected: Boolean, backgroundColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF2D2D2D) else backgroundColor,
        modifier = Modifier
            .height(45.dp)
            .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(12.dp)),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)) else null
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BookCard(book: Book, onClick: () -> Unit, backgroundColor: Color) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(320.dp)
            .clickable { onClick() }
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Book Cover
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (book.id == 1) Color(0xFFE57373) else Color(0xFFFFB74D))
            )

            // Favorite Icon
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .size(36.dp)
                    .align(Alignment.TopEnd),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.2f)
            ) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Bottom Info Card
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = book.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF4FC3F7),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = book.owner,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = book.rating.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
