package ru.finnipet.app.data

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val onboardingComplete: Boolean = false,
    val petName: String = "Финни",

    val coins: Int = 30,
    val hunger: Int = 80,
    val happiness: Int = 80,

    val savingsBalance: Int = 0,
    val savingsGoalId: String = "goal_bed",
    val lastInterestEarned: Int = 0,

    val inventory: Map<String, Int> = emptyMap(),
    val totalNeedPurchases: Int = 0,
    val totalWantPurchases: Int = 0,

    val xp: Int = 0,
    val unlockedBadgeIds: Set<String> = emptySet(),

    val streakDays: Int = 0,
    val currentDayEpoch: Long = -1,
    val currentDayAnsweredIds: Set<String> = emptySet(),
    val coinsEarnedToday: Int = 0,
    val allTimeAnsweredIds: Set<String> = emptySet(),
    val lastStreakCreditEpoch: Long = -1,
)
