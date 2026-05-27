package com.example.bookswap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class BookViewModel : ViewModel() {
    private val postgrest = supabase.postgrest
    private val auth = supabase.auth
    private val storage = supabase.storage
    private val realtime = supabase.realtime

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _books = mutableStateListOf<Book>()
    val books: List<Book> = _books

    private val _favorites = mutableStateListOf<Long>()
    val favorites: List<Long> = _favorites

    private val _wishlist = mutableStateListOf<Long>()
    val wishlist: List<Long> = _wishlist

    private val _bookReviews = mutableStateListOf<BookReview>()
    val bookReviews: List<BookReview> = _bookReviews

    init {
        fetchBooks()
        subscribeToBooks()
    }

    private fun subscribeToBooks() {
        val channel = realtime.channel("books-realtime")
        
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "books"
        }.onEach { action ->
            when (action) {
                is PostgresAction.Insert -> {
                    val newBook = action.decodeRecord<Book>()
                    if (_books.none { it.id == newBook.id }) {
                        _books.add(0, newBook)
                    }
                }
                is PostgresAction.Update -> {
                    val updatedBook = action.decodeRecord<Book>()
                    val index = _books.indexOfFirst { it.id == updatedBook.id }
                    if (index != -1) {
                        _books[index] = updatedBook
                    }
                }
                is PostgresAction.Delete -> {
                    val deletedId = action.oldRecord["id"]?.toString()?.toLongOrNull()
                    _books.removeAll { it.id == deletedId }
                }
                else -> {}
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            try {
                channel.subscribe()
            } catch (e: Exception) {
                println("Realtime subscription error: ${e.message}")
            }
        }
    }

    fun fetchBooks() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val results = postgrest["books"]
                    .select() {
                        order("id", Order.DESCENDING)
                    }
                    .decodeList<Book>()

                // Fetch average ratings and review counts for all books
                val allReviews = try {
                    postgrest["book_reviews"].select().decodeList<BookReview>()
                } catch (e: Exception) {
                    emptyList<BookReview>()
                }

                results.forEach { book ->
                    val bookReviews = allReviews.filter { it.bookId == book.id }
                    if (bookReviews.isNotEmpty()) {
                        book.averageRating = bookReviews.map { it.rating }.average()
                        book.reviewCount = bookReviews.size
                    } else {
                        book.averageRating = 0.0
                        book.reviewCount = 0
                    }
                }

                _books.clear()
                _books.addAll(results)
                
                fetchFavorites()
                fetchWishlist()
            } catch (e: Exception) {
                _error.value = "Failed to fetch books: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchFavorites() {
        val user = auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                val results = postgrest["favorites"]
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<Favorite>()
                _favorites.clear()
                _favorites.addAll(results.map { it.bookId })
            } catch (e: Exception) {
                println("Failed to fetch favorites: ${e.message}")
            }
        }
    }

    fun fetchWishlist() {
        val user = auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                val results = postgrest["wishlist"]
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<WishlistItem>()
                _wishlist.clear()
                _wishlist.addAll(results.map { it.bookId })
            } catch (e: Exception) {
                println("Failed to fetch wishlist: ${e.message}")
            }
        }
    }

    fun toggleFavorite(bookId: Long) {
        val user = auth.currentUserOrNull() ?: return
        
        viewModelScope.launch {
            try {
                if (_favorites.contains(bookId)) {
                    postgrest["favorites"].delete {
                        filter {
                            eq("user_id", user.id)
                            eq("book_id", bookId)
                        }
                    }
                    _favorites.remove(bookId)
                } else {
                    val fav = Favorite(userId = user.id, bookId = bookId)
                    postgrest["favorites"].insert(fav)
                    _favorites.add(bookId)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update favorite: ${e.message}"
            }
        }
    }

    fun toggleWishlist(bookId: Long) {
        val user = auth.currentUserOrNull() ?: return
        
        viewModelScope.launch {
            try {
                if (_wishlist.contains(bookId)) {
                    postgrest["wishlist"].delete {
                        filter {
                            eq("user_id", user.id)
                            eq("book_id", bookId)
                        }
                    }
                    _wishlist.remove(bookId)
                } else {
                    val item = WishlistItem(userId = user.id, bookId = bookId)
                    postgrest["wishlist"].insert(item)
                    _wishlist.add(bookId)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update wishlist: ${e.message}"
            }
        }
    }

    fun updateBookAvailability(bookId: Long, isAvailable: Boolean) {
        // INSTANT LOCAL UPDATE
        val index = _books.indexOfFirst { it.id == bookId }
        if (index != -1) {
            _books[index] = _books[index].copy(isAvailable = isAvailable)
        }

        viewModelScope.launch {
            try {
                postgrest["books"].update({
                    Book::isAvailable setTo isAvailable
                }) {
                    filter {
                        eq("id", bookId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update availability: ${e.message}"
                fetchBooks() // Revert only on error
            }
        }
    }

    fun rateBook(bookId: Long, newRating: Double) {
        // Optimistic update
        val index = _books.indexOfFirst { it.id == bookId }
        if (index != -1) {
            _books[index] = _books[index].copy(rating = newRating)
        }

        viewModelScope.launch {
            try {
                postgrest["books"].update({
                    Book::rating setTo newRating
                }) {
                    filter {
                        eq("id", bookId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to rate book: ${e.message}"
                fetchBooks()
            }
        }
    }

    fun deleteBook(bookId: Long, onSuccess: () -> Unit) {
        // INSTANT LOCAL REMOVAL
        _books.removeAll { it.id == bookId }
        onSuccess() // Navigate away INSTANTLY

        viewModelScope.launch {
            try {
                postgrest["books"].delete {
                    filter {
                        eq("id", bookId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete book: ${e.message}"
                fetchBooks() // Restore if delete actually failed
            }
        }
    }

    fun submitReview(bookId: Long, rating: Int, comment: String, onSuccess: () -> Unit) {
        val currentUser = auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                val review = BookReview(
                    bookId = bookId,
                    userId = currentUser.id,
                    rating = rating,
                    comment = comment
                )
                postgrest["book_reviews"].insert(review)
                fetchBooks() // Refresh average ratings
                fetchBookReviews(bookId)
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = e.message ?: ""
                if (errorMessage.contains("duplicate key") || errorMessage.contains("23505")) {
                    _error.value = "You have already reviewed this book. You can only leave one review per book."
                } else {
                    _error.value = "Failed to submit review: ${e.message}"
                }
            }
        }
    }

    fun fetchBookReviews(bookId: Long) {
        viewModelScope.launch {
            try {
                val reviews = postgrest["book_reviews"].select {
                    filter { eq("book_id", bookId) }
                }.decodeList<BookReview>()
                
                if (reviews.isNotEmpty()) {
                    val userIds = reviews.map { it.userId }.distinct()
                    val profiles = postgrest["profiles"].select {
                        filter { isIn("id", userIds) }
                    }.decodeList<Profile>()
                    
                    reviews.forEach { review ->
                        val profile = profiles.find { it.id == review.userId }
                        review.reviewerName = profile?.fullName ?: profile?.email ?: "Anonymous"
                    }
                }
                
                _bookReviews.clear()
                _bookReviews.addAll(reviews.sortedByDescending { it.createdAt })
            } catch (e: Exception) {
                _error.value = "Failed to fetch reviews: ${e.message}"
            }
        }
    }

    private suspend fun compressImage(bitmap: Bitmap): ByteArray = withContext(Dispatchers.Default) {
        var quality = 80
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        var compressedBytes = outputStream.toByteArray()
        
        while (compressedBytes.size > 300 * 1024 && quality > 10) {
            quality -= 10
            val loopStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, loopStream)
            compressedBytes = loopStream.toByteArray()
        }
        compressedBytes
    }

    fun addBook(
        title: String,
        author: String,
        description: String,
        category: String,
        location: String,
        isForRent: Boolean,
        rentalPrice: Double?,
        imageBitmap: Bitmap?,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || description.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }

        val currentUser = auth.currentUserOrNull() ?: return

        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                var imageUrl: String? = null
                if (imageBitmap != null) {
                    val finalImageBytes = compressImage(imageBitmap)
                    val fileName = "${UUID.randomUUID()}.jpg"
                    val bucket = storage.from("book-images")
                    bucket.upload(fileName, finalImageBytes)
                    imageUrl = bucket.publicUrl(fileName)
                }

                val fullName = currentUser.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "Unknown"
                val username = currentUser.userMetadata?.get("username")?.toString()?.replace("\"", "") ?: fullName
                
                val book = Book(
                    title = title,
                    author = author,
                    description = description,
                    owner = fullName,
                    ownerUsername = username,
                    ownerId = currentUser.id,
                    rating = 0.0,
                    swaps = 0,
                    isAvailable = true,
                    isForRent = isForRent,
                    rentalPricePerDay = if (isForRent) rentalPrice else null,
                    imageUrl = imageUrl,
                    category = category,
                    location = location
                )

                postgrest["books"].insert(book)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add book"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateBook(
        bookId: Long,
        title: String,
        author: String,
        description: String,
        category: String,
        location: String,
        isForRent: Boolean,
        isAvailable: Boolean,
        rentalPrice: Double?,
        imageBitmap: Bitmap?,
        existingImageUrl: String?,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || description.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }

        _loading.value = true
        _error.value = null

        // OPTIMISTIC LOCAL UPDATE
        val index = _books.indexOfFirst { it.id == bookId }
        if (index != -1) {
            _books[index] = _books[index].copy(
                title = title,
                author = author,
                description = description,
                category = category,
                location = location,
                isForRent = isForRent,
                isAvailable = isAvailable,
                rentalPricePerDay = if (isForRent) rentalPrice else null
            )
        }

        viewModelScope.launch {
            try {
                var finalImageUrl = existingImageUrl

                if (imageBitmap != null) {
                    val compressedBytes = compressImage(imageBitmap)
                    val fileName = "${UUID.randomUUID()}.jpg"
                    val bucket = storage.from("book-images")
                    bucket.upload(fileName, compressedBytes)
                    finalImageUrl = bucket.publicUrl(fileName)
                }

                postgrest["books"].update({
                    Book::title setTo title
                    Book::author setTo author
                    Book::description setTo description
                    Book::category setTo category
                    Book::location setTo location
                    Book::isForRent setTo isForRent
                    Book::isAvailable setTo isAvailable
                    Book::rentalPricePerDay setTo (if (isForRent) rentalPrice else null)
                    Book::imageUrl setTo finalImageUrl
                }) {
                    filter {
                        eq("id", bookId)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update book"
                fetchBooks() // Restore on error
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
