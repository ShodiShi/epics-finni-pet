package ru.finnipet.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// One warm light scheme on purpose: a kids' game should look the same in any system theme.
private val FinniColors = lightColorScheme(
    primary = FoxOrange,
    onPrimary = Color.White,
    primaryContainer = FoxOrangeSoft,
    onPrimaryContainer = FoxOrangeLip,
    inversePrimary = FoxOrangeSoft,
    secondary = LeafGreen,
    onSecondary = Color.White,
    secondaryContainer = LeafGreenSoft,
    onSecondaryContainer = LeafGreenLip,
    tertiary = SkyBlue,
    onTertiary = Color.White,
    tertiaryContainer = SkyBlueSoft,
    onTertiaryContainer = SkyBlueLip,
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = CreamDeep,
    onSurfaceVariant = InkSoft,
    surfaceTint = Color.Transparent,
    inverseSurface = Ink,
    inverseOnSurface = Cream,
    error = TomatoRed,
    onError = Color.White,
    errorContainer = TomatoRedSoft,
    onErrorContainer = TomatoRedLip,
    outline = LineStrong,
    outlineVariant = Line,
    scrim = Color(0xCC2A1D15),
    surfaceBright = Paper,
    surfaceDim = CreamDeep,
    surfaceContainerLowest = Paper,
    surfaceContainerLow = Color(0xFFFFFBF6),
    surfaceContainer = Cream,
    surfaceContainerHigh = CreamDeep,
    surfaceContainerHighest = Color(0xFFFFE6CF),
)

private val FinniShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun FinniPetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FinniColors,
        typography = FinniTypography,
        shapes = FinniShapes,
        content = content,
    )
}
