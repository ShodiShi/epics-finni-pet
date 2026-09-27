package ru.finnipet.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.finnipet.app.data.GameState
import java.time.LocalDate
import java.time.ZoneId

class GameLogicTest {

    private fun day(n: Long, base: GameState = GameState()) = applyDayRollover(base, n)

    @Test
    fun `first rollover picks five daily questions and a balanced sort deck`() {
        val s = day(100)
        assertEquals(5, s.todayQuestionIds.size)
        assertEquals(Economy.SORT_CARDS_PER_ROUND, s.todaySortCardIds.size)
        val cards = todaysSortCards(s)
        assertEquals(cards.size / 2, cards.count { it.isNeed })
    }

    @Test
    fun `daily questions prefer ones never answered`() {
        val seen = QUESTIONS.take(20).map { it.id }.toSet()
        val picked = pickDailyQuestions(seen, 7)
        assertTrue(picked.all { it !in seen })
    }

    @Test
    fun `daily picks are stable for the same day`() {
        assertEquals(pickDailyQuestions(emptySet(), 42), pickDailyQuestions(emptySet(), 42))
        assertEquals(pickSortDeck(42), pickSortDeck(42))
    }

    @Test
    fun `rollover on the same day changes nothing`() {
        val s = day(100)
        assertEquals(s, applyDayRollover(s, 100))
    }

    @Test
    fun `answering every question credits the streak once`() {
        var s = day(100)
        todaysQuestions(s).forEach { s = answerQuestion(s, it.id, it.correctIndex) }
        assertEquals(1, s.streakDays)
        assertEquals(50, s.coinsEarnedToday)
        assertTrue(100L in s.activeDays)
        assertTrue(quizDoneToday(s))
        val again = answerQuestion(s, s.todayQuestionIds.first(), 0)
        assertEquals(s, again)
    }

    @Test
    fun `wrong answers pay nothing but still count as answered`() {
        val s = day(100)
        val q = todaysQuestions(s).first()
        val after = answerQuestion(s, q.id, (q.correctIndex + 1) % q.options.size)
        assertEquals(s.coins, after.coins)
        assertTrue(q.id in after.currentDayAnsweredIds)
    }

    @Test
    fun `streak survives a completed yesterday and breaks after a skipped day`() {
        var s = day(100)
        todaysQuestions(s).forEach { s = answerQuestion(s, it.id, it.correctIndex) }
        val nextDay = applyDayRollover(s, 101)
        assertEquals(1, nextDay.streakDays)
        val skipped = applyDayRollover(s, 103)
        assertEquals(0, skipped.streakDays)
    }

    @Test
    fun `savings earn interest overnight`() {
        val s = day(100).copy(savingsBalance = 60)
        val next = applyDayRollover(s, 101)
        assertEquals(63, next.savingsBalance)
        assertEquals(3, next.lastInterestEarned)
    }

    @Test
    fun `reaching a savings goal moves the item into the room and starts the next goal`() {
        val s = day(100).copy(coins = 150)
        val after = deposit(s, 110)
        assertTrue("goal_bed" in after.ownedDecorations)
        assertEquals("goal_zoo", after.savingsGoalId)
        assertEquals(10, after.savingsBalance)
        assertEquals(1, after.goalsReached)
        assertEquals(40, after.coins)
        assertTrue(after.savedToday)
    }

    @Test
    fun `deposits never exceed the coins you have`() {
        val s = day(100).copy(coins = 7)
        val after = deposit(s, 50)
        assertEquals(0, after.coins)
        assertEquals(7, after.savingsBalance)
    }

    @Test
    fun `sharing completes good causes`() {
        val s = day(100).copy(coins = 100)
        val after = share(s, 45)
        assertEquals(1, after.completedShareGoals)
        assertEquals("share_books", after.shareGoalId)
        assertEquals(5, after.shareBalance)
        assertTrue("kind_heart" in applyBadgeUnlocks(after).unlockedBadgeIds)
    }

