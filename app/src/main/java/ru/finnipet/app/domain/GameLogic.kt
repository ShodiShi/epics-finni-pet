package ru.finnipet.app.domain

import ru.finnipet.app.data.GameState
import kotlin.random.Random

private const val MILLIS_PER_DAY = 86_400_000L
private const val QUESTIONS_PER_DAY = 5
private const val XP_PER_LEVEL = 50
private const val DAILY_HUNGER_DECAY = 15
private const val DAILY_HAPPINESS_DECAY = 10

fun todayEpochDay(): Long = System.currentTimeMillis() / MILLIS_PER_DAY

enum class PetMood { HAPPY, NEUTRAL, SAD }

fun deriveMood(hunger: Int, happiness: Int): PetMood {
    val average = (hunger + happiness) / 2
    return when {
        average >= 60 -> PetMood.HAPPY
        average >= 30 -> PetMood.NEUTRAL
        else -> PetMood.SAD
    }
}

/** Deterministically picks today's quiz batch so it stays stable across recompositions/restarts. */
fun todaysQuestions(epochDay: Long): List<Question> =
    QUESTIONS.shuffled(Random(epochDay)).take(QUESTIONS_PER_DAY)

fun levelForXp(xp: Int): Int = xp / XP_PER_LEVEL + 1

/** Returns (xpIntoCurrentLevel, xpNeededForNextLevel). */
fun levelProgress(xp: Int): Pair<Int, Int> = (xp % XP_PER_LEVEL) to XP_PER_LEVEL

/** Unions in any newly-earned badges. Badges are sticky: once earned they are never removed. */
fun applyBadgeUnlocks(state: GameState): GameState {
    val newlyUnlocked = BADGES.filter { it.isUnlocked(state) }.map { it.id }
    return state.copy(unlockedBadgeIds = state.unlockedBadgeIds + newlyUnlocked)
}

/**
 * Rolls the pet's daily state forward when a new calendar day has started:
 * resets today's quiz batch, applies streak gain/loss, decays pet stats, and pays savings interest.
 */
fun applyDayRollover(state: GameState, epochDay: Long = todayEpochDay()): GameState {
    if (state.currentDayEpoch == epochDay) return state

    val wasYesterdayCompleted = state.lastStreakCreditEpoch == state.currentDayEpoch
    val isConsecutiveDay = state.currentDayEpoch >= 0 && epochDay == state.currentDayEpoch + 1
    val newStreak = when {
        state.currentDayEpoch < 0 -> 0
        isConsecutiveDay && wasYesterdayCompleted -> state.streakDays
        else -> 0
    }

    val interest = if (state.savingsBalance > 0) (state.savingsBalance / 20).coerceAtLeast(1) else 0
    val isFirstEverLaunch = state.currentDayEpoch < 0

    return applyBadgeUnlocks(
        state.copy(
            currentDayEpoch = epochDay,
            currentDayAnsweredIds = emptySet(),
            coinsEarnedToday = 0,
            streakDays = newStreak,
            savingsBalance = state.savingsBalance + interest,
            lastInterestEarned = interest,
            hunger = if (isFirstEverLaunch) state.hunger else (state.hunger - DAILY_HUNGER_DECAY).coerceAtLeast(0),
            happiness = if (isFirstEverLaunch) state.happiness else (state.happiness - DAILY_HAPPINESS_DECAY).coerceAtLeast(0),
        )
    )
}
