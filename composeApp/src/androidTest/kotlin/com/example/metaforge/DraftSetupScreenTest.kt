package com.example.metaforge

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
 * UI tests for [DraftSetupScreen] — the entry point for every draft session.
 *
 * These tests drive the composable in isolation by supplying a freshly
 * instantiated [DraftSetupViewModel] directly (no Koin needed) and assert
 * on visible text nodes to cover three critical flows:
 *
 * 1. All tier + party chips render on first load.
 * 2. START is disabled until the user makes the required selections.
 * 3. Squad mode hides the per-slot pick/lane sections.
 */
@RunWith(AndroidJUnit4::class)
class DraftSetupScreenTest {

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

    // ─── Test 1: initial render ───────────────────────────────────────────────

    /**
     * All three tier chips (Epic / Legend / Mythic) and all four party-size
     * chips (Solo / Duo / Trio / Squad) must be visible on screen when the
     * user first opens Draft Configuration.
     */
    @Test
    fun draft_setup_shows_all_tier_and_party_chips() {
        setScreen()

        // Tier chips
        rule.onNodeWithText("Epic").assertIsDisplayed()
        rule.onNodeWithText("Legend").assertIsDisplayed()
        rule.onNodeWithText("Mythic").assertIsDisplayed()

        // Party size chips
        rule.onNodeWithText("Solo").assertIsDisplayed()
        rule.onNodeWithText("Duo").assertIsDisplayed()
        rule.onNodeWithText("Trio").assertIsDisplayed()
        rule.onNodeWithText("Squad").assertIsDisplayed()
    }

    // ─── Test 2: START disabled → enabled (Solo critical flow) ───────────────

    /**
     * With Solo selected, the user MUST choose exactly 1 pick slot AND 1
     * preferred lane before START SIMULATOR becomes enabled. Verifies the
     * canStart gate that prevents an accidental empty-selection launch.
     */
    @Test
    fun start_disabled_until_solo_slot_and_lane_are_selected() {
        setScreen()

        // Initially nothing selected → START must be disabled
        rule.onNodeWithText("START SIMULATOR").assertIsNotEnabled()

        // Pick a slot
        rule.onNodeWithText("Slot 1").performClick()

        // Still missing a lane → still disabled
        rule.onNodeWithText("START SIMULATOR").assertIsNotEnabled()

        // Pick a lane
        rule.onNodeWithText("Gold Lane").performClick()

        // Now both required selections made → START must be enabled
        rule.onNodeWithText("START SIMULATOR").assertIsEnabled()
    }

    // ─── Test 3: Squad hides pick-order and preferred-lane sections ──────────

    /**
     * Squad mode (party = 5) hides sections 4 (pick order) and 5 (preferred
     * lane) because recommendations cover all ally slots automatically.
     * Instead, a Squad-mode info card is displayed.
     */
    @Test
    fun squad_hides_pick_order_and_preferred_lane_sections() {
        setScreen()

        rule.onNodeWithText("Squad").performClick()

        // Slot radio buttons must not exist
        rule.onNodeWithText("Slot 1").assertDoesNotExist()
        rule.onNodeWithText("Slot 2").assertDoesNotExist()

        // Lane radio buttons must not exist
        rule.onNodeWithText("Gold Lane").assertDoesNotExist()
        rule.onNodeWithText("Jungle").assertDoesNotExist()

        // Squad info card should be visible instead
        rule.onNodeWithText("Squad mode", substring = true).assertIsDisplayed()
    }
}
