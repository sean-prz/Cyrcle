package com.github.se.cyrcle.ui.profile

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userRepository: UserRepository
  private lateinit var parkingRepository: ParkingRepository
  private lateinit var userViewModel: UserViewModel

  private lateinit var mockNavigationActions: NavigationActions

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)

    userRepository = MockUserRepository()
    parkingRepository = MockParkingRepository()
    userViewModel = UserViewModel(userRepository, parkingRepository)

    composeTestRule.setContent { ProfileScreen(mockNavigationActions, userViewModel) }
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testUserSignIn() {
    composeTestRule.onNodeWithTag("CreateProfileScreen").assertExists()

    userViewModel.setCurrentUser(TestInstancesUser.user1)
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("ViewProfileScreen"))

    composeTestRule.onNodeWithTag("ViewProfileScreen").assertExists()
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testUserDisconnects() {
    userViewModel.setCurrentUser(TestInstancesUser.user1)
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("ViewProfileScreen"))

    composeTestRule.onNodeWithTag("ViewProfileScreen").assertExists()

    userViewModel.setCurrentUser(null)
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("CreateProfileScreen"))

    composeTestRule.onNodeWithTag("CreateProfileScreen").assertExists()
  }
}
