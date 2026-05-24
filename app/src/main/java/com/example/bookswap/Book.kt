package com.example.bookswap

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: Int? = null,
    val title: String,
    val owner: String,
    val rating: Double,
    val swaps: Int,
    val description: String,
    val isAvailable: Boolean = true,
    val isForRent: Boolean = false,
    val rentalPricePerDay: Double? = null
)

val sampleBooks = listOf(
    Book(
        id = 1,
        title = "The Alchemist",
        owner = "John Doe",
        rating = 4.8,
        swaps = 12,
        description = "A global phenomenon, The Alchemist has been read and loved by over 62 million readers, becoming one of the best-selling books in history.",
        isAvailable = true,
        isForRent = true,
        rentalPricePerDay = 2.5
    ),
    Book(
        id = 2,
        title = "Atomic Habits",
        owner = "Jane Smith",
        rating = 4.9,
        swaps = 8,
        description = "No matter your goals, Atomic Habits offers a proven framework for improving--every day.",
        isAvailable = false,
        isForRent = false
    ),
    Book(
        id = 3,
        title = "The Psychology of Money",
        owner = "Alice Johnson",
        rating = 4.7,
        swaps = 15,
        description = "Doing well with money isn’t necessarily about what you know. It’s about how you behave.",
        isAvailable = true,
        isForRent = true,
        rentalPricePerDay = 3.0
    )
)
