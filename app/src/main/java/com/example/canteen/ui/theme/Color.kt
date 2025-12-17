package com.example.canteen.ui.theme

import androidx.compose.ui.graphics.Color

// These are the color definitions that Theme.kt needs.
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Green = Color(0xFFCEFDCB)
val lightRed = Color(0xFFE4002B)
val lightGreen =  Color(0xFF00B140) // green
val veryLightRed = Color(0xFFFFDFDF) // Light pink
val lightViolet = Color(0xFFE6EBFF) // violet-blue tint
val lightBlue = Color(0xFFE7F3FF) // very light blue
val softGreen = Color(0xFFDFF7E7) // soft green
val blue = Color(0xFF4A6CF7)
val black = Color(0x33000000)
val veryLightBlue = Color(0xFFE8EDFF)
val white = Color(0xFFFFFFFF)
val gray = Color(0xFFD3D3D3)
val middleGray = Color(0xFF949494)
val veryLightViolet = Color(0xFFEDE5F3)
val isVeryLightBlue = Color(0xFFF4F5FF)
val darkGray = Color(0xFF414141)


// Temp

object AppColors {
    // Primary Colors
    val primary = Color(0xFFFF6B35)        // Warm Orange - appetizing and energetic
    val primaryVariant = Color(0xFFE55A2B) // Darker orange for pressed states
    val primaryLight = Color(0xFFFF8C61)   // Lighter orange for subtle accents

    // Background Colors - Dark Matte Theme
    val background = Color(0xFF1A1A1A)     // Dark matte background (like Claude's interface)
    val surface = Color(0xFF2D2D2D)        // Elevated surface - cards and components
    val sheet = Color(0xFF242424)          // Bottom sheets and modals
    val surfaceVariant = Color(0xFF3A3A3A) // Alternative surface for variety

    // Text Colors - Adjusted for dark background
    val textPrimary = Color(0xFFE8E8E8)    // Light gray - high contrast on dark
    val textSecondary = Color(0xFFB0B0B0)  // Medium gray - subtle text
    val textTertiary = Color(0xFF808080)   // Dark gray - placeholder text

    // Accent Colors
    val success = Color(0xFF00B894)        // Green - success states
    val error = Color(0xFFD63031)          // Red - error states
    val warning = Color(0xFFFDCB6E)        // Yellow - warning states
    val info = Color(0xFF74B9FF)           // Blue - info states

    // Additional UI Colors
    val divider = Color(0xFF404040)        // Subtle divider for dark theme
    val disabled = Color(0xFF5A5A5A)       // Disabled state color
    val overlay = Color(0x80000000)        // Semi-transparent black for overlays
}