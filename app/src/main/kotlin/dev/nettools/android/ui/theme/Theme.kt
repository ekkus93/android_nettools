package dev.nettools.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Primary40,
    onPrimary = OnPrimary40,
    primaryContainer = PrimaryContainer80,
    onPrimaryContainer = OnPrimaryContainer80,
    secondary = Secondary40,
    onSecondary = OnSecondary40,
    secondaryContainer = SecondaryContainer80,
    onSecondaryContainer = OnSecondaryContainer80,
    tertiary = Tertiary40,
    tertiaryContainer = TertiaryContainer80,
    error = Error40,
    onError = OnError40,
    surface = Surface80,
)

private val DarkColors = darkColorScheme(
    primary = Primary80,
    onPrimary = OnPrimary80,
    primaryContainer = PrimaryContainer40,
    onPrimaryContainer = OnPrimaryContainer40,
    secondary = Secondary80,
    onSecondary = OnSecondary80,
    secondaryContainer = SecondaryContainer40,
    onSecondaryContainer = OnSecondaryContainer40,
    tertiary = Tertiary80,
    tertiaryContainer = TertiaryContainer40,
    error = Error80,
    onError = OnError80,
    surface = Surface40,
)

/**
 * Material Design 3 theme for NetTools.
 * Automatically switches between light and dark color schemes based on system preference.
 * On Android 12+ (API 31), supports dynamic Material You colors derived from the wallpaper.
 *
 * @param darkTheme Whether to use the dark color scheme.
 * @param dynamicColor Whether to use dynamic Material You colors; defaults to true on API 31+.
 * @param content The composable content to render within the theme.
 */
@Composable
fun NetToolsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            dynamicDarkColorScheme(context)
        dynamicColor && !darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
