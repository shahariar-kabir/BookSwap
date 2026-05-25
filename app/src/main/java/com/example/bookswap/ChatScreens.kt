package com.example.bookswap

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
    onBack: () -> Unit
) {
    // Only show ACCEPTED requests as "Messages"
    val chatRequests = viewModel.chatRequests.filter { it.status == "accepted" }
    var showTopMenu by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showTopMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
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
                }
            )
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No active conversations", color = Color.Gray)
                    Text("Accepted swap requests will appear here", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatRequests) { request ->
                    val isReceiver = request.receiverId == currentUserId
                    val otherPartyName = if (isReceiver) request.senderName else request.receiverName
                    val otherPartyAvatar = if (isReceiver) request.senderAvatar else request.receiverAvatar
                    val otherPartyId = if (isReceiver) request.senderId else request.receiverId
                    
                    var showMenu by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { 
                            request.id?.let { onChatClick(it) }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD))
                                    .clickable { otherPartyId.let { onProfileClick(it) } },
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
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1976D2))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = otherPartyName ?: "User",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = request.bookTitle ?: "Book", 
                                        fontSize = 13.sp, 
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.LightGray)
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

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val request = viewModel.chatRequests.find { it.id == requestId }

    val otherPartyName = remember(request) {
        if (request?.receiverId == currentUserId) request.senderName else request?.receiverName
    }
    val otherPartyUsername = remember(request) {
        if (request?.receiverId == currentUserId) request.senderUsername else request?.receiverUsername
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
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        modifier = Modifier
                            .clickable { otherPartyId?.let { onProfileClick(it) } }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE3F2FD)),
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
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color(0xFF1976D2))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(otherPartyName ?: "Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                if (otherPartyUsername != null) {
                                    Text(
                                        text = " @$otherPartyUsername",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                            request?.bookTitle?.let { 
                                Text(
                                    text = "Swap: $it", 
                                    fontSize = 12.sp, 
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                ) 
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier.padding(16.dp).navigationBarsPadding().fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(requestId, messageText)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF1976D2))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            reverseLayout = false,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                val isMe = message.senderId == currentUserId
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (isMe) Color(0xFF1976D2) else Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 0.dp,
                            bottomEnd = if (isMe) 0.dp else 16.dp
                        )
                    ) {
                        Text(
                            text = message.content,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (isMe) Color.White else Color.Black
                        )
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
        title = { Text("Blocked Users", fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.sizeIn(maxHeight = 400.dp)) {
                if (loading && blockedUsers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else if (blockedUsers.isEmpty()) {
                    Text("No blocked users", modifier = Modifier.padding(16.dp), color = Color.Gray)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(blockedUsers) { blocked ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF0F0F0)),
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
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = blocked.blockedName ?: "User",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (blocked.blockedUsername != null) {
                                        Text(text = "@${blocked.blockedUsername}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                TextButton(
                                    onClick = { blocked.id?.let { viewModel.unblockUser(it, blocked.blockedId) } }
                                ) {
                                    Text("Unblock", color = Color(0xFF1976D2))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
