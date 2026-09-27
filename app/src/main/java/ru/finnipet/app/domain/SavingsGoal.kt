package ru.finnipet.app.domain

data class SavingsGoal(
    val id: String,
    val title: String,
    val emoji: String,
    val target: Int,
)

val SAVINGS_GOALS: List<SavingsGoal> = listOf(
    SavingsGoal("goal_bed", "Мягкая подстилка", "🛏️", 150),
    SavingsGoal("goal_zoo", "Поход в зоопарк", "🦁", 300),
    SavingsGoal("goal_bike", "Велосипед", "🚲", 400),
    SavingsGoal("goal_house", "Большой домик", "🏠", 700),
)

fun savingsGoalById(id: String): SavingsGoal =
    SAVINGS_GOALS.firstOrNull { it.id == id } ?: SAVINGS_GOALS.first()

fun nextSavingsGoal(currentId: String): SavingsGoal {
    val index = SAVINGS_GOALS.indexOfFirst { it.id == currentId }
    val nextIndex = (index + 1).mod(SAVINGS_GOALS.size)
    return SAVINGS_GOALS[nextIndex]
}
