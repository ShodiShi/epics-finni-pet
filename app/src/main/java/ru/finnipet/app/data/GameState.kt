package ru.finnipet.app.data

import kotlinx.serialization.Serializable

/**
 * Everything the game persists. New fields must have defaults so saves from older versions keep loading.
 */
@Serializable
data class GameState(
    val onboardingComplete: Boolean = false,
    val petName: String = "Финни",
    val soundOn: Boolean = true,
    val musicOn: Boolean = true,

    val coins: Int = 40,
    val hunger: Float = 80f,
    val happiness: Float = 80f,
    /** When hunger/happiness were last decayed; 0 until the first tick. */
    val statsUpdatedAt: Long = 0L,

    val inventory: Map<String, Int> = mapOf("item_apple" to 1, "item_ball" to 1),
    val totalNeedPurchases: Int = 0,
    val totalWantPurchases: Int = 0,

    val savingsBalance: Int = 0,
    val savingsGoalId: String = "goal_bed",
    val totalSaved: Int = 0,
    val goalsReached: Int = 0,
    val lastReachedGoalId: String? = null,
    val lastInterestEarned: Int = 0,
    val ownedDecorations: Set<String> = emptySet(),

    val shareBalance: Int = 0,
    val shareGoalId: String = "share_food",
    val totalShared: Int = 0,
    val completedShareGoals: Int = 0,
    val lastCompletedShareGoalId: String? = null,

    val xp: Int = 0,
    val totalCoinsEarned: Int = 0,
    val unlockedBadgeIds: Set<String> = emptySet(),

    val streakDays: Int = 0,
    val currentDayEpoch: Long = -1,
    val lastStreakCreditEpoch: Long = -1,
    val activeDays: Set<Long> = emptySet(),

    val todayQuestionIds: List<String> = emptyList(),
    val currentDayAnsweredIds: Set<String> = emptySet(),
    val correctToday: Int = 0,
    val coinsEarnedToday: Int = 0,
    val allTimeAnsweredIds: Set<String> = emptySet(),

    val todaySortCardIds: List<String> = emptyList(),
    val sortPlayedEpoch: Long = -1,
    val sortBestScore: Int = 0,
    val sortPerfectRounds: Int = 0,

    val caredToday: Boolean = false,
    val savedToday: Boolean = false,
    val chestClaimedEpoch: Long = -1,
    val petsToday: Int = 0,

    /** Days added by the hidden demo tools (Settings → tap the version 5 times) to show streaks and interest. */
    val debugDayOffset: Int = 0,
)
