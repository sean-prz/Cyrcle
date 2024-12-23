package com.github.se.cyrcle.ui.map

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAddressRepository
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockOfflineParkingRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockPermissionHandler
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.io.datastore.PreferenceStorage
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.BikeLocationViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@RunWith(AndroidJUnit4::class)
class MapScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigation: NavigationActions
  private lateinit var mockReportedObjectRepository: ReportedObjectRepository
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var mapViewModel: MapViewModel
  private lateinit var permissionHandler: MockPermissionHandler
  private lateinit var addressViewModel: AddressViewModel
  private lateinit var bikeLocationViewModel: BikeLocationViewModel

  @Before
  fun setUp() {
    mockNavigation = mock(NavigationActions::class.java)
    mockReportedObjectRepository = MockReportedObjectRepository()

    val imageRepository = MockImageRepository()
    val parkingRepository = MockParkingRepository()
    val offlineParkingRepository = MockOfflineParkingRepository()
    val userRepository = MockUserRepository()
    val authenticationRepository = MockAuthenticationRepository()
    val addressRepository = MockAddressRepository()

    userViewModel =
        UserViewModel(userRepository, parkingRepository, imageRepository, authenticationRepository)

    parkingViewModel =
        ParkingViewModel(
            imageRepository,
            userViewModel,
            parkingRepository,
            offlineParkingRepository,
            mockReportedObjectRepository)
    mapViewModel = MapViewModel()
    permissionHandler = MockPermissionHandler()
    addressViewModel = AddressViewModel(addressRepository)

    bikeLocationViewModel = BikeLocationViewModel(PreferenceStorage(mock(Context::class.java)))

    `when`(mockNavigation.currentRoute()).thenReturn(Screen.MAP)
  }

  /**
   * Test to verify that the map screen and its components are displayed correctly.
   *
   * This test sets the content to the `MapScreen` and checks if the map screen, bottom navigation
   * bar, zoom controls, and add button are displayed.
   */
  @Test
  fun testMapIsDisplayed() {
    composeTestRule.setContent {
      MapScreen(
          mockNavigation,
          parkingViewModel,
          userViewModel,
          mapViewModel,
          permissionHandler,
          addressViewModel,
          bikeLocationViewModel)
    }

    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("NavigationBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SearchBar").assertIsDisplayed()
  }

  @Test
  fun testAddParkingRules() {
    composeTestRule.setContent {
      MapScreen(
          mockNavigation,
          parkingViewModel,
          userViewModel,
          mapViewModel,
          permissionHandler,
          addressViewModel,
          bikeLocationViewModel)
    }

    // Check that the add button has no click action when there is no user
    composeTestRule.onNodeWithTag("addButton").assertDoesNotExist()
    verifyNoInteractions(mockNavigation)

    userViewModel.setCurrentUser(TestInstancesUser.user1)

    // Check that the add button has a click action when there is a user
    composeTestRule.onNodeWithTag("addButton").performClick()
    verify(mockNavigation).navigateTo(Route.ADD_SPOTS)
  }

  /**
   * Test to verify that the recenter button has a click action.
   *
   * This test sets the content to the `MapScreen` and checks if the recenter button has a click
   * action. It then performs a click action on the recenter button and asserts that the focus mode
   * is toggled.
   */
  @Test
  fun testRecenterButton() {
    composeTestRule.setContent {
      MapScreen(
          mockNavigation,
          parkingViewModel,
          userViewModel,
          mapViewModel,
          permissionHandler,
          addressViewModel,
          bikeLocationViewModel)
    }

    // Start with enabled location
    permissionHandler.authorizeLoc.value = true

    // Assert that the recenter button is displayed
    composeTestRule.onNodeWithTag("recenterButton").assertIsDisplayed()

    // Assert that the recenter button has a click action
    composeTestRule.onNodeWithTag("recenterButton").assertHasClickAction()

    assert(mapViewModel.isTrackingModeEnable.value)

    // Remove location permission
    permissionHandler.authorizeLoc.value = false
    // Assert that the recenter button is displayed
    composeTestRule.onNodeWithTag("recenterButton").assertDoesNotExist()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testAddButtonNavigatesToLocationPicker() {
    val navigationActions = mock(NavigationActions::class.java)
    userViewModel.setCurrentUser(TestInstancesUser.user1)

    composeTestRule.setContent {
      MapScreen(
          navigationActions,
          parkingViewModel,
          userViewModel,
          mapViewModel,
          permissionHandler,
          addressViewModel,
          bikeLocationViewModel)
    }
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("addButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("addButton").performClick()

    verify(navigationActions).navigateTo(Route.ADD_SPOTS)
  }

  /**
   * Test to verify that settings menu is displayed and advanced mode switch is displayed when
   * settings button is clicked.
   */
  @Test
  fun testSettingsMenu() {
    composeTestRule.setContent {
      MapScreen(
          mockNavigation,
          parkingViewModel,
          userViewModel,
          mapViewModel,
          permissionHandler,
          addressViewModel,
          bikeLocationViewModel)
    }

    composeTestRule.onNodeWithTag("SettingsMenuButton").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithTag("SettingsMenuButton").performClick()

    composeTestRule.onNodeWithTag("SettingsMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("advancedModeSwitch").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SettingsToZoneRow").assertIsDisplayed().performClick()
    verify(mockNavigation).navigateTo(Route.ZONE)
  }

  @Test
  fun testFilterMenu() {
    composeTestRule.setContent {
      MapScreen(
          mockNavigation,
          parkingViewModel,
          userViewModel,
          mapViewModel,
          permissionHandler,
          addressViewModel,
          bikeLocationViewModel)
    }

    composeTestRule
        .onNodeWithTag("FilterButton")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    composeTestRule.onNodeWithTag("FilterMenu").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("FilterMenuClose")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    composeTestRule.onNodeWithTag("FilterMenu").assertIsNotDisplayed()
  }

  /** Test that verify that the Search Bar works */
  @Test
  fun testSearchBar() {
    composeTestRule.setContent {
      MapScreen(
          mockNavigation,
          parkingViewModel,
          userViewModel,
          mapViewModel,
          permissionHandler,
          addressViewModel,
          bikeLocationViewModel)
    }

    composeTestRule.onNodeWithTag("SearchBar").assertIsDisplayed()

    composeTestRule.onNodeWithTag("SearchBar").performTextInput("Test Location")
    composeTestRule.onNodeWithTag("SearchBar").assertTextEquals("Test Location")
  }

  /**
   * Test that verifies that using the search bar to look for a location triggers the apparition of
   * the Suggestion Menu
   */
  @Test
  fun testSuggestionsMenu() {
    composeTestRule.setContent {
      MapScreen(
          mockNavigation,
          parkingViewModel,
          userViewModel,
          mapViewModel,
          permissionHandler,
          addressViewModel,
          bikeLocationViewModel)
    }

    composeTestRule.onNodeWithTag("SearchBar").assertIsDisplayed()

    composeTestRule.onNodeWithTag("SearchBar").performTextInput("Mock Park Location")

    composeTestRule.onNodeWithTag("SearchBar").performImeAction()

    composeTestRule.onNodeWithTag("SuggestionsMenu").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("suggestionItemMock City")
        .assertIsDisplayed()
        .assertHasClickAction()
    composeTestRule
        .onNodeWithTag("suggestionItemMock City 2")
        .assertIsDisplayed()
        .assertHasClickAction()
  }

  @Test
  fun testPreviewCard() {
    composeTestRule.setContent {
      PreviewCard(navigationActions = mockNavigation, parkingViewModel = parkingViewModel)
      parkingViewModel.selectParking(TestInstancesParking.parking1)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("PreviewCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PreviewCardTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PreviewCardRackType").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PreviewCardProtection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PreviewCardCapacity").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("PreviewCardDetailsButton")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    verify(mockNavigation).navigateTo(Screen.PARKING_DETAILS)
  }
}
