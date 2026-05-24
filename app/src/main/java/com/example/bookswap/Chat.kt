package com.example.bookswap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    var senderName: String? = null,
    var senderUsername: String? = null,
    var receiverName: String? = null,
    var receiverUsername: String? = null,
    var bookTitle: String? = null
)

@Serializable
data class Message(
    val id: Long? = null,
    @SerialName("chat_request_id")
    val chatRequestId: Long,
    @SerialName("sender_id")
    val senderId: String,
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
