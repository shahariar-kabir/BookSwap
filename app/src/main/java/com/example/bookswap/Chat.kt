package com.example.bookswap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ChatRequest(
    val id: Long? = null,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("receiver_id")
    val receiverId: String,
    @SerialName("book_id")
    val bookId: Long,
    val type: String, // "swap" or "rent"
    val status: String = "pending",
    @SerialName("created_at")
    val createdAt: String? = null,
    
    // UI Helper fields (not saved in chat_requests table directly)
    @Transient var senderName: String? = null,
    @Transient var senderUsername: String? = null,
    @Transient var senderAvatar: String? = null,
    @Transient var receiverName: String? = null,
    @Transient var receiverUsername: String? = null,
    @Transient var receiverAvatar: String? = null,
    @Transient var bookTitle: String? = null,
    @Transient var lastMessage: String? = null,
    @Transient var lastMessageTime: String? = null,
    @Transient var unreadCount: Int = 0
)

@Serializable
data class Message(
    val id: Long? = null,
    @SerialName("chat_request_id")
    val chatRequestId: Long,
    @SerialName("sender_id")
    val senderId: String,
    val content: String,
    @SerialName("message_type")
    val messageType: String = "text", // "text", "image", "system"
    @SerialName("media_url")
    val mediaUrl: String? = null,
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class BlockedUser(
    val id: Long? = null,
    @SerialName("blocker_id")
    val blockerId: String,
    @SerialName("blocked_id")
    val blockedId: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    
    // UI Helper fields
    @Transient var blockedName: String? = null,
    @Transient var blockedUsername: String? = null,
    @Transient var blockedAvatar: String? = null
)
