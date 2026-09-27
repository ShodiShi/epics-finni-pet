package ru.finnipet.app.ui.screens

import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.Economy
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.domain.quizDoneToday
import ru.finnipet.app.domain.sortDoneToday
import ru.finnipet.app.ui.LocalBarInset
import ru.finnipet.app.ui.components.ChunkyButton
import ru.finnipet.app.ui.components.ChunkyColor
import ru.finnipet.app.ui.components.ProgressBar
import ru.finnipet.app.ui.components.SceneHeader
import ru.finnipet.app.ui.components.StatusBarScrim
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.FoxOrange
import ru.finnipet.app.ui.theme.FoxOrangeSoft
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.LeafGreenSoft
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SkyBlue
import ru.finnipet.app.ui.theme.SunGoldSoft
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private val FACTS = listOf(
    "Первые монеты появились больше 2500 лет назад!",
    "Раньше люди не пользовались деньгами, а меняли вещи друг на друга. Это называется «бартер».",
    "Слово «копейка» появилось из-за всадника с копьём, которого чеканили на монете.",
    "Рублю больше 700 лет — это одна из самых старых валют в мире.",
    "Самая большая золотая монета в мире весит целую тонну!",
    "Первые бумажные деньги придумали в Китае около тысячи лет назад.",
    "В России бумажные деньги — ассигнации — появились при Екатерине II.",
    "Банковским картам уже больше 70 лет.",
    "Деньги бывают наличными — монеты и купюры — и безналичными, например на карте.",
    "Взрослые часто копят на большие покупки несколько месяцев. Терпение — это суперсила!",
)

fun plural(n: Int, one: String, few: String, many: String): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
    }
}

@Composable
fun rememberUntilMidnight(): String {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = System.currentTimeMillis()
        }
    }
    val current = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val minutes = ChronoUnit.MINUTES.between(current, current.toLocalDate().plusDays(1).atStartOfDay()).coerceAtLeast(1)
    return if (minutes >= 60) "${minutes / 60} ч ${minutes % 60} мин" else "$minutes мин"
}

@Composable
fun EarnScreen(state: GameState, onStartQuiz: () -> Unit, onStartSort: () -> Unit) {
    val barInset = LocalBarInset.current
    val untilMidnight = rememberUntilMidnight()
    val answered = state.currentDayAnsweredIds.count { it in state.todayQuestionIds }
    val total = state.todayQuestionIds.size.coerceAtLeast(1)
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
                    background = R.drawable.bg_school,
                    title = "Учёба",
                    subtitle = "Узнавай новое — получай монеты",
                    pose = PetPose.TEACHER,
                    coins = state.coins,
                )
            }
            item { StreakCard(state) }
            item {
                val done = quizDoneToday(state)
                ActivityCard(
                    icon = R.drawable.ic_book,
                    title = "Вопросы дня",
                    description = "5 вопросов о деньгах. За каждый верный ответ — 10 монет.",
                    reward = "до +${Economy.QUESTIONS_PER_DAY * 10}",
                    progress = answered.toFloat() / total,
                    progressLabel = "$answered из $total",
                    done = done,
                    doneText = "Готово! Новые вопросы через $untilMidnight",
                    button = if (answered == 0) "Начать" else "Продолжить",
                    color = ChunkyColor.ORANGE,
                    onClick = onStartQuiz,
                )
            }
            item {
                val done = sortDoneToday(state)
                ActivityCard(
                    icon = R.drawable.sort_candy,
                    title = "Нужно или хочется?",
                    description = "Разложи ${Economy.SORT_CARDS_PER_ROUND} карточек: без чего не обойтись, а что — просто приятно.",
                    reward = "до +${Economy.SORT_CARDS_PER_ROUND * Economy.SORT_REWARD}",
                    progress = null,
                    progressLabel = if (state.sortBestScore > 0) "Лучший результат: ${state.sortBestScore} из ${Economy.SORT_CARDS_PER_ROUND}" else null,
                    done = done,
                    doneText = "Сыграно! Новая игра через $untilMidnight",
                    button = "Играть",
                    color = ChunkyColor.BLUE,
                    onClick = onStartSort,
                )
            }
            item {
                val fact = FACTS[(state.currentDayEpoch.coerceAtLeast(0) % FACTS.size).toInt()]
                Row(
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .background(SunGoldSoft, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(painterResource(R.drawable.ic_star), null, Modifier.size(44.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Знаешь ли ты?", style = MaterialTheme.typography.titleMedium, color = Ink)
                        Spacer(Modifier.height(2.dp))
                        Text(fact, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                    }
                }
            }
        }
        StatusBarScrim(scrolled, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun StreakCard(state: GameState) {
    val today = LocalDate.ofEpochDay(state.currentDayEpoch.coerceAtLeast(0))
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val letters = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(Paper, RoundedCornerShape(24.dp))
            .border(2.dp, Line, RoundedCornerShape(24.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.ic_flame), null,
                Modifier
                    .size(46.dp)
                    .alpha(if (state.streakDays > 0) 1f else 0.4f),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    if (state.streakDays > 0) {
                        "Серия: ${state.streakDays} ${plural(state.streakDays, "день", "дня", "дней")} подряд!"
                    } else {
                        "Начни серию сегодня!"
                    },
                    style = MaterialTheme.typography.titleMedium, color = Ink,
                )
                Text("Отвечай на вопросы дня каждый день", style = MaterialTheme.typography.bodySmall, color = InkSoft)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            letters.forEachIndexed { i, letter ->
                val day = monday.plusDays(i.toLong())
                val epoch = day.toEpochDay()
                val active = epoch in state.activeDays
                val isToday = day == today
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .background(if (active) FoxOrangeSoft else Cream, CircleShape)
                            .border(if (isToday) 3.dp else 0.dp, if (isToday) FoxOrange else Color.Transparent, CircleShape)
                            .alpha(if (day.isAfter(today)) 0.45f else 1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (active) {
                            Image(painterResource(R.drawable.ic_flame), null, Modifier.size(24.dp))
                        } else {
                            Box(Modifier.size(8.dp).background(Line, CircleShape))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(letter, style = MaterialTheme.typography.labelSmall, color = if (isToday) FoxOrange else InkSoft)
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    reward: String,
    progress: Float?,
    progressLabel: String?,
    done: Boolean,
    doneText: String,
    button: String,
    color: ChunkyColor,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(Paper, RoundedCornerShape(24.dp))
            .border(2.dp, if (done) LeafGreen.copy(alpha = 0.45f) else Line, RoundedCornerShape(24.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(72.dp)
                    .background(if (done) LeafGreenSoft else FoxOrangeSoft, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(icon), null, Modifier.size(56.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = Ink)
                Spacer(Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = InkSoft)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.ic_coin), null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(reward, style = MaterialTheme.typography.labelMedium, color = FoxOrange)
                    if (progressLabel != null) {
                        Spacer(Modifier.width(10.dp))
                        Text(progressLabel, style = MaterialTheme.typography.labelMedium, color = InkSoft)
                    }
                }
            }
        }
        if (progress != null && !done) {
            Spacer(Modifier.height(12.dp))
            ProgressBar(progress, SkyBlue)
        }
        Spacer(Modifier.height(14.dp))
        if (done) {
            Text(
                doneText,
                style = MaterialTheme.typography.labelLarge,
                color = LeafGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LeafGreenSoft, RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                textAlign = TextAlign.Center,
            )
        } else {
            ChunkyButton(button, onClick, color = color, modifier = Modifier.fillMaxWidth())
        }
    }
}
