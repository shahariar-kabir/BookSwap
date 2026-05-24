package com.example.bookswap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookswap.ui.theme.CyanMain

@Composable
fun ProfileScreen(
    userName: String,
    profile: Profile?,
    booksCount: Int,
    swapsCount: Int,
    favoritesCount: Int,
    wishlistCount: Int,
    myBooks: List<Book> = emptyList(),
    favoriteBooks: List<Book> = emptyList(),
    onBookClick: (Book) -> Unit = {},
    onUpdateProfile: (String, String, String, String, Bitmap?) -> Unit = { _, _, _, _, _ -> },
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    onAddBookClick: () -> Unit = {},
    onSeeAllWishlist: () -> Unit = {},
    onSeeAllMyBooks: () -> Unit = {},
    onSeeAllFavorites: () -> Unit = {},
    onSwapRequestsClick: () -> Unit = {},
    onChatClick: (Long) -> Unit = {},
    isCurrentUser: Boolean = true,
    chatViewModel: ChatViewModel? = null,
    initialTabIndex: Int = 0,
    bookViewModel: BookViewModel? = null
) {
    val scrollState = rememberScrollState()
    val windowSize = rememberWindowSize()
    var showPersonalInfo by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    
    // Updated tabs: Wishlist instead of Swaps
    val tabs = if (isCurrentUser) listOf("My Books", "Favorites", "Wishlist") else listOf("Books")

    val chatRequests = chatViewModel?.chatRequests ?: emptyList()
    val pendingRequestsCount = remember(chatRequests) {
        chatRequests.count { it.status == "pending" && it.receiverId == profile?.id }
    }
    
    val wishlistBooks = remember(bookViewModel?.books, bookViewModel?.wishlist) {
        val ids = bookViewModel?.wishlist ?: emptyList()
        bookViewModel?.books?.filter { it.id in ids } ?: emptyList()
    }

    if (showEditDialog && profile != null && isCurrentUser) {
        var fullName by remember { mutableStateOf(profile.fullName) }
        var username by remember { mutableStateOf(profile.username ?: "") }
        var phone by remember { mutableStateOf(profile.phone ?: "") }
        var address by remember { mutableStateOf(profile.address ?: "") }
        var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
        val context = LocalContext.current

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                imageBitmap = BitmapFactory.decodeStream(inputStream)
            }
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = imageBitmap!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (profile.avatarUrl != null) {
                            AsyncImage(
                                model = profile.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
                        }
                    }
                    Text("Change Photo", fontSize = 12.sp, color = CyanMain)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Text("@", modifier = Modifier.padding(start = 12.dp), color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(fullName, username, phone, address, imageBitmap)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanMain)
                ) {
                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(scrollState)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    if (isCurrentUser) {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Outlined.Logout, contentDescription = "Logout", tint = Color.Red)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 100.dp else 140.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile?.avatarUrl != null) {
                        AsyncImage(
                            model = profile.avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 60.dp else 80.dp),
                            tint = Color(0xFF1976D2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile?.fullName ?: userName,
                    fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 24.sp else 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profile?.username?.let { "@$it" } ?: "Book Enthusiast",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        // Stat Cards Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                count = booksCount.toString(), 
                label = "Books", 
                icon = Icons.Default.MenuBook, 
                color = Color(0xFFE8F5E9), 
                contentColor = Color(0xFF2E7D32),
                onClick = { selectedTabIndex = 0 }
            )
            if (isCurrentUser) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    count = favoritesCount.toString(), 
                    label = "Favorites", 
                    icon = Icons.Default.Favorite, 
                    color = Color(0xFFFFEBEE), 
                    contentColor = Color(0xFFD32F2F),
                    onClick = { selectedTabIndex = 1 }
                )
            }
            StatCard(
                modifier = Modifier.weight(1f),
                count = swapsCount.toString(), 
                label = "Swaps", 
                icon = Icons.Default.SwapHoriz, 
                color = Color(0xFFE3F2FD), 
                contentColor = Color(0xFF1976D2)
            )
            if (isCurrentUser) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    count = wishlistCount.toString(),
                    label = "Wishlist", 
                    icon = Icons.Default.Bookmark, 
                    color = Color(0xFFF3E5F5), 
                    contentColor = Color(0xFF7B1FA2),
                    onClick = { selectedTabIndex = 2 }
                )
            }
        }

        // Tab Selection
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2),
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }

        // Tab Content Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 200.dp)
        ) {
            when (selectedTabIndex) {
                0 -> { // My Books
                    TabHeader(
                        title = if (isCurrentUser) "My Collection" else "Books",
                        onSeeAll = if (myBooks.isNotEmpty()) onSeeAllMyBooks else null
                    )
                    if (myBooks.isEmpty()) {
                        EmptyTabContent(message = if (isCurrentUser) "You haven't listed any books yet." else "No books listed.")
                    } else {
                        myBooks.take(5).forEach { book ->
                            RecentBookRow(book = book, onClick = { onBookClick(book) })
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                1 -> { // Favorites
                    if (isCurrentUser) {
                        TabHeader(
                            title = "Favorite Books",
                            onSeeAll = if (favoriteBooks.isNotEmpty()) onSeeAllFavorites else null
                        )
                        if (favoriteBooks.isEmpty()) {
                            EmptyTabContent(message = "No favorite books yet.")
                        } else {
                            favoriteBooks.take(5).forEach { book ->
                                RecentBookRow(book = book, onClick = { onBookClick(book) })
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    } else {
                        EmptyTabContent(message = "No public swap history.")
                    }
                }
                2 -> { // Wishlist
                    if (isCurrentUser) {
                        TabHeader(
                            title = "My Wishlist",
                            onSeeAll = if (wishlistBooks.isNotEmpty()) onSeeAllWishlist else null
                        )
                        if (wishlistBooks.isEmpty()) {
                            EmptyTabContent(message = "No books in wishlist.")
                        } else {
                            wishlistBooks.take(5).forEach { book ->
                                RecentBookRow(book = book, onClick = { onBookClick(book) })
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }

        // Settings & Actions
        if (isCurrentUser) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Settings & Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ProfileActionItem(
                    icon = Icons.Default.Badge,
                    title = "Personal Information",
                    subtitle = if (showPersonalInfo) "Hide details" else "View contact details",
                    onClick = { showPersonalInfo = !showPersonalInfo },
                    containerColor = Color(0xFFE1F5FE),
                    iconColor = Color(0xFF0288D1),
                    trailingIcon = if (showPersonalInfo) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                )

                AnimatedVisibility(
                    visible = showPersonalInfo,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoItem(icon = Icons.Default.Email, label = "Email", value = profile?.email ?: "N/A")
                        InfoItem(icon = Icons.Default.AlternateEmail, label = "Username", value = profile?.username?.let { "@$it" } ?: "N/A")
                        InfoItem(icon = Icons.Default.Phone, label = "Phone", value = profile?.phone ?: "N/A")
                        InfoItem(icon = Icons.Default.Home, label = "Address", value = profile?.address ?: "N/A")
                        
                        Button(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Profile Information")
                        }
                    }
                }

                ProfileActionItem(
                    icon = Icons.Default.Add,
                    title = "List a New Book",
                    subtitle = "Share your collection with the community",
                    onClick = onAddBookClick,
                    containerColor = Color(0xFFF3E5F5),
                    iconColor = Color(0xFF7B1FA2)
                )

                // Swap Manager - Under List a New Book
                ProfileActionItem(
                    icon = Icons.Default.SwapCalls,
                    title = "Swap Manager",
                    subtitle = "Manage all incoming and outgoing requests",
                    onClick = onSwapRequestsClick,
                    containerColor = Color(0xFFFFF3E0),
                    iconColor = Color(0xFFFF9800),
                    badgeCount = if (pendingRequestsCount > 0) pendingRequestsCount else null
                )

                ProfileActionItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    subtitle = "Sign out of your session",
                    onClick = onLogout,
                    containerColor = Color(0xFFFFEBEE),
                    iconColor = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun TabHeader(title: String, onSeeAll: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
        if (onSeeAll != null) {
            Text(
                text = "See All", 
                color = CyanMain, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }
    }
}

@Composable
fun EmptyTabContent(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, fontSize = 12.sp, color = Color.Gray)
                Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    count: String,
    label: String,
    icon: ImageVector,
    color: Color,
    contentColor: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count, 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label, 
                fontSize = 10.sp, 
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfileActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    containerColor: Color,
    iconColor: Color,
    trailingIcon: ImageVector = Icons.Default.ChevronRight,
    badgeCount: Int? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (badgeCount != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color.Red,
                            shape = CircleShape,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Text(
                                text = badgeCount.toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.wrapContentHeight()
                            )
                        }
                    }
                }
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(trailingIcon, contentDescription = null, tint = Color.LightGray)
        }
    }
}
