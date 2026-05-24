package com.example.bookswap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    COMPACT, MEDIUM, EXPANDED
}

data class WindowSize(
    val widthSizeClass: WindowSizeClass,
    val heightSizeClass: WindowSizeClass,
    val screenWidth: Dp,
    val screenHeight: Dp
)

@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    return remember(screenWidth, screenHeight) {
        WindowSize(
            widthSizeClass = when {
                screenWidth < 600.dp -> WindowSizeClass.COMPACT
                screenWidth < 840.dp -> WindowSizeClass.MEDIUM
                else -> WindowSizeClass.EXPANDED
            },
            heightSizeClass = when {
                screenHeight < 480.dp -> WindowSizeClass.COMPACT
                screenHeight < 900.dp -> WindowSizeClass.MEDIUM
                else -> WindowSizeClass.EXPANDED
            },
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }
}
