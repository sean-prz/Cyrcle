package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.cyrcle.model.parking.ParkingProtection
import org.junit.Rule
import org.junit.Test

class EnumDropDownTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun enumDropDownTest() {
    val tagD = "EnumDropDown"
    val tag1 = "EnumDropDown1"

    val parkingList = ParkingProtection.entries.toList()
    val defSelected = ParkingProtection.NONE
    val label = "Protection"
    val a1 = mutableStateOf(defSelected)
    val a2 = mutableStateOf(defSelected)

    composeTestRule.setContent {
      EnumDropDown(parkingList, a1, label)
      EnumDropDown(parkingList, a2, label, Modifier, tag1)
    }

    composeTestRule.onNodeWithTag(tagD, true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tagD}Label", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tagD).performClick()
    parkingList.withIndex().forEach { (_, _) -> }

    composeTestRule.onNodeWithTag(tag1, true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${tag1}Label", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag(tag1, true).performClick()
    parkingList.withIndex().forEach { (_, _) -> }
  }

  @Test
  fun enumDropDownTrailingIconTest() {
    val tag = "DropDownIcon"
    var expanded = false

    composeTestRule.setContent { DropDownTrailingIcon(expanded) }

    composeTestRule.onNodeWithTag(tag, true).assertIsDisplayed()

    expanded = true
    composeTestRule.onNodeWithTag(tag, true).assertIsDisplayed()
  }
}
