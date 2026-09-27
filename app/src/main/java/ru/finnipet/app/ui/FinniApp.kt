package ru.finnipet.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.finnipet.app.R
import ru.finnipet.app.ui.screens.BadgesScreen
import ru.finnipet.app.ui.screens.EarnScreen
import ru.finnipet.app.ui.screens.HomeScreen
import ru.finnipet.app.ui.screens.OnboardingScreen
import ru.finnipet.app.ui.screens.SavingsScreen
import ru.finnipet.app.ui.screens.SettingsScreen
import ru.finnipet.app.ui.screens.ShopScreen
import ru.finnipet.app.ui.theme.FinniPetTheme

private data class NavDestination(val route: String, val labelRes: Int, val icon: ImageVector)

private val NAV_ITEMS = listOf(
    NavDestination("home", R.string.nav_home, Icons.Filled.Home),
    NavDestination("earn", R.string.nav_earn, Icons.Filled.School),
    NavDestination("shop", R.string.nav_shop, Icons.Filled.ShoppingCart),
    NavDestination("savings", R.string.nav_savings, Icons.Filled.Savings),
    NavDestination("badges", R.string.nav_badges, Icons.Filled.EmojiEvents),
)

@Composable
fun FinniApp(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()

    FinniPetTheme {
        if (!state.onboardingComplete) {
            OnboardingScreen(onComplete = viewModel::completeOnboarding)
        } else {
            val navController = rememberNavController()
            Scaffold(
                bottomBar = {
                    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                    NavigationBar {
                        NAV_ITEMS.forEach { dest ->
                            NavigationBarItem(
                                selected = currentRoute == dest.route,
                                onClick = {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(dest.icon, contentDescription = null) },
                                label = { Text(stringResource(dest.labelRes)) },
                            )
                        }
                    }
                },
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.padding(padding),
                ) {
                    composable("home") {
                        HomeScreen(
                            state = state,
                            onFeed = viewModel::feedPet,
                            onPlay = viewModel::playWithPet,
                            onOpenSettings = { navController.navigate("settings") },
                        )
                    }
                    composable("earn") {
                        EarnScreen(state = state, onAnswer = viewModel::answerQuestion)
                    }
                    composable("shop") {
                        ShopScreen(state = state, onBuy = viewModel::buyItem)
                    }
                    composable("savings") {
                        SavingsScreen(
                            state = state,
                            onDeposit = viewModel::depositToSavings,
                            onNewGoal = viewModel::chooseNextSavingsGoal,
                            onAcknowledgeInterest = viewModel::acknowledgeInterest,
                        )
                    }
                    composable("badges") {
                        BadgesScreen(state = state)
                    }
                    composable("settings") {
                        SettingsScreen(
                            state = state,
                            onRename = viewModel::renamePet,
                            onReset = viewModel::resetProgress,
                        )
                    }
                }
            }
        }
    }
}
