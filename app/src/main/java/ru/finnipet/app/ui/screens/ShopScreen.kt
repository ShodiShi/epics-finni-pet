package ru.finnipet.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.ItemCategory
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.domain.SHOP_ITEMS
import ru.finnipet.app.domain.ShopItem
import ru.finnipet.app.domain.canAfford
import ru.finnipet.app.ui.LocalBarInset
import ru.finnipet.app.ui.components.ChunkyButton
import ru.finnipet.app.ui.components.ChunkyColor
import ru.finnipet.app.ui.components.PillTab
import ru.finnipet.app.ui.components.PillTabs
import ru.finnipet.app.ui.components.SceneHeader
import ru.finnipet.app.ui.components.StatusBarScrim
import ru.finnipet.app.ui.components.Tab
import ru.finnipet.app.ui.components.ToastHost
import ru.finnipet.app.ui.components.rememberToastState
import ru.finnipet.app.ui.fx.LocalFx
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.FoxOrange
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SkyBlueSoft
import ru.finnipet.app.ui.theme.toneColor

private val SHOP_TIPS = listOf(
    "Сначала купи еду — она нужна каждый день. Игрушки подождут!",
    "Хочется игрушку? Подумай: может, лучше положить монеты в копилку?",
    "Одинаковые вещи бывают разной цены. Сравнивай!",
)

@Composable
fun ShopScreen(state: GameState, onBuy: (String) -> Unit, onNavigate: (Tab) -> Unit) {
    val barInset = LocalBarInset.current
    val toast = rememberToastState()
    var category by rememberSaveable { mutableIntStateOf(0) }
    val items = SHOP_ITEMS.filter { it.category == if (category == 0) ItemCategory.NEED else ItemCategory.WANT }
    val grid = rememberLazyGridState()
    val scrolled by remember { derivedStateOf { grid.firstVisibleItemIndex > 0 } }

    Box(Modifier.fillMaxSize().background(Cream)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = grid,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = barInset + 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SceneHeader(
                    background = R.drawable.bg_shop,
                    title = "Магазин",
                    subtitle = "Трать с умом",
                    pose = PetPose.SHOP,
                    coins = state.coins,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .background(SkyBlueSoft, RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(painterResource(PetPose.THINK.art), null, Modifier.size(44.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        SHOP_TIPS[(state.currentDayEpoch.coerceAtLeast(0) % SHOP_TIPS.size).toInt()],
                        style = MaterialTheme.typography.bodyMedium, color = Ink,
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                PillTabs(
                    listOf(PillTab("Еда · нужно", R.drawable.item_apple), PillTab("Игрушки · хочется", R.drawable.item_ball)),
                    selected = category,
                    onSelect = { category = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                ShopItemCard(
                    item = item,
                    owned = state.inventory[item.id] ?: 0,
                    affordable = canAfford(state, item),
                    missing = item.cost - state.coins,
                    onBuy = { onBuy(item.id) },
                    onBought = {
                        toast.show(
                            "Куплено: ${item.name.lowercase()}! Загляни домой — ${if (item.category == ItemCategory.NEED) "пора угощать" else "пора играть"}.",
                            icon = item.art,
                        )
                    },
                    onTooExpensive = { missing ->
                        toast.show(
                            "Не хватает $missing ${plural(missing, "монеты", "монет", "монет")}. Заработай в «Учёбе»!",
                            icon = R.drawable.ic_coin,
                            action = "В учёбу",
                        ) { onNavigate(Tab.EARN) }
                    },
                    modifier = Modifier
                        .padding(start = if (index % 2 == 0) 16.dp else 0.dp, end = if (index % 2 == 1) 16.dp else 0.dp)
                        .animateItem(),
                )
            }
        }
        StatusBarScrim(scrolled, Modifier.align(Alignment.TopCenter))
        ToastHost(toast, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    owned: Int,
    affordable: Boolean,
    missing: Int,
    onBuy: () -> Unit,
    onBought: () -> Unit,
    onTooExpensive: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fx = LocalFx.current
    val feedback = rememberFeedback()
    val scope = rememberCoroutineScope()
    val bounce = remember { Animatable(1f) }
    val shake = remember { Animatable(0f) }
    var artCenter by remember { mutableStateOf(Offset.Zero) }
    val need = item.category == ItemCategory.NEED

    Column(
        modifier
            .background(Paper, RoundedCornerShape(24.dp))
            .border(2.dp, Line, RoundedCornerShape(24.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(108.dp)
                    .background(toneColor(item.tone), CircleShape)
                    .onGloballyPositioned { artCenter = it.boundsInRoot().center },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(item.art), item.name,
                    Modifier
                        .size(84.dp)
                        .graphicsLayer { scaleX = bounce.value; scaleY = bounce.value },
                )
            }
            if (owned > 0) {
                Text(
                    "×$owned",
                    style = MaterialTheme.typography.labelMedium,
                    color = Paper,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(LeafGreen, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(item.name, style = MaterialTheme.typography.titleMedium, color = Ink, textAlign = TextAlign.Center, maxLines = 1)
        Text(
            "+${item.statGain} ${if (need) "сытости" else "радости"}",
            style = MaterialTheme.typography.labelMedium,
            color = if (need) LeafGreen else FoxOrange,
        )
        Spacer(Modifier.height(10.dp))
        ChunkyButton(
            text = "${item.cost}",
            icon = R.drawable.ic_coin,
            color = if (affordable) ChunkyColor.GOLD else ChunkyColor.WHITE,
            height = 44.dp,
            sfx = null,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(shake.value.dp.roundToPx(), 0) },
            onClick = {
                if (affordable) {
                    onBuy()
                    feedback.success(Sfx.PURCHASE)
                    fx.coinsFromCounter(artCenter, 4)
                    fx.sparkles(artCenter, 10, 80f)
                    onBought()
                    scope.launch {
                        bounce.snapTo(0.7f)
                        bounce.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 500f))
                    }
                } else {
                    feedback.error(Sfx.WRONG)
                    onTooExpensive(missing)
                    scope.launch {
                        shake.animateTo(0f, keyframes {
                            durationMillis = 380
                            -10f at 60
                            10f at 120
                            -7f at 190
                            7f at 260
                            0f at 380
                        })
                    }
                }
            },
        )
        if (!affordable) {
            Spacer(Modifier.height(4.dp))
            Text("не хватает $missing", style = MaterialTheme.typography.labelSmall, color = InkSoft)
        }
    }
}
