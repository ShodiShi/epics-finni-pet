package ru.finnipet.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.Economy
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.domain.SAVINGS_GOALS
import ru.finnipet.app.domain.savingsGoalById
import ru.finnipet.app.domain.shareGoalById
import ru.finnipet.app.ui.LocalBarInset
import ru.finnipet.app.ui.components.ChunkyButton
import ru.finnipet.app.ui.components.ChunkyColor
import ru.finnipet.app.ui.components.PillTab
import ru.finnipet.app.ui.components.PillTabs
import ru.finnipet.app.ui.components.ProgressBar
import ru.finnipet.app.ui.components.SceneHeader
import ru.finnipet.app.ui.components.StatusBarScrim
import ru.finnipet.app.ui.fx.LocalFx
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.BerryPink
import ru.finnipet.app.ui.theme.BerryPinkSoft
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.LeafGreenSoft
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SunGold
import ru.finnipet.app.ui.theme.SunGoldSoft

private val AMOUNTS = listOf(5, 10, 25)

@Composable
fun SavingsScreen(
    state: GameState,
    onDeposit: (Int) -> Unit,
    onShare: (Int) -> Unit,
    onAcknowledgeInterest: () -> Unit,
) {
    val barInset = LocalBarInset.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
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
                    background = R.drawable.bg_bank,
                    title = "Копилка",
                    subtitle = "Копи на мечту и помогай другим",
                    pose = if (tab == 0) PetPose.PIGGY else PetPose.GIFT,
                    coins = state.coins,
                )
            }
            if (state.lastInterestEarned > 0) {
                item { InterestBanner(state.lastInterestEarned, onAcknowledgeInterest) }
            }
            item {
                PillTabs(
                    listOf(PillTab("Коплю", R.drawable.ic_piggy), PillTab("Делюсь", R.drawable.ic_gift)),
                    selected = tab,
                    onSelect = { tab = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
            if (tab == 0) {
                item { DreamCard(state, onDeposit) }
                item { RoomShelf(state) }
                item {
                    InfoCard(
                        R.drawable.ic_seedling, "Как растут накопления?",
                        "Каждый новый день копилка добавляет ${Economy.INTEREST_PERCENT}% к тому, что в ней лежит. " +
                            "Это называется «проценты». Чем больше и дольше копишь, тем больше прибавка!",
                        SunGoldSoft,
                    )
                }
            } else {
                item { ShareCard(state, onShare) }
                item {
                    InfoCard(
                        R.drawable.ic_heart, "Зачем делиться?",
                        "Когда мы помогаем другим, мир становится добрее. Даже маленькая помощь важна!" +
                            if (state.totalShared > 0) " Всего подарено монет: ${state.totalShared}." else "",
                        BerryPinkSoft,
                    )
                }
            }
        }
        StatusBarScrim(scrolled, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun InterestBanner(amount: Int, onDone: () -> Unit) {
    val feedback = rememberFeedback()
    LaunchedEffect(amount) { feedback.play(Sfx.COINS) }
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .background(LeafGreenSoft, RoundedCornerShape(22.dp))
            .border(2.dp, LeafGreen.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(R.drawable.ic_seedling), null, Modifier.size(46.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            "Пока тебя не было, копилка выросла на +$amount. Это проценты!",
            style = MaterialTheme.typography.bodyMedium, color = Ink, modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        ChunkyButton("Ура!", onDone, color = ChunkyColor.GREEN, height = 40.dp, textStyle = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun DreamCard(state: GameState, onDeposit: (Int) -> Unit) {
    val goal = savingsGoalById(state.savingsGoalId)
    val fraction by animateFloatAsState(
        (state.savingsBalance.toFloat() / goal.target).coerceIn(0f, 1f),
        tween(900, delayMillis = 250, easing = FastOutSlowInEasing),
        label = "dream",
    )
    val left = (goal.target - state.savingsBalance).coerceAtLeast(0)
    val days = (left + 9) / 10
    JarCard(
        art = goal.art,
        title = goal.title,
        subtitle = "Моя мечта",
        fraction = fraction,
        progressText = "${state.savingsBalance} из ${goal.target}",
        hint = if (left > 0) "Если откладывать по 10 монет в день, мечта сбудется через $days ${plural(days, "день", "дня", "дней")}." else null,
        ringColor = SunGold,
        coins = state.coins,
        onPut = onDeposit,
        buttonColor = ChunkyColor.GOLD,
    )
}

@Composable
private fun ShareCard(state: GameState, onShare: (Int) -> Unit) {
    val goal = shareGoalById(state.shareGoalId)
    val fraction by animateFloatAsState(
        (state.shareBalance.toFloat() / goal.target).coerceIn(0f, 1f),
        tween(900, delayMillis = 250, easing = FastOutSlowInEasing),
        label = "share",
    )
    JarCard(
        art = goal.art,
        title = goal.title,
        subtitle = goal.description,
        fraction = fraction,
        progressText = "${state.shareBalance} из ${goal.target}",
        hint = if (state.completedShareGoals > 0) {
            "Собрано добрых целей: ${state.completedShareGoals}"
        } else {
            null
        },
        ringColor = BerryPink,
        coins = state.coins,
        onPut = onShare,
        buttonColor = ChunkyColor.PINK,
    )
}

@Composable
private fun JarCard(
    @DrawableRes art: Int,
    title: String,
    subtitle: String,
    fraction: Float,
    progressText: String,
    hint: String?,
    ringColor: Color,
    coins: Int,
    onPut: (Int) -> Unit,
    buttonColor: ChunkyColor,
) {
    val fx = LocalFx.current
    val feedback = rememberFeedback()
    val scope = rememberCoroutineScope()
    val bounce = remember { Animatable(1f) }
    var target by remember { mutableStateOf(Offset.Zero) }

    fun put(amount: Int) {
        val actual = amount.coerceAtMost(coins)
        if (actual <= 0) return
        onPut(actual)
        feedback.success(Sfx.COIN)
        fx.coinsFromCounter(target, (actual / 5).coerceIn(2, 6))
        scope.launch {
            delay(420)
            fx.sparkles(target, 8, 90f)
            bounce.snapTo(1.18f)
            bounce.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 450f))
        }
    }

    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(Paper, RoundedCornerShape(28.dp))
            .border(2.dp, Line, RoundedCornerShape(28.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(210.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(210.dp)) {
                val stroke = 16.dp.toPx()
                val arc = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                drawArc(Line, 135f, 270f, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
                if (fraction > 0f) {
                    drawArc(ringColor, 135f, 270f * fraction, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }
            Box(
                Modifier
                    .size(150.dp)
                    .background(SunGoldSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(art), title,
                    Modifier
                        .size(118.dp)
                        .onGloballyPositioned { target = it.boundsInRoot().center }
                        .graphicsLayer { scaleX = bounce.value; scaleY = bounce.value },
                )
            }
            Text(
                "${(fraction * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Paper, RoundedCornerShape(50))
                    .border(2.dp, Line, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Ink, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkSoft, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        ProgressBar(fraction, ringColor, height = 14.dp)
        Spacer(Modifier.height(4.dp))
        Text(progressText, style = MaterialTheme.typography.labelLarge, color = Ink)
        if (hint != null) {
            Spacer(Modifier.height(6.dp))
            Text(hint, style = MaterialTheme.typography.bodySmall, color = InkSoft, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AMOUNTS.forEach { amount ->
                ChunkyButton(
                    "+$amount", { put(amount) }, color = buttonColor, enabled = coins >= amount,
                    height = 46.dp, sfx = null, modifier = Modifier.weight(1f),
                )
            }
            ChunkyButton(
                "Всё", { put(coins) }, color = ChunkyColor.GREEN, enabled = coins > 0,
                height = 46.dp, sfx = null, modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RoomShelf(state: GameState) {
    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(Paper, RoundedCornerShape(24.dp))
            .border(2.dp, Line, RoundedCornerShape(24.dp))
            .padding(14.dp),
    ) {
        Text("Сбывшиеся мечты", style = MaterialTheme.typography.titleMedium, color = Ink)
        Text("Всё, на что накопишь, появится в комнате питомца.", style = MaterialTheme.typography.bodySmall, color = InkSoft)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SAVINGS_GOALS.forEach { goal ->
                val owned = goal.id in state.ownedDecorations
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .background(if (owned) SunGoldSoft else Cream, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painterResource(goal.art), goal.title,
                            Modifier
                                .size(50.dp)
                                .graphicsLayer { alpha = if (owned) 1f else 0.25f },
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (owned) "есть!" else "${goal.target}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (owned) LeafGreen else InkSoft,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(@DrawableRes icon: Int, title: String, body: String, background: Color) {
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(background, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(icon), null, Modifier.size(46.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(2.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        }
    }
}
