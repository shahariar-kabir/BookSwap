package com.example.bookswap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BookDetailsScreen(
    book: Book,
    onBack: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (book.isAvailable) {
                    Button(
                        onClick = { /* Swap action */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Swap Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                if (book.isForRent && book.rentalPricePerDay != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { /* Rent action */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE57373))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Rent for $${book.rentalPricePerDay}/day",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE57373)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFFE57373))
                        }
                    }
                }
                
                if (!book.isAvailable && !book.isForRent) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.LightGray.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Book Not Available", color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(16.dp)
            ) {
                // Book Cover Placeholder
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(32.dp),
                    color = if (book.id == 1) Color(0xFFE57373) else Color(0xFFFFB74D)
                ) {
                    if (!book.isAvailable) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                            Surface(color = Color.White, shape = RoundedCornerShape(8.dp)) {
                                Text("NOT AVAILABLE", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Top Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White)
                    }
                    IconButton(
                        onClick = { /* Bookmark */ },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = Color.White)
                    }
                }

                // Book Info Overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = book.title,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4FC3F7),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = book.owner,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Swaps", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            Text(
                                text = book.swaps.toString(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row {
                    Text(
                        "Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(
                        "Details",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip(
                        icon = if (book.isAvailable) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        text = if (book.isAvailable) "Available" else "Busy",
                        bgColor = if (book.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                    InfoChip(
                        icon = Icons.Default.Sell,
                        text = if (book.isForRent) "Rentable" else "Swap Only",
                        bgColor = if (book.isForRent) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                    )
                    InfoChip(Icons.Default.Star, book.rating.toString(), Color(0xFFFFF8E1))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = book.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.DarkGray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        }
    }
}
