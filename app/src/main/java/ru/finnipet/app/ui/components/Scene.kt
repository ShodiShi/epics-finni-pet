package ru.finnipet.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.Paper

/** Text with a thick white outline so it reads like a sticker on top of busy artwork. */
@Composable
fun StickerText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Ink,
    outline: Color = Color.White,
    outlineWidth: Dp = 7.dp,
    textAlign: TextAlign? = null,
) {
    val density = LocalDensity.current
    Box(modifier) {
        Text(
            text,
            style = style.copy(drawStyle = Stroke(width = with(density) { outlineWidth.toPx() }, join = StrokeJoin.Round)),
            color = outline,
            textAlign = textAlign,
        )
        Text(text, style = style, color = color, textAlign = textAlign)
    }
}

/**
 * Illustrated header: a scene painting, a sticker-style title, the coin counter and a pose of Finni standing on the
 * edge of the cream content sheet below.
 */
@Composable
fun SceneHeader(
    @DrawableRes background: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    pose: PetPose? = null,
    coins: Int? = null,
    height: Dp = 260.dp,
    poseHeight: Dp = 150.dp,
    leading: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Image(
            painterResource(background), null,
            Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Brush.verticalGradient(listOf(Color(0x33000000), Color.Transparent)))
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(30.dp)
                .background(Cream, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
        )
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (leading != null) {
                    leading()
                    Spacer(Modifier.width(10.dp))
                }
                Column {
                    StickerText(title, MaterialTheme.typography.headlineLarge)
                    if (subtitle != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSoft,
                            modifier = Modifier
                                .background(Paper.copy(alpha = 0.9f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
            if (coins != null) CoinCounter(coins)
        }
        if (pose != null) {
            PetSprite(
                pose,
                height = poseHeight,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 12.dp),
            )
        }
    }
}

/**
 * Cream strip behind the status bar. Scene screens scroll edge to edge, so once the painted header has scrolled away
 * this fades in and keeps list content from running under the clock.
 */
@Composable
fun StatusBarScrim(visible: Boolean, modifier: Modifier = Modifier) {
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(200), label = "statusScrim")
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier
            .fillMaxWidth()
            .height(top + 14.dp)
            .graphicsLayer { this.alpha = alpha }
            .drawBehind {
                val solid = top.toPx() / size.height
                drawRect(Brush.verticalGradient(0f to Cream, solid to Cream, 1f to Cream.copy(alpha = 0f)))
            }
    )
}
