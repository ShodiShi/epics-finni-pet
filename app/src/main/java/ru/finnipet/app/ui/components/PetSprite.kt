package ru.finnipet.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.finnipet.app.R
import ru.finnipet.app.domain.PetPose
import kotlin.math.roundToInt

/**
 * Finni. Every pose is drawn at the same pixel scale (derived from the idle pose's height) so switching poses never
 * makes the character grow or shrink; poses cross-fade with a little pop and the idle breathes.
 */
@Composable
fun PetSprite(
    pose: PetPose,
    height: Dp,
    modifier: Modifier = Modifier,
    breathing: Boolean = true,
    contentDescription: String? = null,
) {
    val idle = painterResource(R.drawable.pet_idle)
    val dpPerPx = height.value / idle.intrinsicSize.height
    val infinite = rememberInfiniteTransition(label = "breath")
    val breath by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath",
    )
    AnimatedContent(
        targetState = pose,
        transitionSpec = {
            (fadeIn(tween(140)) + scaleIn(spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow), initialScale = 0.9f))
                .togetherWith(fadeOut(tween(110)))
        },
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier,
        label = "pose",
    ) { shown ->
        val painter = painterResource(shown.art)
        Image(
            painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .size((painter.intrinsicSize.width * dpPerPx).dp, (painter.intrinsicSize.height * dpPerPx).dp)
                .graphicsLayer {
                    if (breathing) {
                        scaleY = 1f + 0.022f * breath
                        scaleX = 1f - 0.012f * breath
                    }
                    transformOrigin = TransformOrigin(0.5f, 1f)
                },
        )
    }
}

/**
 * Places its content so that the point [pivot] (fractions of the content size; default bottom-centre) sits on
 * [anchor], given in this layout's own pixel coordinates. Used to stand Finni and furniture on the room floor.
 */
@Composable
fun Anchored(
    anchor: () -> Offset,
    modifier: Modifier = Modifier,
    pivot: Offset = Offset(0.5f, 1f),
    content: @Composable () -> Unit,
) {
    Layout(content, modifier.fillMaxSize()) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            val a = anchor()
            placeables.forEach {
                it.place((a.x - it.width * pivot.x).roundToInt(), (a.y - it.height * pivot.y).roundToInt())
            }
        }
    }
}

/** Soft oval shadow under a character or object. */
@Composable
fun GroundShadow(width: Dp, modifier: Modifier = Modifier, alpha: Float = 0.22f) {
    Canvas(modifier.size(width, width * 0.2f)) {
        scale(1f, 0.2f, pivot = center) {
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFF5B3A22).copy(alpha = alpha), Color.Transparent), center, size.width / 2f),
                radius = size.width / 2f,
                center = center,
            )
        }
    }
}
