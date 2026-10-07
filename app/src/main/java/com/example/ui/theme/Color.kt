package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val KrishiHeaderGreen = Color(0xFF0C4624)
val KrishiPrimaryGreen = Color(0xFF1B8243)
val KrishiSecondaryGreen = Color(0xFF4CAF50)
val KrishiAccentOrange = Color(0xFFE65100)
val KrishiBackground = Color(0xFFF3F5F1)
val KrishiCardSurface = Color(0xFFFFFFFF)
val KrishiTextDark = Color(0xFF1B261F)
val KrishiTextMuted = Color(0xFF617065)

val LightColorScheme = lightColorScheme(
    primary = KrishiPrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF003914),
    secondary = KrishiSecondaryGreen,
    background = KrishiBackground,
    surface = KrishiCardSurface,
    onBackground = KrishiTextDark,
    onSurface = KrishiTextDark
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003914),
    secondary = Color(0xFFA5D6A7),
    background = Color(0xFF121B14),
    surface = Color(0xFF1A261D)
)
