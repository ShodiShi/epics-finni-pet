package ru.finnipet.app.domain

import androidx.annotation.DrawableRes
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt
import kotlin.random.Random

object Economy {
    const val QUESTIONS_PER_DAY = 5
    const val SORT_CARDS_PER_ROUND = 8
    const val SORT_REWARD = 2
    const val CHEST_REWARD = 25
    const val HUNGER_PER_HOUR = 5f
    const val HAPPINESS_PER_HOUR = 3f
    const val PET_HAPPINESS = 1.5f
    const val MAX_PETS_PER_DAY = 8
    const val INTEREST_PERCENT = 5
    const val MAX_STAT = 100f
    private const val ACTIVE_DAYS_KEPT = 35
    const val ACTIVE_DAYS_WINDOW = ACTIVE_DAYS_KEPT.toLong()
}

private const val MILLIS_PER_HOUR = 3_600_000f

fun localEpochDay(nowMillis: Long): Long =
    Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

/** The game's calendar day; the hidden demo tools can move it forward to show streaks and interest. */
fun effectiveDay(state: GameState, nowMillis: Long): Long = localEpochDay(nowMillis) + state.debugDayOffset

// ---- Daily content ------------------------------------------------------------------------------------------------

fun pickDailyQuestions(allTimeAnswered: Set<String>, epochDay: Long): List<String> {
    val random = Random(epochDay * 31 + 7)
    val unseen = QUESTIONS.filter { it.id !in allTimeAnswered }.shuffled(random)
    val seen = QUESTIONS.filter { it.id in allTimeAnswered }.shuffled(random)
    return (unseen + seen).take(Economy.QUESTIONS_PER_DAY).map { it.id }
}

fun pickSortDeck(epochDay: Long): List<String> {
    val random = Random(epochDay * 17 + 3)
    val half = Economy.SORT_CARDS_PER_ROUND / 2
    val needs = SORT_CARDS.filter { it.isNeed }.shuffled(random).take(half)
    val wants = SORT_CARDS.filterNot { it.isNeed }.shuffled(random).take(half)
    return (needs + wants).shuffled(random).map { it.id }
}

fun todaysQuestions(state: GameState): List<Question> = state.todayQuestionIds.mapNotNull(::question)

fun todaysSortCards(state: GameState): List<SortCard> = state.todaySortCardIds.mapNotNull(::sortCard)

fun quizDoneToday(state: GameState): Boolean =
    state.todayQuestionIds.isNotEmpty() && state.todayQuestionIds.all { it in state.currentDayAnsweredIds }

fun sortDoneToday(state: GameState): Boolean = state.sortPlayedEpoch == state.currentDayEpoch

private fun ensureDailyContent(state: GameState): GameState {
    var s = state
    if (s.todayQuestionIds.isEmpty()) s = s.copy(todayQuestionIds = pickDailyQuestions(s.allTimeAnsweredIds, s.currentDayEpoch))
    if (s.todaySortCardIds.isEmpty()) s = s.copy(todaySortCardIds = pickSortDeck(s.currentDayEpoch))
    return s
}

// ---- Time --------------------------------------------------------------------------------------------------------

/**
 * Starts a new calendar day: fresh quiz and sort deck, daily flags reset, streak kept or broken, interest paid.
 */
fun applyDayRollover(state: GameState, epochDay: Long): GameState {
    if (state.currentDayEpoch == epochDay) return ensureDailyContent(state)

    val firstLaunch = state.currentDayEpoch < 0
    val consecutive = !firstLaunch && epochDay == state.currentDayEpoch + 1
    val completedYesterday = state.lastStreakCreditEpoch == state.currentDayEpoch
    val streak = if (consecutive && completedYesterday) state.streakDays else 0

    val interest = if (!firstLaunch && state.savingsBalance > 0) {
        (state.savingsBalance * Economy.INTEREST_PERCENT / 100).coerceAtLeast(1)
    } else {
        0
    }

    val rolled = state.copy(
        currentDayEpoch = epochDay,
        streakDays = streak,
        todayQuestionIds = pickDailyQuestions(state.allTimeAnsweredIds, epochDay),
        currentDayAnsweredIds = emptySet(),
        correctToday = 0,
        coinsEarnedToday = 0,
        todaySortCardIds = pickSortDeck(epochDay),
        caredToday = false,
        savedToday = false,
        petsToday = 0,
        savingsBalance = state.savingsBalance + interest,
        totalSaved = state.totalSaved + interest,
        lastInterestEarned = interest,
        activeDays = state.activeDays.filter { it > epochDay - Economy.ACTIVE_DAYS_WINDOW }.toSet(),
    )
    return settleSavingsGoals(rolled)
}

