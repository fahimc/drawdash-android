package com.drawdash.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AppSmokeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun homeScreenAppears() {
        rule.waitUntil(2_000) {
            rule.onAllNodesWithText("DrawDash").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Play").assertIsDisplayed()
    }
}
