package ru.finnipet.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.finnipet.app.R
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.FoxOrangeLip
import ru.finnipet.app.ui.theme.FoxOrangeSoft
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.TomatoRed

enum class Tab(val route: String, val label: String, @DrawableRes val icon: Int) {
    HOME("home", "Дом", R.drawable.ic_house),
    EARN("earn", "Учёба", R.drawable.ic_book),
    SHOP("shop", "Магазин", R.drawable.ic_bag),
    SAVINGS("savings", "Копилка", R.drawable.ic_piggy),
    BADGES("badges", "Награды", R.drawable.ic_trophy),
}

/** Floating white tab bar with illustrated icons; a red dot flags a tab that wants attention. */
@Composable
fun FinniBottomBar(current: Tab?, attention: Set<Tab>, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val feedback = rememberFeedback()
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Box(
        modifier
            .fillMaxWidth()
            .shadow(18.dp, shape, ambientColor = Color(0x55000000), spotColor = Color(0x55000000))
            .background(Paper, shape)
            .navigationBarsPadding()
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            Tab.entries.forEach { tab ->
                val selected = tab == current
                val scale by animateFloatAsState(
                    if (selected) 1.16f else 0.94f,
                    spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
                    label = "tabScale",
                )
                val pill by animateFloatAsState(if (selected) 1f else 0f, spring(stiffness = Spring.StiffnessMedium), label = "tabPill")
                val labelColor by animateColorAsState(if (selected) FoxOrangeLip else InkSoft, label = "tabLabel")
                Column(
                    Modifier
                        .weight(1f)
                        .semantics { this.selected = selected }
                        .clickable(remember { MutableInteractionSource() }, indication = null, role = Role.Tab) {
                            if (!selected) {
                                feedback.tap(Sfx.POP)
                                onSelect(tab)
                            }
                        }
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.size(width = 58.dp, height = 42.dp), contentAlignment = Alignment.Center) {
                        Box(
                            Modifier
                                .size(width = 58.dp, height = 40.dp)
                                .graphicsLayer { alpha = pill; scaleX = 0.6f + 0.4f * pill; scaleY = 0.6f + 0.4f * pill }
                                .background(FoxOrangeSoft, RoundedCornerShape(50))
                        )
                        Image(
                            painterResource(tab.icon), tab.label,
                            Modifier
                                .size(36.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale },
                        )
                        if (tab in attention) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-6).dp, y = 2.dp)
                                    .size(12.dp)
                                    .background(TomatoRed, CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }
                    Text(tab.label, style = MaterialTheme.typography.labelSmall, color = labelColor)
                }
            }
        }
    }
}
