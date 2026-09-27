package ru.finnipet.app.domain

import androidx.annotation.DrawableRes
import ru.finnipet.app.R

enum class ItemCategory { NEED, WANT }

/** Pastel family used behind an item's artwork. */
enum class Tone { PINK, ORANGE, BLUE, GOLD, GREEN, PURPLE }

data class ShopItem(
    val id: String,
    val name: String,
    @DrawableRes val art: Int,
    val cost: Int,
    val category: ItemCategory,
    val statGain: Int,
    val tone: Tone,
)

val SHOP_ITEMS: List<ShopItem> = listOf(
    ShopItem("item_apple", "Яблоко", R.drawable.item_apple, 15, ItemCategory.NEED, 15, Tone.PINK),
    ShopItem("item_carrot", "Морковка", R.drawable.item_carrot, 10, ItemCategory.NEED, 10, Tone.ORANGE),
    ShopItem("item_bread", "Хлебушек", R.drawable.item_bread, 12, ItemCategory.NEED, 12, Tone.GOLD),
    ShopItem("item_milk", "Молоко", R.drawable.item_milk, 20, ItemCategory.NEED, 20, Tone.BLUE),
    ShopItem("item_fish", "Рыбка", R.drawable.item_fish, 25, ItemCategory.NEED, 25, Tone.GREEN),
    ShopItem("item_ball", "Мячик", R.drawable.item_ball, 20, ItemCategory.WANT, 20, Tone.BLUE),
    ShopItem("item_balloon", "Шарик", R.drawable.item_balloon, 18, ItemCategory.WANT, 12, Tone.PINK),
    ShopItem("item_bow", "Бантик", R.drawable.item_bow, 30, ItemCategory.WANT, 15, Tone.PURPLE),
    ShopItem("item_cap", "Кепка", R.drawable.item_cap, 35, ItemCategory.WANT, 18, Tone.GREEN),
    ShopItem("item_bear", "Мишка", R.drawable.item_bear, 45, ItemCategory.WANT, 30, Tone.GOLD),
)

fun shopItem(id: String): ShopItem? = SHOP_ITEMS.firstOrNull { it.id == id }
