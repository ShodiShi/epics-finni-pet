package ru.finnipet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.ItemCategory
import ru.finnipet.app.domain.SHOP_ITEMS
import ru.finnipet.app.domain.ShopItem

@Composable
fun ShopScreen(state: GameState, onBuy: (String) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val needs = SHOP_ITEMS.filter { it.category == ItemCategory.NEED }
    val wants = SHOP_ITEMS.filter { it.category == ItemCategory.WANT }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(stringResource(R.string.shop_title, state.petName), style = MaterialTheme.typography.headlineMedium)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    stringResource(R.string.shop_section_need),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(needs) { item ->
                ShopItemCard(
                    item = item,
                    coins = state.coins,
                    owned = state.inventory[item.id] ?: 0,
                    onBuy = {
                        if (state.coins >= item.cost) {
                            onBuy(item.id)
                            scope.launch { snackbarHostState.showSnackbar("Куплено: ${item.name}") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Не хватает монет 🪙") }
                        }
                    },
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    stringResource(R.string.shop_section_want),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(wants) { item ->
                ShopItemCard(
                    item = item,
                    coins = state.coins,
                    owned = state.inventory[item.id] ?: 0,
                    onBuy = {
                        if (state.coins >= item.cost) {
                            onBuy(item.id)
                            scope.launch { snackbarHostState.showSnackbar("Куплено: ${item.name}") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Не хватает монет 🪙") }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ShopItemCard(item: ShopItem, coins: Int, owned: Int, onBuy: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(item.emoji, style = MaterialTheme.typography.headlineMedium)
            Text(item.name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(
                text = stringResource(R.string.shop_price_format, item.cost),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (owned > 0) {
                Text(
                    text = "${stringResource(R.string.shop_owned_label)}: $owned",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Button(onClick = onBuy, modifier = Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.shop_buy_button))
            }
        }
    }
}
