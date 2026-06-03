package com.example.bookswap

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapCalls
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwapManagerScreen(
    viewModel: ChatViewModel,
    currentUserId: String,
    onChatClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val chatRequests = viewModel.chatRequests

    BookSwapScaffold(
        title = "Swap Manager",
        showBack = true,
        onBack = onBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (chatRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SwapCalls, 
                            contentDescription = null, 
                            modifier = Modifier.size(80.dp), 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No swap requests found", 
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(chatRequests) { request ->
                        SwapRequestItem(
                            request = request,
                            currentUserId = currentUserId,
                            onAccept = { request.id?.let { viewModel.updateRequestStatus(it, "accepted") } },
                            onReject = { request.id?.let { viewModel.updateRequestStatus(it, "rejected") } },
                            onChatClick = { request.id?.let { onChatClick(it) } },
                            onDelete = { request.id?.let { viewModel.deleteChat(it) } },
                            onComplete = { request.id?.let { viewModel.completeSwap(it) } },
                            onDeliver = { request.id?.let { viewModel.markAsDelivered(it) } },
                            onMarkAsRented = { request.id?.let { viewModel.markAsRented(it) } },
                            onUnmark = { request.id?.let { viewModel.unmarkStatus(it) } }
                        )
                    }
                }
            }
        }
    }
}
