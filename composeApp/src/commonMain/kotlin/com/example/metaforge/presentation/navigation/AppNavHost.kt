package com.example.metaforge.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.example.metaforge.presentation.screens.counterpick.CounterPickScreen
import com.example.metaforge.presentation.screens.draft.DraftScreen
import com.example.metaforge.presentation.screens.draft.DraftSetupScreen
import com.example.metaforge.presentation.screens.draft.DraftViewModel
import com.example.metaforge.presentation.screens.heroselect.HeroInfoScreen
import com.example.metaforge.presentation.screens.heroselect.HeroListScreen
import com.example.metaforge.presentation.screens.heroselect.HeroSelectScreen
import com.example.metaforge.presentation.screens.home.HomeScreen
import com.example.metaforge.presentation.screens.settings.SettingsScreen
import com.example.metaforge.presentation.screens.synergy.SynergyScreen
import com.example.metaforge.presentation.theme.MetaForgeColors
import org.koin.compose.viewmodel.koinViewModel

// Route yang menampilkan BottomNav
private val bottomNavRoutes = listOf(
    Route.Home::class,
    Route.DraftSetup::class,
    Route.HeroList::class,
    Route.Settings::class
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavRoutes.any {
        currentDestination?.hasRoute(it) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MetaForgeBottomNav(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = MetaForgeColors.DeepNavy
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home,
            modifier = modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(250)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(250)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(250)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(250)
                )
            }
        ) {
            composable<Route.Home> {
                HomeScreen(
                    onNavigateToDraftSetup = {
                        navController.navigate(Route.DraftSetup)
                    },
                    onNavigateToHeroList = {
                        navController.navigate(Route.HeroList)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Route.Settings)
                    }
                )
            }
            composable<Route.DraftSetup> {
                DraftSetupScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStartDraft = { rank, party, pickPos, isFirst, role ->
                        navController.navigate(
                            Route.DraftSimActive(rank, party, pickPos, isFirst, role)
                        )
                    }
                )
            }
            composable<Route.DraftSimActive> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.DraftSimActive>()
                val draftViewModel: DraftViewModel = koinViewModel()
                LaunchedEffect(args) {
                    draftViewModel.setupDraft(
                        args.pickPosition, args.isFirstPick, args.preferredRole
                    )
                }
                DraftScreen(
                    isUserFirstPick = args.isFirstPick,
                    userPickPosition = args.pickPosition,
                    viewModel = draftViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHeroSelect = { slotIndex, isAlly, isBan ->
                        navController.navigate(Route.HeroSelect(slotIndex, isAlly, isBan))
                    },
                    onNavigateToCounter = { navController.navigate(Route.CounterPick) },
                    onNavigateToSynergy = { navController.navigate(Route.Synergy) }
                )
            }
            composable<Route.HeroSelect> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.HeroSelect>()
                HeroSelectScreen(
                    slotIndex = route.slotIndex,
                    isAlly = route.isAlly,
                    isBan = route.isBan,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Route.HeroList> {
                HeroListScreen(
                    onNavigateToHeroInfo = { id, name, role ->
                        navController.navigate(Route.HeroInfo(id, name, role))
                    }
                )
            }
            composable<Route.HeroInfo> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.HeroInfo>()
                HeroInfoScreen(
                    heroId = route.heroId,
                    heroName = route.heroName,
                    heroRole = route.heroRole,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Route.CounterPick> {
                CounterPickScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Route.Synergy> {
                SynergyScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Route.Settings> {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun MetaForgeBottomNav(
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (Any) -> Unit
) {
    NavigationBar(
        containerColor = MetaForgeColors.DarkCard,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hasRoute(item.route::class) == true
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold
                        else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MetaForgeColors.CyberBlue,
                    selectedTextColor = MetaForgeColors.CyberBlue,
                    unselectedIconColor = MetaForgeColors.TextSecondary,
                    unselectedTextColor = MetaForgeColors.TextSecondary,
                    indicatorColor = MetaForgeColors.CyberBlue.copy(alpha = 0.15f)
                )
            )
        }
    }
}