package ru.finnipet.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper

enum class TailSide { BOTTOM, LEFT }

private fun bubbleShape(side: TailSide, tail: Float, radius: Float): Shape = GenericShape { size, _ ->
    when (side) {
        TailSide.BOTTOM -> {
            addRoundRect(RoundRect(0f, 0f, size.width, size.height - tail, CornerRadius(radius)))
            moveTo(size.width / 2f - tail, size.height - tail - 1f)
            lineTo(size.width / 2f, size.height)
            lineTo(size.width / 2f + tail, size.height - tail - 1f)
            close()
        }
        TailSide.LEFT -> {
            addRoundRect(RoundRect(tail, 0f, size.width, size.height, CornerRadius(radius)))
            moveTo(tail + 1f, size.height / 2f - tail)
            lineTo(0f, size.height / 2f)
            lineTo(tail + 1f, size.height / 2f + tail)
            close()
        }
    }
}

/** Speech bubble whose text pops in whenever it changes. */
@Composable
fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    side: TailSide = TailSide.BOTTOM,
    maxWidth: Dp = 250.dp,
    onClick: (() -> Unit)? = null,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val tail = with(density) { 12.dp.toPx() }
    val shape = remember(side, tail) { bubbleShape(side, tail, with(density) { 18.dp.toPx() }) }
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            (fadeIn(tween(120)) + scaleIn(spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium), initialScale = 0.7f))
                .togetherWith(fadeOut(tween(90)))
        },
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier,
        label = "bubble",
    ) { shown ->
        Box(
            Modifier
                .widthIn(max = maxWidth)
                .shadow(8.dp, shape, ambientColor = Color(0x40000000), spotColor = Color(0x40000000))
                .background(Paper, shape)
                .border(2.dp, Line, shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick)
                    } else {
                        Modifier
                    }
                )
                .padding(
                    start = if (side == TailSide.LEFT) 26.dp else 16.dp,
                    end = 16.dp,
                    top = 10.dp,
                    bottom = if (side == TailSide.BOTTOM) 22.dp else 10.dp,
                ),
        ) {
            Text(shown, style = MaterialTheme.typography.bodyMedium, color = Ink, textAlign = TextAlign.Center)
        }
    }
}
