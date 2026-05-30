package com.example.metaforge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.metaforge.presentation.screens.counterpick.CounterPickScreen
import com.example.metaforge.presentation.screens.draft_arena.DraftScreen
import com.example.metaforge.presentation.screens.draft_setup.DraftSetupScreen
import com.example.metaforge.presentation.screens.hero_encyclopedia.HeroInfoScreen
import com.example.metaforge.presentation.screens.hero_encyclopedia.HeroListScreen
import com.example.metaforge.presentation.screens.hero_select.HeroSelectScreen
import com.example.metaforge.presentation.screens.home.HomeScreen
import com.example.metaforge.presentation.screens.settings.SettingsScreen

object Routes {
    const val HOME         = "home"
    const val DRAFT_SETUP  = "draft_setup"
    // picks and lanes are dash-separated (e.g. "1-3", "GOLD_LANE-JUNGLE").
    // Use "none" sentinel for Squad (no per-slot preference).
    const val DRAFT_ARENA  = "draft_arena/{rank}/{party}/{picks}/{isFirst}/{lanes}/{banCount}"
    const val HERO_SELECT  = "hero_select/{slot}/{isAlly}/{isBan}"
    const val COUNTER_PICK = "counter_pick"
    const val HERO_LIST    = "hero_list"
    const val HERO_INFO    = "hero_info/{heroId}"
    const val SETTINGS     = "settings"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToDraftSetup = { navController.navigate(Routes.DRAFT_SETUP) },
                onNavigateToHeroList   = { navController.navigate(Routes.HERO_LIST) },
                onNavigateToSettings   = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.DRAFT_SETUP) {
            DraftSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartDraft   = { rank, party, picks, isFirst, lanes, banCount ->
                    val picksArg = if (picks.isEmpty()) "none" else picks.joinToString("-")
                    val lanesArg = if (lanes.isEmpty()) "none" else lanes.joinToString("-")
                    navController.navigate(
                        Routes.DRAFT_ARENA
                            .replace("{rank}", rank)
                            .replace("{party}", party.toString())
                            .replace("{picks}", picksArg)
                            .replace("{isFirst}", isFirst.toString())
                            .replace("{lanes}", lanesArg)
                            .replace("{banCount}", banCount.toString())
                    )
                }
            )
        }

        composable(Routes.DRAFT_ARENA) { entry ->
            DraftScreen(
                rank      = entry.arguments?.getString("rank")    ?: "Mythic",
                party     = entry.arguments?.getString("party")?.toIntOrNull()  ?: 1,
                picksArg  = entry.arguments?.getString("picks")   ?: "1",
                isFirst   = entry.arguments?.getString("isFirst")?.toBoolean()  ?: true,
                lanesArg  = entry.arguments?.getString("lanes")   ?: "GOLD_LANE",
                banCount  = entry.arguments?.getString("banCount")?.toIntOrNull() ?: 5,
                onNavigateBack          = { navController.popBackStack() },
                onNavigateToHeroSelect  = { slot, isAlly, isBan ->
                    navController.navigate(
                        Routes.HERO_SELECT
                            .replace("{slot}",   slot.toString())
                            .replace("{isAlly}", isAlly.toString())
                            .replace("{isBan}",  isBan.toString())
                    )
                }
            )
        }

        composable(Routes.HERO_SELECT) { entry ->
            HeroSelectScreen(
                slotIndex = entry.arguments?.getString("slot")?.toIntOrNull()    ?: 0,
                isAlly    = entry.arguments?.getString("isAlly")?.toBoolean()   ?: true,
                isBan     = entry.arguments?.getString("isBan")?.toBoolean()    ?: false,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.COUNTER_PICK) {
            CounterPickScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.HERO_LIST) {
            HeroListScreen(
                onNavigateToHeroInfo = { heroId ->
                    navController.navigate(Routes.HERO_INFO.replace("{heroId}", heroId.toString()))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HERO_INFO) { entry ->
            val heroId = entry.arguments?.getString("heroId")?.toIntOrNull() ?: 0
            HeroInfoScreen(heroId = heroId, onNavigateBack = { navController.popBackStack() })
        }
    }
}
