package com.example.bookswap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Random

@Composable
fun SplashScreen() {
    val windowSize = rememberWindowSize()
    
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFBA68C8), // Light Purple
            Color(0xFF9C27B0), // Purple
            Color(0xFF7B1FA2), // Dark Purple
            Color(0xFF4A148C)  // Deep Purple
        ),
        start = Offset(0f, 0f),
        end = Offset.Infinite
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        GlitterEffect()
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            val logoSize = when (windowSize.widthSizeClass) {
                WindowSizeClass.COMPACT -> 120.dp
                WindowSizeClass.MEDIUM -> 180.dp
                WindowSizeClass.EXPANDED -> 240.dp
            }

            BookLogo(size = logoSize)

            Spacer(modifier = Modifier.height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 16.dp else 32.dp))

            val titleFontSize = when (windowSize.widthSizeClass) {
                WindowSizeClass.COMPACT -> 40.sp
                WindowSizeClass.MEDIUM -> 56.sp
                WindowSizeClass.EXPANDED -> 72.sp
            }

            Text(
                text = "BOOKSWAP",
                color = Color.White,
                fontSize = titleFontSize,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                letterSpacing = 2.sp
            )

            Text(
                text = "looking for books?",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 16.sp else 20.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun GlitterEffect() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val random = Random(42) // Fixed seed for stable preview, remove for dynamic glitter
        repeat(150) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val radius = random.nextFloat() * 1.5.dp.toPx()
            val alpha = random.nextFloat() * 0.6f + 0.2f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun BookLogo(size: androidx.compose.ui.unit.Dp = 120.dp) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "Logo",
        modifier = Modifier.size(size)
    )
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}
