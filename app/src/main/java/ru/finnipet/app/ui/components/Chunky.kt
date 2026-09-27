package ru.finnipet.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.BerryPink
import ru.finnipet.app.ui.theme.BerryPinkLip
import ru.finnipet.app.ui.theme.Disabled
import ru.finnipet.app.ui.theme.DisabledInk
import ru.finnipet.app.ui.theme.DisabledLip
import ru.finnipet.app.ui.theme.FoxOrange
import ru.finnipet.app.ui.theme.FoxOrangeLip
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.LeafGreenLip
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.LineStrong
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SkyBlue
import ru.finnipet.app.ui.theme.SkyBlueLip
import ru.finnipet.app.ui.theme.SunGold
import ru.finnipet.app.ui.theme.SunGoldLip
import ru.finnipet.app.ui.theme.TomatoRed
import ru.finnipet.app.ui.theme.TomatoRedLip

enum class ChunkyColor(val face: Color, val lip: Color, val content: Color) {
    ORANGE(FoxOrange, FoxOrangeLip, Color.White),
    GREEN(LeafGreen, LeafGreenLip, Color.White),
    BLUE(SkyBlue, SkyBlueLip, Color.White),
    PINK(BerryPink, BerryPinkLip, Color.White),
    GOLD(SunGold, SunGoldLip, Ink),
    RED(TomatoRed, TomatoRedLip, Color.White),
    WHITE(Paper, LineStrong, Ink),
}

/**
 * The raised, pressable surface every button and tappable card is built from: a face sitting on a darker lip that
 * sinks when pressed. Min-width constraints are passed through so `fillMaxWidth` stretches the face.
 */
@Composable
fun ChunkySurface(
    modifier: Modifier = Modifier,
    face: Color = Paper,
    lip: Color = LineStrong,
    border: Color? = Line,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    lipHeight: Dp = 5.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    sfx: Sfx? = Sfx.TAP,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    /** Stretch children to the face's minimum size (single-child faces like buttons). Overlays should not. */
    fillContent: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink by animateFloatAsState(
        if (pressed && enabled && onClick != null) 1f else 0f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "press",
    )
    val feedback = rememberFeedback()
    val clickable = if (onClick != null) {
        Modifier.clickable(interaction, indication = null, enabled = enabled, role = Role.Button) {
            sfx?.let(feedback::tap)
            onClick()
        }
    } else {
        Modifier
    }
    Box(
        modifier
            .then(clickable)
            .drawBehind {
                val lipPx = lipHeight.toPx()
                val radius = shape.topStart.toPx(size, this)
                drawRoundRect(lip, Offset(0f, lipPx), Size(size.width, size.height - lipPx), CornerRadius(radius))
            }
            .padding(bottom = lipHeight),
        propagateMinConstraints = true,
    ) {
        Box(
            Modifier
                .graphicsLayer { translationY = sink * lipHeight.toPx() }
                .background(face, shape)
                .then(if (border != null) Modifier.border(2.dp, border, shape) else Modifier)
                .padding(contentPadding),
            contentAlignment = contentAlignment,
            propagateMinConstraints = fillContent,
            content = content,
        )
    }
}

@Composable
fun ChunkyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: ChunkyColor = ChunkyColor.ORANGE,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    height: Dp = 54.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    sfx: Sfx? = Sfx.TAP,
) {
    val face = if (enabled) color.face else Disabled
    val lip = if (enabled) color.lip else DisabledLip
    val content = if (enabled) color.content else DisabledInk
    ChunkySurface(
        modifier = modifier,
        face = face,
        lip = lip,
        border = null,
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
        enabled = enabled,
        sfx = sfx,
        fillContent = true,
    ) {
        Row(
            Modifier
                .heightIn(min = height)
                .drawBehind {
                    // soft gloss along the top edge
                    drawRoundRect(
                        Color.White.copy(alpha = if (enabled) 0.18f else 0f),
                        Offset(8.dp.toPx(), 5.dp.toPx()),
                        Size(size.width - 16.dp.toPx(), size.height * 0.34f),
                        CornerRadius(12.dp.toPx()),
                    )
                }
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = textStyle, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
