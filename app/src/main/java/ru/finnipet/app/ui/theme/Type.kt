package ru.finnipet.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.finnipet.app.R

private fun nunito(weight: Int) = Font(
    R.font.nunito,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Nunito = FontFamily(nunito(600), nunito(700), nunito(800), nunito(900))

private fun style(size: Int, weight: Int, line: Int, spacing: Double = 0.0) = TextStyle(
    fontFamily = Nunito,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.em,
)

val FinniTypography = Typography(
    displayLarge = style(44, 900, 50),
    displayMedium = style(36, 900, 42),
    displaySmall = style(30, 900, 36),
    headlineLarge = style(30, 900, 36),
    headlineMedium = style(26, 900, 32),
    headlineSmall = style(22, 900, 28),
    titleLarge = style(20, 800, 26),
    titleMedium = style(17, 800, 22),
    titleSmall = style(15, 800, 20),
    bodyLarge = style(17, 700, 24),
    bodyMedium = style(15, 700, 21),
    bodySmall = style(13, 700, 18),
    labelLarge = style(16, 800, 20, 0.01),
    labelMedium = style(13, 800, 16, 0.01),
    labelSmall = style(11, 800, 14, 0.02),
)
