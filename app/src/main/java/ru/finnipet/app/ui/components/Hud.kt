package ru.finnipet.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.finnipet.app.R
import ru.finnipet.app.ui.fx.LocalFx
import ru.finnipet.app.ui.fx.LocalSound
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.FoxOrangeLip
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SunGold
import ru.finnipet.app.ui.theme.TomatoRed

/** Frosted pill used for everything in the top HUD. */
@Composable
fun HudPill(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(
        modifier
            .shadow(6.dp, RoundedCornerShape(50), ambientColor = Color(0x33000000), spotColor = Color(0x33000000))
            .background(Paper.copy(alpha = 0.94f), RoundedCornerShape(50))
            .border(2.dp, Color.White, RoundedCornerShape(50))
            .padding(start = 6.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

/**
 * Coin balance. It registers itself as the target flying coins aim for, bumps when they land and counts up
 * to the new balance with a small delay so the number changes as the coins arrive.
 */
@Composable
fun CoinCounter(coins: Int, modifier: Modifier = Modifier) {
    val fx = LocalFx.current
    val sound = LocalSound.current
    val bump = remember { Animatable(1f) }
    val shown by animateIntAsState(
        coins,
        tween(durationMillis = 650, delayMillis = if (coins >= 0) 300 else 0, easing = FastOutSlowInEasing),
        label = "coins",
    )
    LaunchedEffect(fx) {
        fx.coinArrived.collect {
            sound?.play(Sfx.TICK, volume = 0.7f, rate = 0.9f + (Math.random() * 0.3).toFloat())
            bump.snapTo(1.25f)
            bump.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
        }
    }
    HudPill(
        modifier
            .onGloballyPositioned { fx.coinTarget = it.boundsInRoot().let { r -> Offset(r.left + r.height / 2f, r.center.y) } }
            .semantics { contentDescription = "Монеты: $coins" }
    ) {
        Image(
            painterResource(R.drawable.ic_coin), null,
            Modifier
                .size(30.dp)
                .graphicsLayer { scaleX = bump.value; scaleY = bump.value },
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "$shown",
            style = MaterialTheme.typography.titleMedium,
            color = if (shown != coins) FoxOrangeLip else Ink,
        )
    }
}

@Composable
fun LevelChip(level: Int, progress: Float, modifier: Modifier = Modifier) {
    HudPill(modifier.semantics { contentDescription = "Уровень $level" }) {
        Box(contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_star), null, Modifier.size(32.dp))
            Text("$level", style = MaterialTheme.typography.labelMedium, color = Ink, modifier = Modifier.padding(top = 3.dp))
        }
        Spacer(Modifier.width(6.dp))
        Box(
            Modifier
                .width(42.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Line)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress.coerceIn(0.04f, 1f))
                    .height(8.dp)
                    .background(SunGold, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun StreakChip(days: Int, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "flame")
    val flicker by infinite.animateFloat(
        0.92f, 1.08f,
        infiniteRepeatable(tween(520, easing = LinearEasing), RepeatMode.Reverse),
        label = "flicker",
    )
    HudPill(modifier.semantics { contentDescription = "Серия: $days дней" }) {
        Image(
            painterResource(R.drawable.ic_flame), null,
            Modifier
                .size(28.dp)
                .graphicsLayer {
                    val lit = days > 0
                    scaleX = if (lit) flicker else 1f
                    scaleY = if (lit) 2f - flicker else 1f
                    alpha = if (lit) 1f else 0.35f
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                },
        )
        Spacer(Modifier.width(4.dp))
        Text("$days", style = MaterialTheme.typography.titleMedium, color = Ink)
    }
}

@Composable
fun RoundIconButton(
    @DrawableRes icon: Int?,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    vector: (@Composable () -> Unit)? = null,
) {
    val feedback = rememberFeedback()
    Box(
        modifier
            .size(size)
            .shadow(6.dp, CircleShape, ambientColor = Color(0x33000000), spotColor = Color(0x33000000))
            .background(Paper.copy(alpha = 0.94f), CircleShape)
            .border(2.dp, Color.White, CircleShape)
            .clickable(remember { MutableInteractionSource() }, indication = null) {
                feedback.tap()
                onClick()
            }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) Image(painterResource(icon), null, Modifier.size(size * 0.62f)) else vector?.invoke()
    }
}

/** Rounded meter with an art icon; pulses red when low. */
@Composable
fun StatMeter(
    label: String,
    @DrawableRes icon: Int,
    value: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(value / 100f, tween(900, delayMillis = 350, easing = FastOutSlowInEasing), label = "meter")
    val low = value < 30f
    val infinite = rememberInfiniteTransition(label = "low")
    val pulse by infinite.animateFloat(1f, 1.12f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "pulse")
    val barColor = if (low) TomatoRed else color
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
            painterResource(icon), null,
            Modifier
                .size(34.dp)
                .graphicsLayer { if (low) { scaleX = pulse; scaleY = pulse } },
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = InkSoft)
                Text("${value.toInt()}%", style = MaterialTheme.typography.labelMedium, color = if (low) TomatoRed else Ink)
            }
            Spacer(Modifier.height(4.dp))
            ProgressBar(animated, barColor, height = 14.dp)
        }
    }
}

@Composable
fun ProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 12.dp, track: Color = Line) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(track)
            .drawBehind {
                val w = size.width * fraction.coerceIn(0f, 1f)
                if (w <= 0f) return@drawBehind
                val r = CornerRadius(size.height / 2f)
                drawRoundRect(
                    Brush.verticalGradient(listOf(color.copy(alpha = 0.85f), color)),
                    size = Size(w.coerceAtLeast(size.height), size.height),
                    cornerRadius = r,
                )
                drawRoundRect(
                    Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(size.height * 0.4f, size.height * 0.18f),
                    size = Size((w - size.height * 0.8f).coerceAtLeast(0f), size.height * 0.26f),
                    cornerRadius = r,
                )
            }
    )
}

/** Quiz-style progress: one rounded segment per step. */
@Composable
fun SegmentedProgress(total: Int, done: Int, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            val fill by animateFloatAsState(if (i < done) 1f else 0f, tween(450), label = "segment")
            ProgressBar(fill, color, Modifier.weight(1f), height = 12.dp)
        }
    }
}

@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier, color: Color = TomatoRed) {
    Box(
        modifier
            .size(24.dp)
            .background(color, CircleShape)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text("$count", style = MaterialTheme.typography.labelSmall, color = Color.White, textAlign = TextAlign.Center)
    }
}
