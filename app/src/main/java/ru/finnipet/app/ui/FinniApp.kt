package ru.finnipet.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.PetNeed
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.domain.chestReady
import ru.finnipet.app.domain.levelTitle
import ru.finnipet.app.domain.petNeed
import ru.finnipet.app.domain.quizDoneToday
import ru.finnipet.app.domain.sortDoneToday
import ru.finnipet.app.ui.components.Celebration
import ru.finnipet.app.ui.components.CelebrationOverlay
import ru.finnipet.app.ui.components.FinniBottomBar
import ru.finnipet.app.ui.components.Tab
import ru.finnipet.app.ui.fx.BackgroundMusic
import ru.finnipet.app.ui.fx.FxController
import ru.finnipet.app.ui.fx.FxLayer
import ru.finnipet.app.ui.fx.LocalFx
import ru.finnipet.app.ui.fx.LocalSound
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.SoundBoard
import ru.finnipet.app.ui.screens.BadgesScreen
import ru.finnipet.app.ui.screens.EarnScreen
import ru.finnipet.app.ui.screens.HomeScreen
import ru.finnipet.app.ui.screens.OnboardingScreen
import ru.finnipet.app.ui.screens.QuizScreen
import ru.finnipet.app.ui.screens.SavingsScreen
import ru.finnipet.app.ui.screens.SettingsScreen
import ru.finnipet.app.ui.screens.ShopScreen
import ru.finnipet.app.ui.screens.SortGameScreen
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.FinniPetTheme

/** Height of the floating tab bar, so tab screens can keep their content clear of it. */
val LocalBarInset = compositionLocalOf { 0.dp }

private val MODAL_ROUTES = setOf("quiz", "sort", "settings")

/** Activities a reward popup would interrupt; celebrations earned there wait until the kid leaves. */
private val FOCUS_ROUTES = setOf("quiz", "sort")

/** A beat between the moment that earned a reward and its popup, so the answer feedback lands first. */
private const val CELEBRATION_DELAY_MS = 700L

@Composable
fun FinniApp(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FinniPetTheme {
        val context = LocalContext.current
        val sound = remember { SoundBoard(context.applicationContext) }
        DisposableEffect(sound) { onDispose { sound.release() } }
        val fx = remember { FxController() }
        val sheets = remember { SheetHost() }
        val current = state
        sound.enabled = current?.soundOn ?: true
        BackgroundMusic(enabled = current?.musicOn == true && current.onboardingComplete, res = R.raw.music_loop)

        CompositionLocalProvider(LocalSound provides sound, LocalFx provides fx, LocalSheets provides sheets) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Cream)
            ) {
                when {
                    current == null -> Unit // the window background shows while the save loads
                    !current.onboardingComplete -> OnboardingScreen(onComplete = viewModel::completeOnboarding)
                    else -> MainScreens(current, viewModel)
                }
                FxLayer(fx)
            }
        }
    }
}

