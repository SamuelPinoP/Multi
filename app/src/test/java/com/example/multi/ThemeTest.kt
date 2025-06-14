package com.example.multi

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.multi.ui.theme.DarkGreen
import com.example.multi.ui.theme.MultiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class ThemeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun primaryColor_isDarkGreen_inLightTheme() {
        var primary: Color? = null
        composeTestRule.setContent {
            MultiTheme(darkTheme = false, dynamicColor = false) {
                primary = MaterialTheme.colorScheme.primary
            }
        }
        composeTestRule.runOnIdle {
            assertEquals(DarkGreen, primary)
        }
    }

    @Test
    fun primaryColor_isDarkGreen_inDarkTheme() {
        var primary: Color? = null
        composeTestRule.setContent {
            MultiTheme(darkTheme = true, dynamicColor = false) {
                primary = MaterialTheme.colorScheme.primary
            }
        }
        composeTestRule.runOnIdle {
            assertEquals(DarkGreen, primary)
        }
    }
}
