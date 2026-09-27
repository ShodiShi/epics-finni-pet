package ru.finnipet.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.BADGES
import ru.finnipet.app.domain.Badge
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.domain.levelForXp
import ru.finnipet.app.domain.levelProgress
import ru.finnipet.app.domain.levelTitle
import ru.finnipet.app.ui.LocalBarInset
import ru.finnipet.app.ui.LocalSheets
import ru.finnipet.app.ui.components.ChunkySurface
import ru.finnipet.app.ui.components.ProgressBar
import ru.finnipet.app.ui.components.SceneHeader
import ru.finnipet.app.ui.components.StatusBarScrim
import ru.finnipet.app.ui.components.SunburstRays
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.FoxOrange
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SunGold
import ru.finnipet.app.ui.theme.SunGoldSoft

private val LOCKED = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun BadgesScreen(state: GameState) {
    val barInset = LocalBarInset.current
    val sheets = LocalSheets.current
    val list = rememberLazyListState()
    val scrolled by remember { derivedStateOf { list.firstVisibleItemIndex > 0 } }
    Box(
        Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            state = list,
            contentPadding = PaddingValues(bottom = barInset + 20.dp),
        ) {
            item {
                SceneHeader(
                    background = R.drawable.bg_trophy,
                    title = "Награды",
                    subtitle = "Получено ${state.unlockedBadgeIds.size} из ${BADGES.size}",
                    pose = PetPose.TROPHY,
                    coins = state.coins,
                )
            }
            item { LevelCard(state.xp) }
            item {
                Row(
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ResultTile(R.drawable.ic_coins_pile, "${state.totalCoinsEarned}", "заработано", Modifier.weight(1f))
                    ResultTile(R.drawable.ic_piggy, "${state.totalSaved}", "накоплено", Modifier.weight(1f))
                    ResultTile(R.drawable.ic_gift, "${state.totalShared}", "подарено", Modifier.weight(1f))
                }
            }
            items(BADGES.chunked(3)) { row ->
                Row(
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { badge ->
                        MedalTile(
                            badge,
                            unlocked = badge.id in state.unlockedBadgeIds,
                            onClick = { sheets.show { current -> BadgeSheet(badge, current) } },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        StatusBarScrim(scrolled, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun LevelCard(xp: Int) {
    val level = levelForXp(xp)
    val (into, span) = levelProgress(xp)
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(Paper, RoundedCornerShape(24.dp))
            .border(2.dp, Line, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_star), null, Modifier.size(78.dp))
            Text("$level", style = MaterialTheme.typography.headlineSmall, color = Ink, modifier = Modifier.padding(top = 6.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Уровень $level", style = MaterialTheme.typography.labelLarge, color = InkSoft)
            Text(levelTitle(level), style = MaterialTheme.typography.headlineSmall, color = FoxOrange)
            Spacer(Modifier.height(8.dp))
            ProgressBar(into.toFloat() / span, SunGold, height = 14.dp)
            Spacer(Modifier.height(4.dp))
            Text("$into / $span опыта до уровня ${level + 1}", style = MaterialTheme.typography.labelMedium, color = InkSoft)
        }
    }
}

@Composable
private fun MedalTile(badge: Badge, unlocked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "medal")
    val float by infinite.animateFloat(-3f, 3f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "medalFloat")
    ChunkySurface(
        modifier = modifier,
        face = if (unlocked) SunGoldSoft else Paper,
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 6.dp),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painterResource(badge.art), badge.title,
                    Modifier
                        .size(76.dp)
                        .graphicsLayer {
                            if (unlocked) translationY = float * density else alpha = 0.35f
                        },
                    colorFilter = if (unlocked) null else LOCKED,
                )
                if (!unlocked) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .background(Paper, CircleShape)
                            .border(2.dp, Line, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Lock, null, tint = InkSoft, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                badge.title,
                style = MaterialTheme.typography.labelMedium,
                color = if (unlocked) Ink else InkSoft,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun BadgeSheet(badge: Badge, state: GameState) {
    val unlocked = badge.id in state.unlockedBadgeIds
    val (current, target) = badge.progress(state)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            if (unlocked) SunburstRays(Modifier.fillMaxSize())
            Image(
                painterResource(badge.art), badge.title,
                Modifier
                    .size(150.dp)
                    .graphicsLayer { if (!unlocked) alpha = 0.4f },
                colorFilter = if (unlocked) null else LOCKED,
            )
        }
        Text(badge.title, style = MaterialTheme.typography.headlineSmall, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(badge.description, style = MaterialTheme.typography.bodyLarge, color = InkSoft, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        if (unlocked) {
            Text(badge.earned, style = MaterialTheme.typography.titleMedium, color = LeafGreen, textAlign = TextAlign.Center)
        } else if (target == 1) {
            Text("Пока не получено", style = MaterialTheme.typography.labelLarge, color = InkSoft)
        } else {
            ProgressBar(current.toFloat() / target, FoxOrange, height = 14.dp)
            Spacer(Modifier.height(4.dp))
            Text("${current.coerceAtMost(target)} из $target", style = MaterialTheme.typography.labelLarge, color = Ink)
        }
    }
}
