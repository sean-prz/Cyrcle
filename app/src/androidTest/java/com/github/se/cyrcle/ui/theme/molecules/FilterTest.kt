package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAddressRepository
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockOfflineParkingRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockPermissionHandler
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class FilterTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var mockParkingRepository: ParkingRepository
  private lateinit var mockOfflineParkingRepository: MockOfflineParkingRepository
  private lateinit var mockImageRepository: ImageRepository
  private lateinit var mockAddressRepository: MockAddressRepository
  private lateinit var mockReportedObjectRepository: ReportedObjectRepository
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var addressViewModel: AddressViewModel
  private lateinit var permissionHandler: MockPermissionHandler
  private lateinit var mapViewModel: MapViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    mockNavigationActions = mock(NavigationActions::class.java)
    mockParkingRepository = MockParkingRepository()
    mockOfflineParkingRepository = MockOfflineParkingRepository()
    mockImageRepository = MockImageRepository()
    mockReportedObjectRepository = MockReportedObjectRepository()
    mockAddressRepository = MockAddressRepository()
    mockUserRepository = MockUserRepository()

    userViewModel =
        UserViewModel(
            mockUserRepository,
            mockParkingRepository,
            mockImageRepository,
            MockAuthenticationRepository())
    addressViewModel = AddressViewModel(mockAddressRepository)
    mapViewModel = MapViewModel()
    permissionHandler = MockPermissionHandler()
    parkingViewModel =
        ParkingViewModel(
            mockImageRepository,
            userViewModel,
            mockParkingRepository,
            mockOfflineParkingRepository,
            mockReportedObjectRepository)

    parkingViewModel.addParking(TestInstancesParking.parking2)
    parkingViewModel.addParking(TestInstancesParking.parking3)
  }

  @Test
  fun testCCTVCheckboxInteraction() {
    composeTestRule.setContent {
      FilterPanel(
          parkingViewModel,
          true,
          addressViewModel,
          permissionHandler = permissionHandler,
          mapViewModel = mapViewModel,
          userViewModel = userViewModel,
          navigationActions = mockNavigationActions)
    }

    // Show filters first
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    // Check both checkbox and label are displayed
    composeTestRule.onNodeWithTag("CCTVCheckbox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("CCTVCheckboxLabel").assertIsDisplayed()
  }

  @Test
  fun testShowFiltersButtonInitiallyDisplaysShowFilters() {
    composeTestRule.setContent {
      FilterPanel(
          parkingViewModel,
          true,
          addressViewModel,
          permissionHandler = permissionHandler,
          mapViewModel = mapViewModel,
          userViewModel = userViewModel,
          navigationActions = mockNavigationActions)
    }

    // Act & Assert
    composeTestRule.onNodeWithTag("ShowFiltersButton").assertIsDisplayed().assertHasClickAction()
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testShowFiltersButtonTogglesFilterSection() {
    composeTestRule.setContent {
      FilterPanel(
          parkingViewModel,
          true,
          addressViewModel,
          permissionHandler = permissionHandler,
          mapViewModel = mapViewModel,
          userViewModel = userViewModel,
          navigationActions = mockNavigationActions)
    }

    // Act: Click to show filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("ShowFiltersButton"))

    // Assert that clear all and apply all buttons are displayed and behave as expected
    composeTestRule.onNodeWithTag("ClearAllFiltersButton").assertIsDisplayed().performClick()
    assert(parkingViewModel.selectedCapacities.value.isEmpty())
    assert(parkingViewModel.selectedProtection.value.isEmpty())
    assert(parkingViewModel.selectedRackTypes.value.isEmpty())
    composeTestRule.onNodeWithTag("ApplyAllFiltersButton").assertIsDisplayed().performClick()
    assert(parkingViewModel.selectedCapacities.value.isNotEmpty())
    assert(parkingViewModel.selectedProtection.value.isNotEmpty())
    assert(parkingViewModel.selectedRackTypes.value.isNotEmpty())

    // Act: Click to hide filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("ShowFiltersButton"))
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testProtectionFilters() {

    composeTestRule.setContent {
      FilterPanel(
          parkingViewModel,
          true,
          addressViewModel,
          permissionHandler = permissionHandler,
          mapViewModel = mapViewModel,
          userViewModel = userViewModel,
          navigationActions = mockNavigationActions)
    }

    // Act: Click to show filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("Protection"))

    composeTestRule
        .onNodeWithTag("Protection")
        .assertIsDisplayed()
        .assertTextEquals("Protection")
        .performClick()

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("ProtectionFilter"))
    composeTestRule.onNodeWithTag("ProtectionFilter").assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("ProtectionFilterItem")
        .assertCountEquals(ParkingProtection.entries.size)
        .assertAll(hasClickAction())
    composeTestRule.onAllNodesWithTag("ProtectionFilterItem").onFirst().performClick()
    assert(
        parkingViewModel.selectedProtection.value.containsAll(
            ParkingProtection.entries.toSet() - ParkingProtection.entries[0]))

    // Assert that clear and apply buttons work
    composeTestRule.onNodeWithTag("ClearProtectionButton").assertIsDisplayed().performClick()
    assert(parkingViewModel.selectedProtection.value.isEmpty())
    composeTestRule.onNodeWithTag("ApplyProtectionButton").assertIsDisplayed().performClick()
    assert(parkingViewModel.selectedProtection.value.isNotEmpty())
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testRackTypeFilters() {
    composeTestRule.setContent {
      FilterPanel(
          parkingViewModel,
          true,
          addressViewModel,
          permissionHandler = permissionHandler,
          mapViewModel = mapViewModel,
          userViewModel = userViewModel,
          navigationActions = mockNavigationActions)
    }

    // Act: Click to show filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("Rack Type"))

    composeTestRule
        .onNodeWithTag("Rack Type")
        .assertIsDisplayed()
        .assertTextEquals("Rack Type")
        .performClick()

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("RackTypeFilter"))
    composeTestRule.onNodeWithTag("RackTypeFilter").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("RackTypeFilterItem").assertAll(hasClickAction())
    composeTestRule.onAllNodesWithTag("RackTypeFilterItem").onFirst().performClick()

    // Assert that the selected rack type is updated in the ParkingViewModel
    assert(
        parkingViewModel.selectedRackTypes.value.containsAll(
            ParkingRackType.entries.toSet() - ParkingRackType.entries[0]))

    // Assert that clear and apply buttons work
    composeTestRule.onNodeWithTag("ClearRack TypeButton").assertIsDisplayed().performClick()
    assert(parkingViewModel.selectedRackTypes.value.isEmpty())
    composeTestRule.onNodeWithTag("ApplyRack TypeButton").assertIsDisplayed().performClick()
    assert(parkingViewModel.selectedRackTypes.value.isNotEmpty())
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testCapacityFilters() {
    composeTestRule.setContent {
      FilterPanel(
          parkingViewModel,
          true,
          addressViewModel,
          permissionHandler = permissionHandler,
          mapViewModel = mapViewModel,
          userViewModel = userViewModel,
          navigationActions = mockNavigationActions)
    }

    // Act: Click to show filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("Capacity"))
    composeTestRule
        .onNodeWithTag("Capacity")
        .assertIsDisplayed()
        .assertTextEquals("Capacity")
        .performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("CapacityFilter"))
    composeTestRule.onNodeWithTag("CapacityFilter").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("CapacityFilterItem").assertAll(hasClickAction())
    composeTestRule.onAllNodesWithTag("CapacityFilterItem").onFirst().performClick()
    assert(
        parkingViewModel.selectedCapacities.value.containsAll(
            ParkingCapacity.entries.toSet() - ParkingCapacity.entries[0]))

    // Assert that clear and apply buttons work
    composeTestRule.onNodeWithTag("ClearCapacityButton").assertIsDisplayed().performClick()
    assert(parkingViewModel.selectedCapacities.value.isEmpty())
    composeTestRule.onNodeWithTag("ApplyCapacityButton").assertIsDisplayed().performClick()
    assert(parkingViewModel.selectedCapacities.value.isNotEmpty())
  }
}
