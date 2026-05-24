package com.example.bookswap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen() {
    val backgroundColor = Color(0xFFBB86FC) // Light blueish hue matching Home/Profile
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BookLogo()

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "BOOKSWAP",
                color = Color(0xFF2D2D2D), // Darker text for visibility on light background
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                letterSpacing = 2.sp
            )

            Text(
                text = "looking for books?",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun BookLogo() {
    Canvas(modifier = Modifier.size(120.dp)) {
        val w = size.width
        val h = size.height
        val bookH = h / 4.5f
        val spacing = 12.dp.toPx()

        val bookColors = listOf(
            Color(0xFFE57373), // Coral/Red to match Navigation accent
            Color(0xFF4FC3F7), // Light Blue
            Color(0xFF81D4FA)  // Lighter Blue
        )

        for (i in 0..2) {
            val y = i * (bookH + spacing)
            
            val path = Path().apply {
                moveTo(w * 0.15f, y + bookH * 0.2f)
                // Left top curve
                quadraticBezierTo(w * 0.15f, y, w * 0.25f, y)
                // Top edge
                lineTo(w * 0.85f, y)
                // Right top curve
                quadraticBezierTo(w * 0.95f, y, w * 0.95f, y + bookH * 0.2f)
                // Right edge
                lineTo(w * 0.95f, y + bookH * 0.8f)
                // Right bottom curve
                quadraticBezierTo(w * 0.95f, y + bookH, w * 0.85f, y + bookH)
                // Bottom edge
                lineTo(w * 0.15f, y + bookH)
                // Left edge back to start
                close()
            }

            // Draw book body with gradient
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(bookColors[i].copy(alpha = 0.8f), bookColors[i])
                )
            )

            // Draw bookmark for first two books
            if (i < 2) {
                drawPath(
                    path = Path().apply {
                        moveTo(w * 0.55f, y)
                        lineTo(w * 0.65f, y)
                        lineTo(w * 0.65f, y + bookH * 0.4f)
                        lineTo(w * 0.60f, y + bookH * 0.3f)
                        lineTo(w * 0.55f, y + bookH * 0.4f)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}
