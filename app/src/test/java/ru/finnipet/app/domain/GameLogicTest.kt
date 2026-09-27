package ru.finnipet.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.finnipet.app.data.GameState

class GameLogicTest {

    @Test
    fun `mood is happy when stats are high`() {
        assertEquals(PetMood.HAPPY, deriveMood(hunger = 90, happiness = 80))
    }

    @Test
    fun `mood is sad when stats are low`() {
        assertEquals(PetMood.SAD, deriveMood(hunger = 10, happiness = 20))
    }

    @Test
    fun `mood is neutral in between`() {
        assertEquals(PetMood.NEUTRAL, deriveMood(hunger = 50, happiness = 40))
    }

    @Test
    fun `level increases every 50 xp`() {
        assertEquals(1, levelForXp(0))
        assertEquals(1, levelForXp(49))
        assertEquals(2, levelForXp(50))
        assertEquals(3, levelForXp(120))
    }

    @Test
    fun `rollover on the same day is a no-op`() {
        val state = GameState(currentDayEpoch = 100, coins = 5)
        val rolled = applyDayRollover(state, epochDay = 100)
        assertEquals(state, rolled)
    }

    @Test
    fun `rollover on the very first launch does not touch pet stats or streak`() {
        val state = GameState(currentDayEpoch = -1, hunger = 80, happiness = 80)
        val rolled = applyDayRollover(state, epochDay = 200)
        assertEquals(200L, rolled.currentDayEpoch)
        assertEquals(0, rolled.streakDays)
        assertEquals(80, rolled.hunger)
        assertEquals(80, rolled.happiness)
    }

    @Test
    fun `consecutive day after completing yesterday keeps the streak and decays stats`() {
        val state = GameState(
            currentDayEpoch = 100,
            lastStreakCreditEpoch = 100,
            streakDays = 3,
            hunger = 80,
            happiness = 80,
        )
        val rolled = applyDayRollover(state, epochDay = 101)
        assertEquals(3, rolled.streakDays)
        assertTrue(rolled.hunger < 80)
        assertTrue(rolled.happiness < 80)
    }

    @Test
    fun `missing a day resets the streak`() {
        val state = GameState(currentDayEpoch = 100, lastStreakCreditEpoch = 100, streakDays = 5)
        val rolled = applyDayRollover(state, epochDay = 105)
        assertEquals(0, rolled.streakDays)
    }

    @Test
    fun `not completing yesterday resets the streak even if consecutive`() {
        val state = GameState(currentDayEpoch = 100, lastStreakCreditEpoch = 99, streakDays = 4)
        val rolled = applyDayRollover(state, epochDay = 101)
        assertEquals(0, rolled.streakDays)
    }

    @Test
    fun `savings balance earns interest on rollover`() {
        val state = GameState(currentDayEpoch = 100, savingsBalance = 200)
        val rolled = applyDayRollover(state, epochDay = 101)
        assertTrue(rolled.savingsBalance > 200)
        assertEquals(rolled.savingsBalance - 200, rolled.lastInterestEarned)
    }

    @Test
    fun `badges are sticky once unlocked`() {
        val earned = applyBadgeUnlocks(GameState(xp = 10))
        assertTrue("first_step" in earned.unlockedBadgeIds)

        val afterReset = earned.copy(xp = 0)
        val stillUnlocked = applyBadgeUnlocks(afterReset)
        assertTrue("first_step" in stillUnlocked.unlockedBadgeIds)
    }

    @Test
    fun `balanced badge requires both a need and a want purchase`() {
        val onlyNeeds = GameState(totalNeedPurchases = 2, totalWantPurchases = 0)
        assertFalse("balanced" in applyBadgeUnlocks(onlyNeeds).unlockedBadgeIds)

        val both = GameState(totalNeedPurchases = 1, totalWantPurchases = 1)
        assertTrue("balanced" in applyBadgeUnlocks(both).unlockedBadgeIds)
    }

    @Test
    fun `todays questions are stable for the same epoch day`() {
        val batchA = todaysQuestions(epochDay = 42)
        val batchB = todaysQuestions(epochDay = 42)
        assertEquals(batchA.map { it.id }, batchB.map { it.id })
        assertEquals(5, batchA.size)
    }
}
