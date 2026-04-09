package dev.nettools.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
)

/**
 * Material Design 3 theme for NetTools.
 * Automatically switches between light and dark color schemes based on system preference.
 *
 * @param darkTheme Whether to use the dark color scheme.
 * @param content The composable content to render within the theme.
 */
@Composable
fun NetToolsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
