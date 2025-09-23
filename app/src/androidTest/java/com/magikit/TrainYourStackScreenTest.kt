package com.magikit

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrainYourStackScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInitialState_showsCheckAndRevealButtons() {
        composeTestRule.setContent {
            TrainYourStackScreen()
        }
        // Wait for the question to load
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Check").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Check").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reveal").assertIsDisplayed()
    }

    @Test
    fun testRevealButton_showsCorrectCardAndNextButton() {
        composeTestRule.setContent {
            TrainYourStackScreen()
        }
        // Wait for the question to load
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Reveal").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Reveal").performClick()
        composeTestRule.onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun testNextButton_resetsState() {
        composeTestRule.setContent {
            TrainYourStackScreen()
        }
        // Wait for the question to load
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Reveal").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Reveal").performClick()
        composeTestRule.onNodeWithText("Next").performClick()
        // Check and Reveal should be visible again
        composeTestRule.onNodeWithText("Check").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reveal").assertIsDisplayed()
    }
}
