package com.example.bookswap

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatViewModel,
    currentUserId: String,
    onChatClick: (Long) -> Unit,
    onProfileClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onAddClick: () -> Unit,
    onProfileTabClick: () -> Unit,
    onBack: () -> Unit
) {
    val chatRequests = viewModel.chatRequests.filter { 
        it.status in listOf("accepted", "completed", "delivered", "rented") 
    }
    var showTopMenu by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }

    BookSwapScaffold(
        title = "Messages",
        showBack = true,
        onBack = onBack,
        actions = {
            Box {
                IconButton(onClick = { showTopMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.primary)
                }
                DropdownMenu(
                    expanded = showTopMenu,
                    onDismissRequest = { showTopMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Blocked Users") },
                        onClick = {
                            showTopMenu = false
                            showBlockedUsersDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                    )
                }
            }
        },
        bottomBar = {
            BookSwapNavigationBar {
                BookSwapNavItem(selected = false, onClick = onHomeClick, icon = Icons.Default.Home, label = "Home")
                BookSwapNavItem(selected = false, onClick = onExploreClick, icon = Icons.Default.Explore, label = "Explore")
                BookSwapNavItem(selected = false, onClick = onAddClick, icon = Icons.Default.AddCircle, label = "Add")
                BookSwapNavItem(selected = true, onClick = {}, icon = Icons.Default.Message, label = "Chats")
                BookSwapNavItem(selected = false, onClick = onProfileTabClick, icon = Icons.Default.Person, label = "Profile")
            }
        }
    ) { padding ->
        if (showBlockedUsersDialog) {
            BlockedUsersDialog(
                viewModel = viewModel,
                onDismiss = { showBlockedUsersDialog = false }
            )
        }

        if (chatRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(
                        Icons.Default.Forum, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp), 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "No conversations yet", 
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Accepted swap requests will appear here for you to chat.", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(chatRequests) { request ->
                    val isReceiver = request.receiverId == currentUserId
                    val otherPartyName = if (isReceiver) request.senderName else request.receiverName
                    val otherPartyAvatar = if (isReceiver) request.senderAvatar else request.receiverAvatar
                    val otherPartyId = if (isReceiver) request.senderId else request.receiverId
                    
                    var showMenu by remember { mutableStateOf(false) }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { request.id?.let { onChatClick(it) } }
                            .floatingElement(shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .clickable { otherPartyId?.let { onProfileClick(it) } },
                                contentAlignment = Alignment.Center
                            ) {
                                if (otherPartyAvatar != null) {
                                    AsyncImage(
                                        model = otherPartyAvatar,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = otherPartyName ?: "User",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                    Icon(
                                        Icons.Default.MenuBook, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(14.dp), 
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = request.bookTitle ?: "Book", 
                                        fontSize = 13.sp, 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Delete Chat") },
                                        onClick = {
                                            showMenu = false
                                            request.id?.let { viewModel.deleteChat(it) }
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Block User") },
                                        onClick = {
                                            showMenu = false
                                            otherPartyId?.let { viewModel.blockUser(it) }
                                        },
                                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMessagesScreen(
    viewModel: ChatViewModel,
    requestId: Long,
    currentUserId: String,
    onProfileClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages = viewModel.messages
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            viewModel.sendImageMessage(requestId, bitmap)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val request = viewModel.chatRequests.find { it.id == requestId }

    val otherPartyName = remember(request) {
        if (request?.receiverId == currentUserId) request.senderName else request?.receiverName
    }
    val otherPartyAvatar = remember(request) {
        if (request?.receiverId == currentUserId) request.senderAvatar else request?.receiverAvatar
    }
    val otherPartyId = remember(request) {
        if (request?.receiverId == currentUserId) request.senderId else request?.receiverId
    }

    LaunchedEffect(requestId) {
        viewModel.fetchMessages(requestId)
        viewModel.startListeningToMessages(requestId)
    }

    DisposableEffect(requestId) {
        onDispose {
            viewModel.stopListeningToMessages()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Column {
                Spacer(modifier = Modifier.statusBarsPadding())
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = otherPartyName ?: "Chat",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = {
                        otherPartyId?.let { id ->
                            Surface(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { onProfileClick(id) }
                                    .floatingElement(shape = CircleShape),
                                color = Color.Transparent
                            ) {
                                if (otherPartyAvatar != null) {
                                    AsyncImage(model = otherPartyAvatar, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.ime)
                    .navigationBarsPadding(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate, 
                            contentDescription = "Send Image", 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Type a message...", color = Color.White.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            maxLines = 5
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(enabled = messageText.isNotBlank()) {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(requestId, messageText)
                                    messageText = ""
                                }
                            }
                            .background(
                                if (messageText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Send, 
                                contentDescription = "Send", 
                                tint = if (messageText.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            reverseLayout = false,
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                if (message.messageType == "system" || message.senderId == "system") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = message.content,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    val isMe = message.senderId == currentUserId
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .floatingElement(
                                    shape = RoundedCornerShape(
                                        topStart = 20.dp,
                                        topEnd = 20.dp,
                                        bottomStart = if (isMe) 20.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 20.dp
                                    ),
                                    backgroundColor = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                                ),
                            color = Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                if (message.messageType == "image" && message.mediaUrl != null) {
                                    AsyncImage(
                                        model = message.mediaUrl,
                                        contentDescription = "Image message",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Text(
                                    text = message.content,
                                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                                    fontSize = 15.sp
                                )
                                if (message.createdAt != null) {
                                    Text(
                                        text = formatRelativeTime(message.createdAt),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.align(Alignment.End),
                                        fontWeight = FontWeight.Light
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedUsersDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val blockedUsers = viewModel.blockedUsers
    val loading by viewModel.loading

    LaunchedEffect(Unit) {
        viewModel.fetchBlockedUsers()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Blocked Users", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) },
        text = {
            Box(modifier = Modifier.sizeIn(maxHeight = 400.dp)) {
                if (loading && blockedUsers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
                    }
                } else if (blockedUsers.isEmpty()) {
                    Text("No blocked users yet.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(blockedUsers) { blocked ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (blocked.blockedAvatar != null) {
                                        AsyncImage(
                                            model = blocked.blockedAvatar,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = blocked.blockedName ?: "User",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (blocked.blockedUsername != null) {
                                        Text(text = "@${blocked.blockedUsername}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                TextButton(
                                    onClick = { blocked.id?.let { viewModel.unblockUser(it, blocked.blockedId) } }
                                ) {
                                    Text("Unblock", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}
