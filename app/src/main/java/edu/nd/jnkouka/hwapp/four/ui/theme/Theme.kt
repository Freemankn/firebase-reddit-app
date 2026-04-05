package edu.nd.jnkouka.hwapp.four.ui.theme

import android.app.Activity
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

private val LightColors = lightColorScheme(
    primary = RedditOrange,
    onPrimary = LightSurface,
    primaryContainer = RedditOrangeLight,
    onPrimaryContainer = LightTextPrimary,

    secondary = RedditBlue,
    onSecondary = LightSurface,
    secondaryContainer = Color(0xFFE8E8FF),
    onSecondaryContainer = LightTextPrimary,

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,

    surfaceVariant = Color(0xFFF2F4F5),
    onSurfaceVariant = LightTextSecondary,

    outline = LightDivider,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = RedditOrange,
    onPrimary = DarkSurface,
    primaryContainer = RedditOrangeDark,
    onPrimaryContainer = DarkTextPrimary,

    secondary = RedditBlue,
    onSecondary = DarkSurface,
    secondaryContainer = Color(0xFF2A2D46),
    onSecondaryContainer = DarkTextPrimary,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,

    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkDivider,
    error = ErrorRed
)

@Composable
fun HWStarterRepoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}