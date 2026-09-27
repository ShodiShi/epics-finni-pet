package ru.finnipet.app.ui.screens

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.DailyQuest
import ru.finnipet.app.domain.Economy
import ru.finnipet.app.domain.ItemCategory
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.domain.ShopItem
import ru.finnipet.app.domain.chestClaimedToday
import ru.finnipet.app.domain.chestReady
import ru.finnipet.app.domain.isQuestDone
import ru.finnipet.app.domain.ownedItems
import ru.finnipet.app.ui.components.ChunkyButton
import ru.finnipet.app.ui.components.ChunkyColor
import ru.finnipet.app.ui.components.ChunkySurface
import ru.finnipet.app.ui.components.CountBadge
import ru.finnipet.app.ui.components.PetSprite
import ru.finnipet.app.ui.components.SunburstRays
import ru.finnipet.app.ui.theme.FoxOrange
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.toneColor

@Composable
fun SheetTitle(title: String, subtitle: String?) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Ink, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkSoft, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun InventorySheet(
    state: GameState,
    category: ItemCategory,
    onUse: (item: ShopItem, from: Offset, art: ImageBitmap) -> Unit,
    onShop: () -> Unit,
) {
    val owned = ownedItems(state, category)
    val food = category == ItemCategory.NEED
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SheetTitle(
            if (food) "Чем покормим?" else "Во что поиграем?",
            if (food) "Еда — это то, что нужно каждый день." else "Игрушки радуют, но это «хочется».",
        )
        if (owned.isEmpty()) {
            Row(verticalAlignment = Alignment.Bottom) {
                PetSprite(if (food) PetPose.HUNGRY else PetPose.SAD, height = 150.dp)
                if (food) Image(painterResource(R.drawable.ic_bowl), "Пустая миска", Modifier.size(72.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                if (food) "В миске пусто. Купим еды в магазине?" else "Игрушек пока нет. Заглянем в магазин?",
                style = MaterialTheme.typography.bodyLarge,
                color = InkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
            Spacer(Modifier.height(16.dp))
            ChunkyButton("В магазин", onShop, color = ChunkyColor.ORANGE, icon = R.drawable.ic_bag,
                modifier = Modifier.padding(horizontal = 40.dp).fillMaxWidth())
        } else {
            owned.chunked(3).forEach { row ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { (item, count) -> InventoryTile(item, count, onUse, Modifier.weight(1f)) }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun InventoryTile(
    item: ShopItem,
    count: Int,
    onUse: (ShopItem, Offset, ImageBitmap) -> Unit,
    modifier: Modifier = Modifier,
) {
    val art = ImageBitmap.imageResource(item.art)
    var center by remember { mutableStateOf(Offset.Zero) }
    ChunkySurface(
        modifier = modifier.onGloballyPositioned { center = it.boundsInRoot().center },
        onClick = { onUse(item, center, art) },
        contentPadding = PaddingValues(10.dp),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(70.dp)
                    .background(toneColor(item.tone), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(item.art), item.name, Modifier.size(54.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(item.name, style = MaterialTheme.typography.titleSmall, color = Ink, maxLines = 1)
            Text(
                "+${item.statGain} ${if (item.category == ItemCategory.NEED) "сытости" else "радости"}",
                style = MaterialTheme.typography.labelSmall,
                color = if (item.category == ItemCategory.NEED) LeafGreen else FoxOrange,
            )
        }
        CountBadge(count, Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp))
    }
}

@Composable
fun QuestsSheet(state: GameState, onClaim: (Offset) -> Unit) {
    val ready = chestReady(state)
    val claimed = chestClaimedToday(state)
    var chestCenter by remember { mutableStateOf(Offset.Zero) }
    val infinite = rememberInfiniteTransition(label = "chest")
    val wiggle by infinite.animateFloat(-6f, 6f, infiniteRepeatable(tween(160), RepeatMode.Reverse), label = "wiggle")
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SheetTitle("Задания на сегодня", "Выполни все три — и сундук с монетами твой!")
        DailyQuest.entries.forEach { quest ->
            val done = isQuestDone(quest, state)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .background(Paper, RoundedCornerShape(20.dp))
                    .border(2.dp, if (done) LeafGreen.copy(alpha = 0.5f) else Line, RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(painterResource(quest.icon), null, Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(quest.title, style = MaterialTheme.typography.titleSmall, color = Ink)
                    if (quest == DailyQuest.QUIZ) {
                        Text(
                            "${state.currentDayAnsweredIds.count { it in state.todayQuestionIds }} из ${state.todayQuestionIds.size}",
                            style = MaterialTheme.typography.labelMedium, color = InkSoft,
                        )
                    }
                }
                Box(
                    Modifier
                        .size(32.dp)
                        .background(if (done) LeafGreen else Color.Transparent, CircleShape)
                        .border(3.dp, if (done) LeafGreen else Line, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.size(190.dp), contentAlignment = Alignment.Center) {
            if (ready || claimed) SunburstRays(Modifier.size(190.dp))
            Image(
                painterResource(if (claimed) R.drawable.ic_chest_open else R.drawable.ic_chest),
                "Сундук",
                Modifier
                    .size(130.dp)
                    .onGloballyPositioned { chestCenter = it.boundsInRoot().center }
                    .graphicsLayer { rotationZ = if (ready) wiggle else 0f },
            )
        }
        ChunkyButton(
            text = when {
                claimed -> "Сундук открыт — приходи завтра!"
                ready -> "Открыть сундук! +${Economy.CHEST_REWARD}"
                else -> "Сначала выполни задания"
            },
            onClick = { onClaim(chestCenter) },
            enabled = ready,
            color = ChunkyColor.GOLD,
            icon = if (ready) R.drawable.ic_coin else null,
            sfx = null,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
        )
    }
}