@Composable
private fun MainScreens(state: GameState, viewModel: GameViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tab = Tab.entries.firstOrNull { it.route == route }
    val sheets = LocalSheets.current
    val density = LocalDensity.current
    var barHeight by remember { mutableIntStateOf(0) }
    val celebrations = remember { mutableStateListOf<Celebration>() }
    val go = rememberUpdatedState<(Tab) -> Unit> { target -> nav.goToTab(target) }

    LaunchedEffect(viewModel) {
        var serial = 0
        viewModel.events.collect { event ->
            celebrations += event.toCelebration(serial++) { go.value(Tab.HOME) }
        }
    }

    val upcoming = celebrations.firstOrNull()
    val holdCelebrations = route in FOCUS_ROUTES
    var shownCelebration by remember { mutableStateOf<Celebration?>(null) }
    LaunchedEffect(upcoming, holdCelebrations) {
        if (upcoming == null || holdCelebrations) {
            shownCelebration = null
        } else {
            // Rewards earned together follow each other directly; only the first one waits.
            if (shownCelebration == null) delay(CELEBRATION_DELAY_MS)
            shownCelebration = upcoming
        }
    }

    val barInset: Dp = with(density) { barHeight.toDp() }
    CompositionLocalProvider(LocalBarInset provides barInset) {
        Box(Modifier.fillMaxSize()) {
            NavHost(
                navController = nav,
                startDestination = Tab.HOME.route,
                enterTransition = {
                    if (targetState.destination.route in MODAL_ROUTES) {
                        slideInVertically(tween(320)) { it / 5 } + fadeIn(tween(240))
                    } else {
                        fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.97f)
                    }
                },
                exitTransition = { fadeOut(tween(150)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = {
                    if (initialState.destination.route in MODAL_ROUTES) {
                        slideOutVertically(tween(260)) { it / 5 } + fadeOut(tween(200))
                    } else {
                        fadeOut(tween(150))
                    }
                },
            ) {
                composable(Tab.HOME.route) {
                    HomeScreen(
                        state = state,
                        onUseItem = viewModel::useItem,
                        onPet = viewModel::petThePet,
                        onClaimChest = viewModel::claimChest,
                        onOpenSettings = { nav.navigate("settings") },
                        onNavigate = go.value,
                    )
                }
                composable(Tab.EARN.route) {
                    EarnScreen(state, onStartQuiz = { nav.navigate("quiz") }, onStartSort = { nav.navigate("sort") })
                }
                composable(Tab.SHOP.route) {
                    ShopScreen(state, onBuy = viewModel::buyItem, onNavigate = go.value)
                }
                composable(Tab.SAVINGS.route) {
                    SavingsScreen(
                        state,
                        onDeposit = viewModel::deposit,
                        onShare = viewModel::share,
                        onAcknowledgeInterest = viewModel::acknowledgeInterest,
                    )
                }
                composable(Tab.BADGES.route) { BadgesScreen(state) }
                composable("settings") { SettingsScreen(state, viewModel, onBack = { nav.popBackStack() }) }
                composable("quiz") {
                    QuizScreen(state, onAnswer = viewModel::answerQuestion, onClose = { nav.popBackStack() })
                }
                composable("sort") {
                    SortGameScreen(
                        state,
                        onAnswer = viewModel::answerSortCard,
                        onFinish = viewModel::finishSortRound,
                        onClose = { nav.popBackStack() },
                    )
                }
            }

            AnimatedVisibility(
                visible = tab != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(tween(260)) { it },
                exit = slideOutVertically(tween(200)) { it },
            ) {
                FinniBottomBar(
                    current = tab,
                    attention = attentionTabs(state),
                    onSelect = { go.value(it) },
                    modifier = Modifier.onSizeChanged { barHeight = it.height },
                )
            }

            SheetLayer(sheets, state)
            CelebrationOverlay(shownCelebration, onDismiss = celebrations::remove)
        }
    }
}

private fun NavHostController.goToTab(tab: Tab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun attentionTabs(state: GameState): Set<Tab> = buildSet {
    if (chestReady(state) || petNeed(state) != PetNeed.NONE) add(Tab.HOME)
    if (!quizDoneToday(state) || !sortDoneToday(state)) add(Tab.EARN)
}

private fun GameEvent.toCelebration(serial: Int, goHome: () -> Unit): Celebration = when (this) {
    is GameEvent.BadgeUnlocked -> Celebration(
        key = "badge-$serial", title = "Новая награда!", subtitle = "«${badge.title}»\n${badge.earned}",
        art = badge.art, button = "Ура!", sfx = Sfx.FANFARE, pose = PetPose.CHEER,
    )
    is GameEvent.LevelUp -> Celebration(
        key = "level-$serial", title = "Уровень $level!", subtitle = "Теперь ты — ${levelTitle(level)}. Так держать!",
        art = R.drawable.ic_star, button = "Здорово!", sfx = Sfx.LEVEL_UP, pose = PetPose.HAPPY,
    )
    is GameEvent.GoalReached -> Celebration(
        key = "goal-$serial", title = "Мечта сбылась!", subtitle = "Мы накопили на мечту! ${goal.roomLine}",
        art = goal.art, button = "Посмотреть в комнате", sfx = Sfx.FANFARE, pose = PetPose.PIGGY, onAction = goHome,
    )
    is GameEvent.ShareGoalReached -> Celebration(
        key = "share-$serial", title = "Спасибо за доброту!", subtitle = goal.thanks,
        art = goal.art, button = "Пожалуйста!", sfx = Sfx.FANFARE, pose = PetPose.GIFT,
    )
}
