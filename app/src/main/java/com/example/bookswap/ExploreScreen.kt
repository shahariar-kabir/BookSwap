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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

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

    BookSwapScaffold(
        title = "Explore",
        bottomBar = {
            BookSwapNavigationBar {
                BookSwapNavItem(selected = false, onClick = onHomeClick, icon = Icons.Default.Home, label = "Home")
                BookSwapNavItem(selected = true, onClick = {}, icon = Icons.Default.Explore, label = "Explore")
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
            // Search Bar
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                BookSwapTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search by title, author, city",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null) }
                        }
                    }
                )
            }

            // Global Explore Area
            if (searchQuery.isEmpty() && selectedCategory == "All") {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Browse by Category",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
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
                        Text(
                            "Trending Now", 
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Available", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Switch(
                                checked = availableOnly, 
                                onCheckedChange = { availableOnly = it }, 
                                modifier = Modifier.scale(0.7f),
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    DiscoveryGrid(
                        books = filteredBooks,
                        favoriteIds = favoriteBookIds,
                        onBookClick = onBookClick,
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        windowSize = windowSize
                    )
                    
                    Spacer(modifier = Modifier.height(100.dp)) // Extra space
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
                            label = { Text("All Results") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), selectedLabelColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (selectedCategory != "All") {
                            InputChip(
                                selected = true,
                                onClick = { selectedCategory = "All" },
                                label = { Text(selectedCategory) },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) },
                                colors = InputChipDefaults.inputChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.Black)
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
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) // Padding
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
            .width(80.dp)
            .height(90.dp)
            .clickable { onClick() }
            .floatingElement(shape = RoundedCornerShape(16.dp)),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                category.icon, 
                contentDescription = null, 
                modifier = Modifier.size(24.dp), 
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.name, 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onSurface,
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
                onClick = { book.id?.let { onBookClick(book) } },
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
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
            Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No results found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onClear) { Text("Clear Search", color = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
