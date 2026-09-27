package ru.finnipet.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.ui.fx.LocalFx
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SunGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Slowly turning sunburst behind rewards. */
@Composable
fun SunburstRays(modifier: Modifier = Modifier, color: Color = SunGold, rays: Int = 14) {
    val infinite = rememberInfiniteTransition(label = "rays")
    val turn by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(26000, easing = LinearEasing)), label = "turn")
    Canvas(modifier) {
        val r = size.minDimension / 2f
        val step = 2f * PI.toFloat() / rays
        rotate(turn) {
            for (i in 0 until rays) {
                val a = i * step
                val path = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(center.x + cos(a) * r, center.y + sin(a) * r)
                    lineTo(center.x + cos(a + step / 2f) * r, center.y + sin(a + step / 2f) * r)
                    close()
                }
                drawPath(path, color.copy(alpha = 0.35f))
            }
        }
        drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.85f), Color.Transparent), center, r * 0.55f), r * 0.55f)
    }
}

data class Celebration(
    val key: String,
    val title: String,
    val subtitle: String,
    @DrawableRes val art: Int,
    val button: String,
    val sfx: Sfx = Sfx.FANFARE,
    val pose: PetPose? = PetPose.CHEER,
    val onAction: (() -> Unit)? = null,
)

/**
 * Full-screen reward moment: rays, the prize popping in, confetti and a fanfare. Each celebration keeps its own
 * content while it fades out, and dismissing names the one that was tapped so a double tap can't skip the next.
 */
@Composable
fun CelebrationOverlay(celebration: Celebration?, onDismiss: (Celebration) -> Unit) {
    val fx = LocalFx.current
    val feedback = rememberFeedback()
    AnimatedContent(
        targetState = celebration,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = { (fadeIn(tween(180)) togetherWith fadeOut(tween(160))).using(null) },
        contentKey = { it?.key },
        label = "celebration",
    ) { shown ->
        if (shown == null) return@AnimatedContent
        val pop = remember(shown.key) { Animatable(0f) }
        LaunchedEffect(shown.key) {
            feedback.play(shown.sfx)
            feedback.buzz()
            fx.confetti()
            pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
        }
        val infinite = rememberInfiniteTransition(label = "float")
        val float by infinite.animateFloat(-6f, 6f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "floatY")
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xCC2A1D15))
                .clickable(remember { MutableInteractionSource() }, indication = null) { },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Box(Modifier.size(300.dp), contentAlignment = Alignment.Center) {
                    SunburstRays(Modifier.fillMaxSize())
                    Image(
                        painterResource(shown.art), null,
                        Modifier
                            .size(180.dp)
                            .graphicsLayer {
                                scaleX = pop.value
                                scaleY = pop.value
                                rotationZ = (1f - pop.value) * -25f
                                translationY = float * density
                            },
                    )
                    if (shown.pose != null) {
                        PetSprite(
                            shown.pose, height = 110.dp,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .graphicsLayer { alpha = pop.value.coerceIn(0f, 1f) },
                        )
                    }
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(28.dp))
                        .background(Paper, RoundedCornerShape(28.dp))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(shown.title, style = MaterialTheme.typography.headlineMedium, color = Ink, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text(shown.subtitle, style = MaterialTheme.typography.bodyLarge, color = InkSoft, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    ChunkyButton(
                        shown.button,
                        onClick = {
                            shown.onAction?.invoke()
                            onDismiss(shown)
                        },
                        color = ChunkyColor.GREEN,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