/** Hunger and happiness slowly drop in real time. */
fun applyDecay(state: GameState, nowMillis: Long): GameState {
    if (state.statsUpdatedAt <= 0L || nowMillis <= state.statsUpdatedAt) return state.copy(statsUpdatedAt = nowMillis)
    val hours = (nowMillis - state.statsUpdatedAt) / MILLIS_PER_HOUR
    return state.copy(
        hunger = (state.hunger - Economy.HUNGER_PER_HOUR * hours).coerceAtLeast(0f),
        happiness = (state.happiness - Economy.HAPPINESS_PER_HOUR * hours).coerceAtLeast(0f),
        statsUpdatedAt = nowMillis,
    )
}

/**
 * The periodic clock step. Decay is only committed once it changes a displayed (rounded) value, so the
 * untouched timestamp keeps accumulating the fraction and nothing is written to disk every tick.
 */
fun tick(state: GameState, nowMillis: Long): GameState {
    val rolled = applyDayRollover(state, effectiveDay(state, nowMillis))
    val decayed = applyDecay(rolled, nowMillis)
    val visibleChange = decayed.hunger.roundToInt() != rolled.hunger.roundToInt() ||
        decayed.happiness.roundToInt() != rolled.happiness.roundToInt()
    return if (rolled.statsUpdatedAt <= 0L || visibleChange) decayed else rolled
}

// ---- Earning -----------------------------------------------------------------------------------------------------

fun answerQuestion(state: GameState, questionId: String, chosenIndex: Int): GameState {
    val q = question(questionId) ?: return state
    if (questionId in state.currentDayAnsweredIds) return state
    val correct = chosenIndex == q.correctIndex
    val reward = if (correct) q.reward else 0
    val answered = state.copy(
        coins = state.coins + reward,
        coinsEarnedToday = state.coinsEarnedToday + reward,
        totalCoinsEarned = state.totalCoinsEarned + reward,
        xp = state.xp + reward,
        correctToday = state.correctToday + if (correct) 1 else 0,
        currentDayAnsweredIds = state.currentDayAnsweredIds + questionId,
        allTimeAnsweredIds = state.allTimeAnsweredIds + questionId,
    )
    return if (quizDoneToday(answered) && answered.lastStreakCreditEpoch != answered.currentDayEpoch) {
        answered.copy(
            streakDays = answered.streakDays + 1,
            lastStreakCreditEpoch = answered.currentDayEpoch,
            activeDays = answered.activeDays + answered.currentDayEpoch,
        )
    } else {
        answered
    }
}

/** The first answer marks the round as today's, so leaving halfway can't be used to replay it. */
fun answerSortCard(state: GameState, correct: Boolean): GameState {
    val played = state.copy(sortPlayedEpoch = state.currentDayEpoch)
    if (!correct) return played
    return played.copy(
        coins = played.coins + Economy.SORT_REWARD,
        totalCoinsEarned = played.totalCoinsEarned + Economy.SORT_REWARD,
        xp = played.xp + 3,
    )
}

fun finishSortRound(state: GameState, score: Int, total: Int): GameState = state.copy(
    sortPlayedEpoch = state.currentDayEpoch,
    sortBestScore = maxOf(state.sortBestScore, score),
    sortPerfectRounds = state.sortPerfectRounds + if (total > 0 && score == total) 1 else 0,
)

// ---- Spending and caring -------------------------------------------------------------------------------------------

fun canAfford(state: GameState, item: ShopItem): Boolean = state.coins >= item.cost

fun buyItem(state: GameState, itemId: String): GameState {
    val item = shopItem(itemId) ?: return state
    if (!canAfford(state, item)) return state
    return state.copy(
        coins = state.coins - item.cost,
        inventory = state.inventory + (itemId to (state.inventory[itemId] ?: 0) + 1),
        totalNeedPurchases = state.totalNeedPurchases + if (item.category == ItemCategory.NEED) 1 else 0,
        totalWantPurchases = state.totalWantPurchases + if (item.category == ItemCategory.WANT) 1 else 0,
    )
}

fun useItem(state: GameState, itemId: String): GameState {
    val item = shopItem(itemId) ?: return state
    val owned = state.inventory[itemId] ?: 0
    if (owned <= 0) return state
    val inventory = if (owned == 1) state.inventory - itemId else state.inventory + (itemId to owned - 1)
    val cared = state.copy(inventory = inventory, caredToday = true, xp = state.xp + 2)
    return when (item.category) {
        ItemCategory.NEED -> cared.copy(hunger = (state.hunger + item.statGain).coerceAtMost(Economy.MAX_STAT))
        ItemCategory.WANT -> cared.copy(happiness = (state.happiness + item.statGain).coerceAtMost(Economy.MAX_STAT))
    }
}

fun ownedItems(state: GameState, category: ItemCategory): List<Pair<ShopItem, Int>> =
    SHOP_ITEMS.filter { it.category == category }
        .mapNotNull { item -> state.inventory[item.id]?.takeIf { it > 0 }?.let { item to it } }

