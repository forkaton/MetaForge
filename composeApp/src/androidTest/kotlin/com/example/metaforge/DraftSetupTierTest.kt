package com.example.metaforge

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.metaforge.presentation.screens.draft_setup.DraftSetupScreen
import com.example.metaforge.presentation.screens.draft_setup.DraftSetupViewModel
import com.example.metaforge.presentation.theme.MetaForgeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI test — Target Tier affects the ban-count label displayed in the header.
 *
 * Epic = 3 BANS / TEAM, Legend = 4 BANS / TEAM, Mythic = 5 BANS / TEAM.
 * This verifies that the business rule from [DraftSetupUiState.banCountPerSide]
 * is correctly surfaced in the UI when the user switches tiers.
 */
@RunWith(AndroidJUnit4::class)
class DraftSetupTierTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setScreen(vm: DraftSetupViewModel = DraftSetupViewModel()) {
        rule.setContent {
            MetaForgeTheme(isDark = true) {
                DraftSetupScreen(
                    onNavigateBack = {},
                    onStartDraft = { _, _, _, _, _, _ -> },
                    viewModel = vm
                )
            }
        }
    }

    @Test
    fun epic_tier_shows_3_bans_label() {
        setScreen()
        rule.onNodeWithText("Epic").performClick()
        rule.onNodeWithText("3 BANS / TEAM", substring = true).assertIsDisplayed()
    }

    @Test
    fun legend_tier_shows_4_bans_label() {
        setScreen()
        rule.onNodeWithText("Legend").performClick()
        rule.onNodeWithText("4 BANS / TEAM", substring = true).assertIsDisplayed()
    }

    @Test
    fun mythic_tier_shows_5_bans_label_by_default() {
        setScreen()
        // Mythic is the default rank — label shows 5 bans without clicking
        rule.onNodeWithText("5 BANS / TEAM", substring = true).assertIsDisplayed()
    }
}
