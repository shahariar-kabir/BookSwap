package com.example.bookswap

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val postgrest = supabase.postgrest
    private val auth = supabase.auth
    private val realtime = supabase.realtime

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _chatRequests = mutableStateListOf<ChatRequest>()
    val chatRequests: List<ChatRequest> = _chatRequests

    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    private val _blockedUsers = mutableStateListOf<BlockedUser>()
    val blockedUsers: List<BlockedUser> = _blockedUsers

    private var messageSubscriptionJob: Job? = null
    private var requestSubscriptionJob: Job? = null

    fun startListeningToRequests(context: Context) {
        val currentUser = auth.currentUserOrNull() ?: return
        requestSubscriptionJob?.cancel()

        val channel = realtime.channel("incoming-requests")
        requestSubscriptionJob = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "chat_requests"
        }.onEach { action ->
            // Fast sync: refresh for any change in chat requests
            fetchChatRequests()

            if (action is PostgresAction.Insert) {
                val newRequest = action.decodeRecord<ChatRequest>()
                if (newRequest.receiverId == currentUser.id) {
                    // Fetch details to show in notification
                    viewModelScope.launch {
                        try {
                            val senderProfile = postgrest["profiles"].select {
                                filter { eq("id", newRequest.senderId) }
                            }.decodeSingle<Profile>()
                            
                            val book = postgrest["books"].select {
                                filter { eq("id", newRequest.bookId) }
                            }.decodeSingle<Book>()

                            NotificationHelper.showSwapRequestNotification(
                                context,
                                senderProfile.fullName,
                                book.title
                            )
                            fetchChatRequests() // Refresh list
                        } catch (e: Exception) {
                            println("Error fetching notification details: ${e.message}")
                        }
                    }
                }
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            channel.subscribe()
        }
    }

    fun sendChatRequest(receiverId: String, bookId: Long, type: String, onSuccess: () -> Unit) {
        val currentUser = auth.currentUserOrNull() ?: return
        
        _loading.value = true
        viewModelScope.launch {
            try {
                val request = ChatRequest(
                    senderId = currentUser.id,
                    receiverId = receiverId,
                    bookId = bookId,
                    type = type,
                    status = "pending"
                )
                postgrest["chat_requests"].insert(request)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to send request: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchChatRequests() {
        val currentUser = auth.currentUserOrNull() ?: return
        _loading.value = true
        viewModelScope.launch {
            try {
                val userId = currentUser.id
                val results = postgrest["chat_requests"].select {
                    filter {
                        or {
                            ChatRequest::senderId eq userId
                            ChatRequest::receiverId eq userId
                        }
                    }
                }.decodeList<ChatRequest>()

                if (results.isNotEmpty()) {
                    val userIds = (results.map { it.senderId } + results.map { it.receiverId }).distinct()
                    val profiles = postgrest["profiles"].select {
                        filter { isIn("id", userIds) }
                    }.decodeList<Profile>()

                    val bookIds = results.map { it.bookId }.distinct()
                    val books = postgrest["books"].select {
                        filter { isIn("id", bookIds) }
                    }.decodeList<Book>()

                    results.forEach { request ->
                        val senderProfile = profiles.find { it.id == request.senderId }
                        val receiverProfile = profiles.find { it.id == request.receiverId }

                        request.senderName = senderProfile?.fullName ?: senderProfile?.email ?: "Unknown User"
                        request.senderUsername = senderProfile?.username
                        request.senderAvatar = senderProfile?.avatarUrl
                        
                        request.receiverName = receiverProfile?.fullName ?: receiverProfile?.email ?: "Unknown Owner"
                        request.receiverUsername = receiverProfile?.username
                        request.receiverAvatar = receiverProfile?.avatarUrl

                        request.bookTitle = books.find { it.id == request.bookId }?.title ?: "Unknown Book"
                    }
                }

                _chatRequests.clear()
                _chatRequests.addAll(results.sortedByDescending { it.createdAt })
            } catch (e: Exception) {
                _error.value = "Failed to fetch chats: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateRequestStatus(requestId: Long, status: String) {
        viewModelScope.launch {
            try {
                postgrest["chat_requests"].update({
                    ChatRequest::status setTo status
                }) {
                    filter { ChatRequest::id eq requestId }
                }
                fetchChatRequests()
            } catch (e: Exception) {
                _error.value = "Failed to update status: ${e.message}"
            }
        }
    }

    fun sendMessage(chatRequestId: Long, content: String) {
        val currentUser = auth.currentUserOrNull() ?: return
        
        // Optimistic update
        val tempId = System.currentTimeMillis() * -1 // Temporary negative ID
        val newMessage = Message(
            id = tempId,
            chatRequestId = chatRequestId,
            senderId = currentUser.id,
            content = content,
            createdAt = null // Will be set by DB
        )
        _messages.add(newMessage)

        viewModelScope.launch {
            try {
                val message = Message(
                    chatRequestId = chatRequestId,
                    senderId = currentUser.id,
                    content = content
                )
                postgrest["messages"].insert(message)
                // We don't need to manually remove tempId because fetchMessages 
                // or the realtime listener will eventually refresh the list
            } catch (e: Exception) {
                _messages.remove(newMessage) // Rollback on error
                _error.value = "Failed to send message: ${e.message}"
            }
        }
    }

    fun startListeningToMessages(chatRequestId: Long) {
        fetchMessages(chatRequestId)
        messageSubscriptionJob?.cancel()

        val channel = realtime.channel("chat-$chatRequestId")
        messageSubscriptionJob = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }.onEach { action ->
            if (action is PostgresAction.Insert) {
                val newMessage = action.decodeRecord<Message>()
                if (newMessage.chatRequestId == chatRequestId) {
                    // Replace optimistic message if it exists or add new one
                    val existingIndex = _messages.indexOfFirst { it.content == newMessage.content && it.id != null && it.id < 0 }
                    if (existingIndex != -1) {
                        _messages[existingIndex] = newMessage
                    } else if (_messages.none { it.id == newMessage.id }) {
                        _messages.add(newMessage)
                    }
                }
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            channel.subscribe()
        }
    }

    fun stopListeningToMessages() {
        messageSubscriptionJob?.cancel()
        _messages.clear()
    }

    fun fetchMessages(chatRequestId: Long) {
        viewModelScope.launch {
            try {
                val results = postgrest["messages"].select {
                    filter { Message::chatRequestId eq chatRequestId }
                    order("created_at", Order.ASCENDING)
                }.decodeList<Message>()
                _messages.clear()
                _messages.addAll(results)
            } catch (e: Exception) {
                _error.value = "Failed to fetch messages: ${e.message}"
            }
        }
    }

    fun deleteChat(requestId: Long) {
        viewModelScope.launch {
            try {
                postgrest["chat_requests"].delete {
                    filter { eq("id", requestId) }
                }
                fetchChatRequests()
            } catch (e: Exception) {
                _error.value = "Failed to delete chat: ${e.message}"
            }
        }
    }

    fun blockUser(userId: String) {
        val currentUser = auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                // Instead of deleting, we "hide" the chats by changing status to 'blocked'
                postgrest["chat_requests"].update({
                    ChatRequest::status setTo "blocked"
                }) {
                    filter {
                        and {
                            eq("status", "accepted") // Only hide currently active chats
                            or {
                                and {
                                    eq("sender_id", currentUser.id)
                                    eq("receiver_id", userId)
                                }
                                and {
                                    eq("sender_id", userId)
                                    eq("receiver_id", currentUser.id)
                                }
                            }
                        }
                    }
                }
                
                // Add to blocked_users table to keep track
                postgrest["blocked_users"].insert(mapOf(
                    "blocker_id" to currentUser.id,
                    "blocked_id" to userId
                ))
                
                fetchChatRequests()
            } catch (e: Exception) {
                _error.value = "Failed to block user: ${e.message}"
            }
        }
    }

    fun unblockUser(blockId: Long, blockedUserId: String) {
        val currentUser = auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                // 1. Remove from blocked_users table
                postgrest["blocked_users"].delete {
                    filter { eq("id", blockId) }
                }

                // 2. Restore chats by changing status back to 'accepted'
                postgrest["chat_requests"].update({
                    ChatRequest::status setTo "accepted"
                }) {
                    filter {
                        and {
                            eq("status", "blocked")
                            or {
                                and {
                                    eq("sender_id", currentUser.id)
                                    eq("receiver_id", blockedUserId)
                                }
                                and {
                                    eq("sender_id", blockedUserId)
                                    eq("receiver_id", currentUser.id)
                                }
                            }
                        }
                    }
                }

                fetchBlockedUsers()
                fetchChatRequests()
            } catch (e: Exception) {
                _error.value = "Failed to unblock user: ${e.message}"
            }
        }
    }

    fun fetchBlockedUsers() {
        val currentUser = auth.currentUserOrNull() ?: return
        _loading.value = true
        viewModelScope.launch {
            try {
                val results = postgrest["blocked_users"].select {
                    filter { eq("blocker_id", currentUser.id) }
                }.decodeList<BlockedUser>()

                if (results.isNotEmpty()) {
                    val userIds = results.map { it.blockedId }.distinct()
                    val profiles = postgrest["profiles"].select {
                        filter { isIn("id", userIds) }
                    }.decodeList<Profile>()

                    results.forEach { blocked ->
                        val profile = profiles.find { it.id == blocked.blockedId }
                        blocked.blockedName = profile?.fullName ?: profile?.email ?: "Unknown User"
                        blocked.blockedUsername = profile?.username
                        blocked.blockedAvatar = profile?.avatarUrl
                    }
                }

                _blockedUsers.clear()
                _blockedUsers.addAll(results)
            } catch (e: Exception) {
                _error.value = "Failed to fetch blocked users: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
