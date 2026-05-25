package com.example.bookswap

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: BookViewModel,
    onBookClick: (Book) -> Unit,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf(
        CategoryItem("Fiction", Icons.Default.AutoStories, Color(0xFFFFEBEE)),
        CategoryItem("Science", Icons.Default.Science, Color(0xFFE3F2FD)),
        CategoryItem("Business", Icons.Default.BusinessCenter, Color(0xFFE8F5E9)),
        CategoryItem("History", Icons.Default.HistoryEdu, Color(0xFFFFF3E0)),
        CategoryItem("Arts", Icons.Default.Palette, Color(0xFFF3E5F5)),
        CategoryItem("Novel", Icons.Default.MenuBook, Color(0xFFE0F7FA))
    )
    var selectedCategory by remember { mutableStateOf("All") }
    var availableOnly by remember { mutableStateOf(false) }

    val books = viewModel.books
    val favoriteBookIds = viewModel.favorites
    val windowSize = rememberWindowSize()

    val filteredBooks = books.filter { book ->
        val matchesSearch = searchQuery.isBlank() || 
            book.title.contains(searchQuery, ignoreCase = true) || 
            book.author.contains(searchQuery, ignoreCase = true) ||
            book.location.contains(searchQuery, ignoreCase = true)
        
        val matchesCategory = selectedCategory == "All" || book.category == selectedCategory
        val matchesAvailability = !availableOnly || book.isAvailable
        
        matchesSearch && matchesCategory && matchesAvailability
    }

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
                    selected = true,
                    onClick = { },
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
            // Explore Search Header
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Explore",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        placeholder = { Text("Search by title, author, or city...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null) }
                            }
                            // Filter icon removed for cleaner UI
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE3F2FD),
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedContainerColor = Color(0xFFF8F9FA)
                        )
                    )
                }
            }

            // Global Explore Area
            if (searchQuery.isEmpty() && selectedCategory == "All") {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Browse by Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categories) { category ->
                            CategoryTile(category = category) {
                                selectedCategory = category.name
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Trending Now", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Available", fontSize = 14.sp, color = Color.Gray)
                            Switch(checked = availableOnly, onCheckedChange = { availableOnly = it }, modifier = Modifier.scale(0.7f))
                        }
                    }

                    DiscoveryGrid(
                        books = filteredBooks,
                        favoriteIds = favoriteBookIds,
                        onBookClick = onBookClick,
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        windowSize = windowSize
                    )
                }
            } else {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedCategory == "All",
                            onClick = { selectedCategory = "All" },
                            label = { Text("All Results") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (selectedCategory != "All") {
                            InputChip(
                                selected = true,
                                onClick = { selectedCategory = "All" },
                                label = { Text(selectedCategory) },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                    
                    if (filteredBooks.isEmpty()) {
                        EmptySearchResults { 
                            searchQuery = ""
                            selectedCategory = "All"
                        }
                    } else {
                        DiscoveryGrid(
                            books = filteredBooks,
                            favoriteIds = favoriteBookIds,
                            onBookClick = onBookClick,
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            windowSize = windowSize
                        )
                    }
                }
            }
        }
    }
}

data class CategoryItem(val name: String, val icon: ImageVector, val color: Color)

@Composable
fun CategoryTile(category: CategoryItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(90.dp)
            .height(100.dp)
            .clickable { onClick() },
        color = category.color,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(category.icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.DarkGray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category.name, 
                fontWeight = FontWeight.Bold, 
                fontSize = 13.sp, 
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DiscoveryGrid(
    books: List<Book>,
    favoriteIds: List<Long>,
    onBookClick: (Book) -> Unit,
    onFavoriteToggle: (Long) -> Unit,
    windowSize: WindowSize
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 4 else 2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.heightIn(max = 2000.dp)
    ) {
        items(books) { book ->
            BookCard(
                book = book,
                isFavorite = favoriteIds.contains(book.id),
                onFavoriteToggle = { book.id?.let { onFavoriteToggle(it) } },
                onClick = { onBookClick(book) },
                backgroundColor = Color.White,
                windowSize = windowSize,
                width = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 165.dp else 220.dp,
                height = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 250.dp else 320.dp
            )
        }
    }
}

@Composable
fun EmptySearchResults(onClear: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No results found", color = Color.Gray, fontWeight = FontWeight.Medium)
            TextButton(onClick = onClear) { Text("Clear Search") }
        }
    }
}
