package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryCyan,
    secondary = SecondaryRed,
    tertiary = ChampionshipGold,
    background = DarkMidnight,
    surface = SurfaceCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = Color(0xFFCAC4D0),
    surfaceVariant = SurfaceHover,
    onSurfaceVariant = TextSecondary
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CyberCyan40,
    secondary = SecondaryRed,
    tertiary = ChampionshipGold,
    background = DarkMidnight,
    surface = SurfaceCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = Color(0xFFCAC4D0),
    surfaceVariant = SurfaceHover,
    onSurfaceVariant = TextSecondary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark theme for premium sports broadcasting feels
  dynamicColor: Boolean = false, // Preserve brand identity
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
