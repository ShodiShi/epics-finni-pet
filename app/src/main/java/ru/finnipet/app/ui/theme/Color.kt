package ru.finnipet.app.ui.theme

import androidx.compose.ui.graphics.Color
import ru.finnipet.app.domain.Tone

// Brand
val FoxOrange = Color(0xFFFF7A1A)
val FoxOrangeLip = Color(0xFFD65E05)
val FoxOrangeSoft = Color(0xFFFFE3CC)

val LeafGreen = Color(0xFF3DBE6B)
val LeafGreenLip = Color(0xFF28984F)
val LeafGreenSoft = Color(0xFFD7F5E1)

val SkyBlue = Color(0xFF3AA6F0)
val SkyBlueLip = Color(0xFF1C84CC)
val SkyBlueSoft = Color(0xFFD9EEFF)

val BerryPink = Color(0xFFFF5E8A)
val BerryPinkLip = Color(0xFFD83C68)
val BerryPinkSoft = Color(0xFFFFE0EA)

val SunGold = Color(0xFFFFC531)
val SunGoldLip = Color(0xFFDFA000)
val SunGoldSoft = Color(0xFFFFF2C7)

val GrapePurple = Color(0xFF9B6BF2)
val GrapePurpleSoft = Color(0xFFEDE3FF)

val TomatoRed = Color(0xFFFF5A5F)
val TomatoRedLip = Color(0xFFD63C42)
val TomatoRedSoft = Color(0xFFFFE1E2)

// Neutrals (warm)
val Cream = Color(0xFFFFF7EC)
val CreamDeep = Color(0xFFFFEEDB)
val Paper = Color(0xFFFFFFFF)
val Ink = Color(0xFF3B2A20)
val InkSoft = Color(0xFF8B7465)
val Line = Color(0xFFF0E1D2)
val LineStrong = Color(0xFFE2CDB9)
val Disabled = Color(0xFFE8DED4)
val DisabledLip = Color(0xFFD2C4B6)
val DisabledInk = Color(0xFFA6978A)
val Floor = Color(0xFFE9B98C)

fun toneColor(tone: Tone): Color = when (tone) {
    Tone.PINK -> BerryPinkSoft
    Tone.ORANGE -> FoxOrangeSoft
    Tone.BLUE -> SkyBlueSoft
    Tone.GOLD -> SunGoldSoft
    Tone.GREEN -> LeafGreenSoft
    Tone.PURPLE -> GrapePurpleSoft
}
