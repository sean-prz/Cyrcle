package com.github.se.cyrcle.ui.review

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockOfflineParkingRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockReviewRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.review.TestInstancesReview
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class AllReviewsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var parkingRepository: MockParkingRepository
  private lateinit var offlineParkingRepository: MockOfflineParkingRepository
  private lateinit var reviewRepository: MockReviewRepository
  private lateinit var userRepository: MockUserRepository
  private lateinit var imageRepository: MockImageRepository
  private lateinit var authenticator: MockAuthenticationRepository
  private lateinit var reportedObjectRepository: MockReportedObjectRepository

  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {

    navigationActions = mock(NavigationActions::class.java)
    parkingRepository = MockParkingRepository()
    offlineParkingRepository = MockOfflineParkingRepository()
    reviewRepository = MockReviewRepository()
    userRepository = MockUserRepository()
    imageRepository = MockImageRepository()
    authenticator = MockAuthenticationRepository()
    reportedObjectRepository = MockReportedObjectRepository()

    userViewModel = UserViewModel(userRepository, parkingRepository, imageRepository, authenticator)

    parkingViewModel =
        ParkingViewModel(
            imageRepository,
            userViewModel,
            parkingRepository,
            offlineParkingRepository,
            reportedObjectRepository)
    reviewViewModel = ReviewViewModel(reviewRepository, reportedObjectRepository)

    reviewViewModel.addReview(TestInstancesReview.review4)
    reviewViewModel.addReview(TestInstancesReview.review3)
    reviewViewModel.addReview(TestInstancesReview.review2)
    reviewViewModel.addReview(TestInstancesReview.review1)
    parkingViewModel.selectParking(TestInstancesParking.parking2)
  }

  @Test
  fun allReviewsScreen_displaysList() {

    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }
    userViewModel.setCurrentUser(TestInstancesUser.user1)

    composeTestRule.onNodeWithTag("ReviewCard0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ReviewCard-1").assertIsDisplayed()
  }

  @Test
  fun clickingReviewCard_expandsCard() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("ReviewCard0").performClick()
    composeTestRule.onNodeWithText("New Review.").assertIsDisplayed()

    composeTestRule.onNodeWithTag("YourReviewTitle").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("OtherReviewsTitle").assertIsDisplayed()
  }

  @Test
  fun signedInUser_seesTwoSectionTitles() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }

    composeTestRule.onNodeWithTag("YourReviewTitle").assertIsDisplayed()
  }

  @Test
  fun sortingReviewsChangesCardOrder() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
    }

    val firstReviewCardB4 = composeTestRule.onNodeWithTag("ReviewCard0")
    // Open filter and select sorting by rating
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.onNodeWithText("Sort By Score (Highest Score First)").performClick()

    // Verify if the reviews are sorted by rating by checking the order of ReviewCards
    val firstReviewCardAfter = composeTestRule.onNodeWithTag("ReviewCard0")
    assertNotEquals(firstReviewCardB4, firstReviewCardAfter)
  }

  @Test
  fun clickingAnotherReviewCard_expandsCorrectCard() {

    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }
    composeTestRule.onNodeWithTag("ReviewCard-1").performClick()
    composeTestRule.onNodeWithText("Bad Parking.").assertIsDisplayed()
  }

  @Test
  fun clickingFilterButton_displaysFilterOptions() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
    }

    // Click the filter button
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    // Verify that the filter options are displayed
    composeTestRule.onNodeWithText("Sort By Score (Highest Score First)").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sort By Date (Most Recent First)").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sort By Helpful (Most Helpful First)").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Sort By Interactions (Most Interactions First)")
        .assertIsDisplayed()
  }

  @Test
  fun clickingEditReviewButton_navigatesToReviewScreen() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }

    // Click the "Edit Review" button
    composeTestRule.onNodeWithTag("MoreOptions-1Button").performClick()
    composeTestRule.onNodeWithTag("MoreOptions-1EditReviewItem").performClick()

    verify(navigationActions).navigateTo(Screen.ADD_REVIEW)
  }

  @Test
  fun clickingAddReviewButton_navigatesToReviewScreen() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.newUser)
    }

    // Click the "Add Review" button
    composeTestRule.onNodeWithTag("AddReviewButton").performClick()

    verify(navigationActions).navigateTo(Screen.ADD_REVIEW)
  }

  @Test
  fun clickingDeleteReviewButton_doesntMoveIfUnneeded() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }
    composeTestRule.onNodeWithTag("ReviewCard-1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoreOptions-1Button").performClick()
    composeTestRule.onNodeWithTag("MoreOptions-1DeleteReviewItem").performClick()
    verify(navigationActions, times(0)).navigateTo(Screen.PARKING_DETAILS)
  }

  @Test
  fun addingLikeAndDislikesToReview_updatesReview() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ReviewCard0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ReviewCardContent0", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("ReviewActions0", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("LikeCount0", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("0")
    composeTestRule
        .onNodeWithTag("LikeButton0", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    composeTestRule.onNodeWithTag("LikeCount0", useUnmergedTree = true).assertTextEquals("1")

    composeTestRule
        .onNodeWithTag("DislikeCount0", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("0")
    composeTestRule
        .onNodeWithTag("DislikeButton0", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    composeTestRule.onNodeWithTag("DislikeCount0", useUnmergedTree = true).assertTextEquals("1")
    composeTestRule
        .onNodeWithTag("LikeCount0", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("0")
  }

  @Test
  fun sortReviewsByHelpfulAndInteractionsTest() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.newUser)
    }
    composeTestRule.waitForIdle()

    // Filter by likes
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.onNodeWithText("Sort By Helpful (Most Helpful First)").performClick()
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    // Normally the two first reviews should have 0 likes and dislikes. Let's assert that.
    composeTestRule.onNodeWithTag("LikeCount0", useUnmergedTree = true).assertTextEquals("0")
    composeTestRule.onNodeWithTag("DislikeCount0", useUnmergedTree = true).assertTextEquals("0")
    composeTestRule.onNodeWithTag("LikeCount1", useUnmergedTree = true).assertTextEquals("0")
    composeTestRule.onNodeWithTag("DislikeCount1", useUnmergedTree = true).assertTextEquals("0")

    // We like the review with index 1
    composeTestRule.onNodeWithTag("LikeButton1", useUnmergedTree = true).performClick()

    // Now that review 1 has 1 like, it should be the first review
    composeTestRule.onNodeWithTag("LikeCount0", useUnmergedTree = true).assertTextEquals("1")
    composeTestRule.onNodeWithTag("DislikeCount0", useUnmergedTree = true).assertTextEquals("0")
    composeTestRule.onNodeWithTag("LikeCount1", useUnmergedTree = true).assertTextEquals("0")
    composeTestRule.onNodeWithTag("DislikeCount1", useUnmergedTree = true).assertTextEquals("0")

    // Filter by interactions
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.onNodeWithText("Sort By Interactions (Most Interactions First)").performClick()
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    // We check that the first review is still first
    composeTestRule.onNodeWithTag("LikeCount0", useUnmergedTree = true).assertTextEquals("1")
    composeTestRule.onNodeWithTag("DislikeCount0", useUnmergedTree = true).assertTextEquals("0")
    composeTestRule.onNodeWithTag("LikeCount1", useUnmergedTree = true).assertTextEquals("0")
    composeTestRule.onNodeWithTag("DislikeCount1", useUnmergedTree = true).assertTextEquals("0")
  }

  @Test
  fun dropdownMenu_displaysOptions() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }
    val tag0 = "MoreOptions0"
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ReviewCard0").assertIsDisplayed()

    // Click the dropdown menu
    composeTestRule
        .onNodeWithTag("${tag0}Button")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    // Verify that the dropdown menu options are displayed
    composeTestRule.onNodeWithTag("${tag0}Menu").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${tag0}ReportReviewItem")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    verify(navigationActions).navigateTo(Screen.REVIEW_REPORT)
  }
}
