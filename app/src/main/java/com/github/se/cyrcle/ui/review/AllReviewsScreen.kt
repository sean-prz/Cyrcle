package com.github.se.cyrcle.ui.review

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@SuppressLint("StateFlowValueCalledInComposition", "Range")
@Composable
fun AllReviewsScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel
) {
  val selectedParking =
      parkingViewModel.selectedParking.collectAsState().value
          ?: return Text(text = "No parking selected. Should not happen")

  // Track the selected card index
  val (selectedCardIndex, setSelectedCardIndex) = remember { mutableStateOf(-1) }

  Scaffold(
      topBar = {
        TopAppBar(
            navigationActions,
            stringResource(R.string.all_review_title)
                .format(selectedParking.optName ?: stringResource(R.string.default_parking_name)))
      },
      modifier = Modifier.testTag("AllReviewsScreen")) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("AllReviewsScreenBox")) {
              Column(
                  modifier = Modifier.fillMaxSize().padding(it),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.SpaceBetween) {
                    // LazyColumn for displaying reviews, each wrapped in a Card
                    LazyColumn(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .testTag("ReviewList")) {
                          reviewViewModel.getReviewsByParking(selectedParking.uid)
                          items(reviewViewModel.parkingReviews.value.size) { index ->
                            val curReview = reviewViewModel.parkingReviews.value[index]

                            if (index == selectedCardIndex) {
                              // Display the larger composable if this card is selected
                              Box(
                                  modifier =
                                      Modifier.fillMaxWidth(2f)
                                          .padding(8.dp)
                                          .background(MaterialTheme.colorScheme.secondaryContainer)
                                          .padding(16.dp)
                                          .testTag("ExpandedReviewBox$index")
                                          .clickable { setSelectedCardIndex(-1) }) {
                                    Column {
                                      Text(
                                          text =
                                              stringResource(R.string.all_review_rating)
                                                  .format(curReview.rating),
                                          fontWeight = FontWeight.Bold,
                                          color = MaterialTheme.colorScheme.primary,
                                          style = MaterialTheme.typography.titleLarge,
                                          modifier = Modifier.testTag("ExpandedReviewRating$index"))
                                      Spacer(modifier = Modifier.height(4.dp))
                                      Text(
                                          text =
                                              stringResource(R.string.all_review_text)
                                                  .format(curReview.text),
                                          color = MaterialTheme.colorScheme.onSecondaryContainer,
                                          style = MaterialTheme.typography.bodyMedium,
                                          modifier = Modifier.testTag("ExpandedReviewText$index"))
                                    }
                                  }
                            } else {
                              // Display the regular card layout if not selected
                              Card(
                                  modifier =
                                      Modifier.fillMaxWidth()
                                          .padding(8.dp)
                                          .shadow(8.dp, shape = MaterialTheme.shapes.medium)
                                          .clickable { setSelectedCardIndex(index) }
                                          .testTag("ReviewCard$index"),
                                  colors =
                                      CardDefaults.cardColors(
                                          containerColor =
                                              MaterialTheme.colorScheme.secondaryContainer),
                                  shape = MaterialTheme.shapes.medium,
                                  elevation = CardDefaults.cardElevation(8.dp)) {
                                    Column(
                                        modifier =
                                            Modifier.fillMaxWidth()
                                                .padding(16.dp)
                                                .testTag("ReviewCardContent$index")) {
                                          Text(
                                              text =
                                                  stringResource(R.string.all_review_rating)
                                                      .format(curReview.rating),
                                              fontWeight = FontWeight.Bold,
                                              color = MaterialTheme.colorScheme.primary,
                                              style = MaterialTheme.typography.titleLarge,
                                              modifier = Modifier.testTag("ReviewRating$index"))
                                          Spacer(modifier = Modifier.height(4.dp))
                                          Text(
                                              text =
                                                  stringResource(R.string.all_review_text)
                                                      .format(curReview.text),
                                              color =
                                                  MaterialTheme.colorScheme.onSecondaryContainer,
                                              style = MaterialTheme.typography.bodySmall,
                                              maxLines = 2,
                                              modifier = Modifier.testTag("ReviewText$index"))
                                        }
                                  }
                            }
                          }
                        }
                  }
            }
      }
}
