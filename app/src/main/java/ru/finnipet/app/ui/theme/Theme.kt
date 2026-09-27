package ru.finnipet.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FinniColorScheme = lightColorScheme(
    primary = FoxOrange,
    onPrimary = Color.White,
    primaryContainer = FoxOrangeContainer,
    onPrimaryContainer = FoxOrangeDark,
    secondary = LeafGreen,
    onSecondary = Color.White,
    secondaryContainer = LeafGreenContainer,
    onSecondaryContainer = LeafGreenDark,
    tertiary = SkyBlue,
    tertiaryContainer = SkyBlueContainer,
    background = WarmBackground,
    onBackground = InkBrown,
    surface = WarmSurface,
    onSurface = InkBrown,
    surfaceVariant = FoxOrangeContainer,
    onSurfaceVariant = InkBrown,
    error = ErrorRed,
)

@Composable
fun FinniPetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = FinniColorScheme,
        typography = FinniTypography,
        content = content,
    )
}
