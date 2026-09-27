package ru.finnipet.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.CreamDeep
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.Paper

data class PillTab(val label: String, @DrawableRes val icon: Int? = null)

/** Two or three options with a white pill that slides to the selected one. */
@Composable
fun PillTabs(tabs: List<PillTab>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val feedback = rememberFeedback()
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(CreamDeep, RoundedCornerShape(50))
            .padding(4.dp)
    ) {
        val slot = maxWidth / tabs.size
        val pillX by animateDpAsState(slot * selected, spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow), label = "pill")
        Box(
            Modifier
                .offset { IntOffset(pillX.roundToPx(), 0) }
                .width(slot)
                .fillMaxHeight()
                .shadow(4.dp, RoundedCornerShape(50), ambientColor = Color(0x33000000), spotColor = Color(0x33000000))
                .background(Paper, RoundedCornerShape(50))
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            tabs.forEachIndexed { index, tab ->
                val color by animateColorAsState(if (index == selected) Ink else InkSoft, label = "tabColor")
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { this.selected = index == selected }
                        .clickable(remember { MutableInteractionSource() }, indication = null, role = Role.Tab) {
                            if (index != selected) {
                                feedback.tap()
                                onSelect(index)
                            }
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (tab.icon != null) {
                        Image(painterResource(tab.icon), null, Modifier.size(26.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(tab.label, style = MaterialTheme.typography.labelLarge, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
