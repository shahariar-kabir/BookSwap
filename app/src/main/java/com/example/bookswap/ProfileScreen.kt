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
import coil.compose.AsyncImage

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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("Change Photo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(8.dp))

                    BookSwapTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name"
                    )
                    BookSwapTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username",
                        leadingIcon = { Text("@", modifier = Modifier.padding(start = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                    BookSwapTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number"
                    )
                    BookSwapTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Address"
                    )
                }
            },
            confirmButton = {
                BookSwapButton(
                    text = "Save",
                    onClick = {
                        onUpdateProfile(fullName, username, phone, address, imageBitmap)
                        showEditDialog = false
                    },
                    modifier = Modifier.width(100.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    BookSwapScaffold(
        title = if (isCurrentUser) "My Profile" else "User Profile",
        showBack = true,
        onBack = onBack,
        actions = {
            if (isCurrentUser) {
                IconButton(onClick = onLogout) {
                    Icon(Icons.Outlined.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
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
            // Profile Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 100.dp else 140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
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
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile?.fullName ?: userName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profile?.username?.let { "@$it" } ?: "Book Enthusiast",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stat Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    count = booksCount.toString(), 
                    label = "Books", 
                    icon = Icons.Default.MenuBook, 
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                    contentColor = MaterialTheme.colorScheme.primary,
                    onClick = { selectedTabIndex = 0 }
                )
                if (isCurrentUser) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = favoritesCount.toString(), 
                        label = "Favorites", 
                        icon = Icons.Default.Favorite, 
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), 
                        contentColor = MaterialTheme.colorScheme.secondary,
                        onClick = { selectedTabIndex = 1 }
                    )
                }
                StatCard(
                    modifier = Modifier.weight(1f),
                    count = swapsCount.toString(), 
                    label = "Swaps", 
                    icon = Icons.Default.SwapHoriz, 
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                    contentColor = MaterialTheme.colorScheme.primary
                )
                if (isCurrentUser) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = wishlistCount.toString(),
                        label = "Wishlist", 
                        icon = Icons.Default.Bookmark, 
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), 
                        contentColor = MaterialTheme.colorScheme.secondary,
                        onClick = { selectedTabIndex = 2 }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Selection
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
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
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    ProfileActionItem(
                        icon = Icons.Default.Badge,
                        title = "Personal Information",
                        subtitle = if (showPersonalInfo) "Hide details" else "View contact details",
                        onClick = { showPersonalInfo = !showPersonalInfo },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconColor = MaterialTheme.colorScheme.primary,
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
                            
                            BookSwapButton(
                                text = "Edit Profile Info",
                                onClick = { showEditDialog = true },
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    ProfileActionItem(
                        icon = Icons.Default.Add,
                        title = "List a New Book",
                        subtitle = "Share your collection with the community",
                        onClick = onAddBookClick,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconColor = MaterialTheme.colorScheme.primary
                    )

                    ProfileActionItem(
                        icon = Icons.Default.SwapCalls,
                        title = "Swap Manager",
                        subtitle = "Manage all incoming and outgoing requests",
                        onClick = onSwapRequestsClick,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconColor = MaterialTheme.colorScheme.primary,
                        badgeCount = if (pendingRequestsCount > 0) pendingRequestsCount else null
                    )

                    ProfileActionItem(
                        icon = Icons.Default.Logout,
                        title = "Logout",
                        subtitle = "Sign out of your session",
                        onClick = onLogout,
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        iconColor = MaterialTheme.colorScheme.error
                    )
                }
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
        Text(
            text = title, 
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onBackground
        )
        if (onSeeAll != null) {
            Text(
                text = "See All", 
                color = MaterialTheme.colorScheme.primary, 
                style = MaterialTheme.typography.labelLarge, 
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
        Text(
            text = message, 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .floatingElement(shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
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
            .clickable { onClick() }
            .floatingElement(shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent
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
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count, 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold, 
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            .clickable { onClick() }
            .floatingElement(shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title, 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeCount != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Text(
                                text = badgeCount.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.wrapContentHeight()
                            )
                        }
                    }
                }
                Text(
                    text = subtitle, 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(trailingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}
