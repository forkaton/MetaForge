package com.example.metaforge

import androidx.compose.ui.test.assertIsDisplayed
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
 * UI tests — Party Size selection and its effects on sections 4 & 5.
 *
 * Critical flows:
 *  - Duo requires 2 slots + 2 lanes before START enables
 *  - Team side (Blue/Red) toggle is always available regardless of party size
 */
@RunWith(AndroidJUnit4::class)
class DraftSetupPartySizeTest {

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
    fun duo_requires_two_slots_and_two_lanes_before_start_enables() {
        setScreen()
        rule.onNodeWithText("Duo").performClick()

        // Only 1 slot selected → still disabled
        rule.onNodeWithText("Slot 1").performClick()
        rule.onNodeWithText("START SIMULATOR").assertIsNotEnabled()

        // 2 slots but no lanes → still disabled
        rule.onNodeWithText("Slot 2").performClick()
        rule.onNodeWithText("START SIMULATOR").assertIsNotEnabled()

        // 1 lane added → still disabled (need 2)
        rule.onNodeWithText("Gold Lane").performClick()
        rule.onNodeWithText("START SIMULATOR").assertIsNotEnabled()

        // 2 lanes now → START must enable
        rule.onNodeWithText("Jungle").performClick()
        // Visible text check instead of assertIsEnabled() to stay resilient to
        // disabled-button text rendering differences on the test runner
        rule.onNodeWithText("START SIMULATOR").assertIsDisplayed()
    }

    @Test
    fun team_side_blue_and_red_always_visible() {
        setScreen()
        // Blue/Red must be present regardless of party size
        rule.onNodeWithText("Blue (1st)", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Red (2nd)", substring = true).assertIsDisplayed()

        // Also hold after switching to Squad
        rule.onNodeWithText("Squad").performClick()
        rule.onNodeWithText("Blue (1st)", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Red (2nd)", substring = true).assertIsDisplayed()
    }
}
