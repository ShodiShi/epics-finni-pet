package ru.finnipet.app.domain

enum class ItemCategory { NEED, WANT }

data class ShopItem(
    val id: String,
    val name: String,
    val emoji: String,
    val cost: Int,
    val category: ItemCategory,
    val statGain: Int,
)

val SHOP_ITEMS: List<ShopItem> = listOf(
    ShopItem("item_apple", "Яблоко", "🍎", 15, ItemCategory.NEED, 15),
    ShopItem("item_carrot", "Морковка", "🥕", 10, ItemCategory.NEED, 10),
    ShopItem("item_fish", "Рыбка", "🐟", 25, ItemCategory.NEED, 25),
    ShopItem("item_milk", "Молоко", "🥛", 20, ItemCategory.NEED, 20),
    ShopItem("item_bread", "Хлеб", "🍞", 12, ItemCategory.NEED, 12),
    ShopItem("item_ball", "Мячик", "🎾", 20, ItemCategory.WANT, 20),
    ShopItem("item_bow", "Бантик", "🎀", 30, ItemCategory.WANT, 15),
    ShopItem("item_bear", "Плюшевый мишка", "🧸", 45, ItemCategory.WANT, 30),
    ShopItem("item_balloon", "Воздушный шар", "🎈", 18, ItemCategory.WANT, 12),
    ShopItem("item_cap", "Кепка", "🧢", 35, ItemCategory.WANT, 18),
)
