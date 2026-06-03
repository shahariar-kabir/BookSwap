package com.example.bookswap

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookswap.ui.theme.*

/**
 * A full-screen background wrapper that provides a soft, moving gradient.
 * Uses a palette of deep purples to warm oranges for depth and warmth.
 */
@Composable
fun FullScreenGlowWrapper(
    modifier: Modifier = Modifier,
    glowColors: List<Color> = listOf(
        OceanPearlWhite.copy(alpha = 0.5f),
        OceanDeepSea.copy(alpha = 0.3f),
        OceanSkyBlue.copy(alpha = 0.4f),
        OceanAquamarine.copy(alpha = 0.4f)
    ),
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlobalGlowTransition")
    
    // Refreshing and fluid movement for Ocean effect
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 1600f,
        targetValue = -600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowXOffset"
    )
    
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 1600f,
        targetValue = -600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowYOffset"
    )

    Box(modifier = modifier.fillMaxSize().background(OceanPearlWhite)) {
        // Moving Neon Glow Layer (Background)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = glowColors,
                    start = Offset(xOffset, yOffset),
                    end = Offset(xOffset + size.width * 1.8f, yOffset + size.height * 1.8f)
                ),
                alpha = 0.8f
            )
        }

        // Soft blur to keep it airy and clean
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
        )

        // Main App Content
        content()
    }
}

/**
 * A custom Jetpack Compose wrapper that creates a 'glowing edge' effect.
 * It layers a blurred, animated gradient behind an opaque foreground container.
 *
 * @param modifier The modifier to be applied to the outer Box.
 * @param glowColors The colors used in the sweep gradient for the glow effect.
 * @param backgroundColor The solid background color for the content layer.
 * @param glowBlurRadius The radius of the blur applied to the glow layer.
 * @param borderWidth The padding between the outer box and the content layer, revealing the glow.
 * @param animationDuration The duration of one full rotation of the glow gradient.
 * @param content The @Composable content to be displayed inside the glowing box.
 */
@Composable
fun AnimatedGlowBox(
    modifier: Modifier = Modifier,
    glowColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primary
    ),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    glowBlurRadius: Dp = 30.dp,
    borderWidth: Dp = 4.dp,
    animationDuration: Int = 3000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Bottom Layer (The Glow)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    rotationZ = rotation
                }
                .blur(radius = glowBlurRadius)
                .background(
                    brush = Brush.sweepGradient(glowColors)
                )
        )

        // Top Layer (The Content)
        Box(
            modifier = Modifier
                .padding(borderWidth)
                .background(backgroundColor)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FullScreenGlowWrapperPreview() {
    BookSwapTheme {
        FullScreenGlowWrapper {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(300.dp, 400.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("App Content with Glow", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun AnimatedGlowBoxDarkModePreview() {
    BookSwapTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedGlowBox(
                    modifier = Modifier.size(200.dp, 100.dp),
                    content = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DARK MODE",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 24.sp
                            )
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun AnimatedGlowBoxLightModePreview() {
    BookSwapTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedGlowBox(
                    modifier = Modifier.size(200.dp, 100.dp),
                    content = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LIGHT MODE",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 24.sp
                            )
                        }
                    }
                )
            }
        }
    }
}
