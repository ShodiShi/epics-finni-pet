package ru.finnipet.app.domain

import androidx.annotation.DrawableRes
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState

data class Badge(
    val id: String,
    val title: String,
    @DrawableRes val art: Int,
    val description: String,
    /** Shown once the badge is won, so it reads as an achievement rather than a task. */
    val earned: String,
    /** (current, target) shown as a progress bar while the badge is still locked. */
    val progress: (GameState) -> Pair<Int, Int>,
) {
    fun isUnlocked(state: GameState): Boolean = progress(state).let { (current, target) -> current >= target }
}

val BADGES: List<Badge> = listOf(
    Badge("first_step", "Первые монеты", R.drawable.badge_first_step,
        "Заработай свои первые монеты", "Начало положено!") {
        it.totalCoinsEarned.coerceAtMost(1) to 1
    },
    Badge("saver_bronze", "Первая сотня", R.drawable.badge_saver_bronze,
        "Отложи в копилку 100 монет", "100 монет уже отложено в копилку.") {
        it.totalSaved to 100
    },
    Badge("balanced", "Умные покупки", R.drawable.badge_balanced,
        "Купи питомцу и еду, и игрушку", "Куплены и еда, и игрушка — всё по плану.") {
        ((if (it.totalNeedPurchases > 0) 1 else 0) + (if (it.totalWantPurchases > 0) 1 else 0)) to 2
    },
    Badge("kind_heart", "Доброе сердце", R.drawable.badge_kind_heart,
        "Помоги другим: собери одну добрую цель", "Первая добрая цель собрана. Спасибо!") {
        it.completedShareGoals to 1
    },
    Badge("sorter", "Знаток покупок", R.drawable.badge_sorter,
        "Без ошибок пройди игру «Нужно или хочется?»", "Игра «Нужно или хочется?» пройдена без единой ошибки.") {
        it.sortPerfectRounds to 1
    },
    Badge("streak_3", "Три дня подряд", R.drawable.badge_streak_3,
        "Отвечай на вопросы дня 3 дня подряд", "Три дня подряд с вопросами дня!") {
        it.streakDays to 3
    },
    Badge("quiz_master", "Знаток финансов", R.drawable.badge_quiz_master,
        "Ответь на все вопросы хотя бы раз", "Пройдены все вопросы игры!") {
        it.allTimeAnsweredIds.count { id -> QUESTIONS.any { q -> q.id == id } } to QUESTIONS.size
    },
    Badge("streak_7", "Неделя подряд", R.drawable.badge_streak_7,
        "Отвечай на вопросы дня 7 дней подряд", "Целая неделя с вопросами дня!") {
        it.streakDays to 7
    },
    Badge("saver_gold", "Мастер накоплений", R.drawable.badge_saver_gold,
        "Отложи в копилку 500 монет", "Уже 500 монет отложено в копилку!") {
        it.totalSaved to 500
    },
)

fun badge(id: String): Badge? = BADGES.firstOrNull { it.id == id }
