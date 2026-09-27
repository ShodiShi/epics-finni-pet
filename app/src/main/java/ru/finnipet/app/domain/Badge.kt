package ru.finnipet.app.domain

import ru.finnipet.app.data.GameState

data class Badge(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val isUnlocked: (GameState) -> Boolean,
)

val BADGES: List<Badge> = listOf(
    Badge(
        id = "first_step",
        title = "Первые монеты",
        emoji = "🥉",
        description = "Заработай свои первые монеты",
        isUnlocked = { it.xp > 0 },
    ),
    Badge(
        id = "saver_bronze",
        title = "Бережливый",
        emoji = "🐷",
        description = "Накопи 100 монет в копилке",
        isUnlocked = { it.savingsBalance >= 100 },
    ),
    Badge(
        id = "saver_gold",
        title = "Мастер накоплений",
        emoji = "💰",
        description = "Накопи 500 монет в копилке",
        isUnlocked = { it.savingsBalance >= 500 },
    ),
    Badge(
        id = "balanced",
        title = "Умный хозяин",
        emoji = "⚖️",
        description = "Купи и еду, и игрушку для питомца",
        isUnlocked = { it.totalNeedPurchases > 0 && it.totalWantPurchases > 0 },
    ),
    Badge(
        id = "streak_3",
        title = "Три дня подряд",
        emoji = "🔥",
        description = "Занимайся 3 дня подряд",
        isUnlocked = { it.streakDays >= 3 },
    ),
    Badge(
        id = "streak_7",
        title = "Неделя подряд",
        emoji = "🌟",
        description = "Занимайся 7 дней подряд",
        isUnlocked = { it.streakDays >= 7 },
    ),
    Badge(
        id = "quiz_master",
        title = "Знаток финансов",
        emoji = "🎓",
        description = "Ответь на все вопросы хотя бы раз",
        isUnlocked = { it.allTimeAnsweredIds.size >= QUESTIONS.size },
    ),
)