fun petThePet(state: GameState): GameState {
    if (state.petsToday >= Economy.MAX_PETS_PER_DAY) return state.copy(petsToday = state.petsToday + 1)
    return state.copy(
        happiness = (state.happiness + Economy.PET_HAPPINESS).coerceAtMost(Economy.MAX_STAT),
        petsToday = state.petsToday + 1,
    )
}

// ---- Saving and sharing --------------------------------------------------------------------------------------------

fun deposit(state: GameState, amount: Int): GameState {
    val coins = amount.coerceIn(0, state.coins)
    if (coins == 0) return state
    return settleSavingsGoals(
        state.copy(
            coins = state.coins - coins,
            savingsBalance = state.savingsBalance + coins,
            totalSaved = state.totalSaved + coins,
            savedToday = true,
            xp = state.xp + (coins / 5).coerceAtLeast(1),
        )
    )
}

/** Every goal the piggy bank covers is "bought": it moves into Finni's room and the next goal starts. */
fun settleSavingsGoals(state: GameState): GameState {
    var s = state
    var goal = savingsGoalById(s.savingsGoalId)
    while (s.savingsBalance >= goal.target) {
        s = s.copy(
            savingsBalance = s.savingsBalance - goal.target,
            ownedDecorations = s.ownedDecorations + goal.id,
            goalsReached = s.goalsReached + 1,
            lastReachedGoalId = goal.id,
            savingsGoalId = nextSavingsGoal(goal.id).id,
        )
        goal = savingsGoalById(s.savingsGoalId)
    }
    return s
}

fun share(state: GameState, amount: Int): GameState {
    val coins = amount.coerceIn(0, state.coins)
    if (coins == 0) return state
    var s = state.copy(
        coins = state.coins - coins,
        shareBalance = state.shareBalance + coins,
        totalShared = state.totalShared + coins,
        savedToday = true,
        xp = state.xp + (coins / 4).coerceAtLeast(1),
    )
    var goal = shareGoalById(s.shareGoalId)
    while (s.shareBalance >= goal.target) {
        s = s.copy(
            shareBalance = s.shareBalance - goal.target,
            completedShareGoals = s.completedShareGoals + 1,
            lastCompletedShareGoalId = goal.id,
            shareGoalId = nextShareGoal(goal.id).id,
        )
        goal = shareGoalById(s.shareGoalId)
    }
    return s
}

// ---- Daily quests ------------------------------------------------------------------------------------------------

enum class DailyQuest(val title: String, @DrawableRes val icon: Int) {
    QUIZ("Ответь на все вопросы дня", R.drawable.ic_book),
    CARE("Покорми питомца или поиграй с ним", R.drawable.ic_heart),
    SAVE("Положи монеты в копилку или поделись", R.drawable.ic_piggy),
}

fun isQuestDone(quest: DailyQuest, state: GameState): Boolean = when (quest) {
    DailyQuest.QUIZ -> quizDoneToday(state)
    DailyQuest.CARE -> state.caredToday
    DailyQuest.SAVE -> state.savedToday
}

fun questsDone(state: GameState): Int = DailyQuest.entries.count { isQuestDone(it, state) }

fun chestClaimedToday(state: GameState): Boolean = state.chestClaimedEpoch == state.currentDayEpoch

fun chestReady(state: GameState): Boolean = questsDone(state) == DailyQuest.entries.size && !chestClaimedToday(state)

fun claimChest(state: GameState): GameState {
    if (!chestReady(state)) return state
    return state.copy(
        coins = state.coins + Economy.CHEST_REWARD,
        totalCoinsEarned = state.totalCoinsEarned + Economy.CHEST_REWARD,
        xp = state.xp + 15,
        chestClaimedEpoch = state.currentDayEpoch,
    )
}

// ---- Progression -------------------------------------------------------------------------------------------------

/** XP needed to reach a level: 0, 50, 150, 300, 500, 750… */
fun levelThreshold(level: Int): Int = 25 * level * (level - 1)

fun levelForXp(xp: Int): Int {
    var level = 1
    while (levelThreshold(level + 1) <= xp) level++
    return level
}

/** (xp into the current level, xp the level spans). */
fun levelProgress(xp: Int): Pair<Int, Int> {
    val level = levelForXp(xp)
    val base = levelThreshold(level)
    return (xp - base) to (levelThreshold(level + 1) - base)
}

/** Rank names are ones Russian uses for girls and boys alike. */
fun levelTitle(level: Int): String = when (level) {
    1 -> "Новичок"
    2 -> "Умница"
    3 -> "Знаток монет"
    4 -> "Мастер копилки"
    5 -> "Эксперт бюджета"
    6 -> "Финансовый гений"
    else -> "Легенда финансов"
}

/** Unions in newly earned badges. Badges are sticky: once earned they are never removed. */
fun applyBadgeUnlocks(state: GameState): GameState {
    val earned = BADGES.filter { it.isUnlocked(state) }.map { it.id }
    val union = state.unlockedBadgeIds + earned
    return if (union == state.unlockedBadgeIds) state else state.copy(unlockedBadgeIds = union)
}