    @Test
    fun `buying needs money and fills the inventory`() {
        val broke = day(100).copy(coins = 5)
        assertEquals(broke, buyItem(broke, "item_fish"))
        val rich = broke.copy(coins = 100)
        val after = buyItem(rich, "item_fish")
        assertEquals(75, after.coins)
        assertEquals(1, after.inventory["item_fish"])
        assertEquals(1, after.totalNeedPurchases)
    }

    @Test
    fun `using food feeds Finni, toys cheer him up, and both count as care`() {
        val s = day(100).copy(hunger = 50f, happiness = 50f, inventory = mapOf("item_apple" to 1, "item_ball" to 2))
        val fed = useItem(s, "item_apple")
        assertEquals(65f, fed.hunger)
        assertFalse("item_apple" in fed.inventory)
        assertTrue(fed.caredToday)
        val played = useItem(fed, "item_ball")
        assertEquals(70f, played.happiness)
        assertEquals(1, played.inventory["item_ball"])
    }

    @Test
    fun `stats cap at one hundred`() {
        val s = day(100).copy(hunger = 95f, inventory = mapOf("item_fish" to 1))
        assertEquals(100f, useItem(s, "item_fish").hunger)
    }

    @Test
    fun `needs decay in real time`() {
        val start = 1_000_000_000L
        val s = day(100).copy(statsUpdatedAt = start, hunger = 80f, happiness = 80f)
        val later = applyDecay(s, start + 2 * 3_600_000L)
        assertEquals(70f, later.hunger, 0.01f)
        assertEquals(74f, later.happiness, 0.01f)
    }

    @Test
    fun `tick only persists decay once a displayed value changes`() {
        val start = LocalDate.of(2026, 9, 28).atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val s = applyDayRollover(GameState(), localEpochDay(start)).copy(statsUpdatedAt = start)
        assertEquals(s, tick(s, start + 60_000))
        assertNotEquals(s, tick(s, start + 3_600_000))
    }

    @Test
    fun `petting helps a little, a few times a day`() {
        var s = day(100).copy(happiness = 50f)
        repeat(Economy.MAX_PETS_PER_DAY + 3) { s = petThePet(s) }
        assertEquals(50f + Economy.MAX_PETS_PER_DAY * Economy.PET_HAPPINESS, s.happiness, 0.01f)
    }

    @Test
    fun `the chest opens only when all three quests are done, once a day`() {
        var s = day(100)
        assertFalse(chestReady(s))
        todaysQuestions(s).forEach { s = answerQuestion(s, it.id, it.correctIndex) }
        s = s.copy(caredToday = true, savedToday = true)
        assertTrue(chestReady(s))
        val claimed = claimChest(s)
        assertEquals(s.coins + Economy.CHEST_REWARD, claimed.coins)
        assertFalse(chestReady(claimed))
        assertEquals(claimed, claimChest(claimed))
    }

    @Test
    fun `sort round is marked played on the first answer`() {
        val s = day(100)
        val answered = answerSortCard(s, correct = true)
        assertTrue(sortDoneToday(answered))
        assertEquals(s.coins + Economy.SORT_REWARD, answered.coins)
        val perfect = finishSortRound(answered, 8, 8)
        assertEquals(1, perfect.sortPerfectRounds)
    }

    @Test
    fun `levels follow the growing thresholds`() {
        assertEquals(1, levelForXp(0))
        assertEquals(1, levelForXp(49))
        assertEquals(2, levelForXp(50))
        assertEquals(3, levelForXp(150))
        assertEquals(4, levelForXp(300))
        assertEquals(0 to 50, levelProgress(0))
        assertEquals(10 to 100, levelProgress(60))
    }

    @Test
    fun `badges are sticky`() {
        val rich = GameState(totalSaved = 150)
        val unlocked = applyBadgeUnlocks(rich)
        assertTrue("saver_bronze" in unlocked.unlockedBadgeIds)
        assertTrue("saver_bronze" in applyBadgeUnlocks(unlocked.copy(totalSaved = 0)).unlockedBadgeIds)
    }

    @Test
    fun `pet mood follows its needs`() {
        assertEquals(PetPose.HUNGRY, restingPose(GameState(hunger = 10f)))
        assertEquals(PetPose.SAD, restingPose(GameState(happiness = 10f)))
        assertEquals(PetPose.IDLE, restingPose(GameState()))
    }
}
