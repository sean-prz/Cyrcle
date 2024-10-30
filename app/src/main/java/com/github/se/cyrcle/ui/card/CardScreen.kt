package com.github.se.cyrcle.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Composable
fun CardScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory)
) {
    val selectedParking =
        parkingViewModel.selectedParking.collectAsState().value
            ?: return Text(text = "No parking selected. Should not happen")

    Scaffold(
        topBar = {
            TopAppBar(navigationActions, "Description of ${selectedParking.optName?: "Parking"}")
        },
        modifier = Modifier.testTag("CardScreen")) {
        Box(
            modifier =
            Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("CardScreenBox") // Test tag for main container
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween) {
                // Display a row of images using LazyRow
                LazyRow(
                    modifier =
                    Modifier.fillMaxWidth()
                        .padding(8.dp)
                        .testTag("ParkingImagesRow"), // Test tag for image row
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(selectedParking.images.size) { index ->
                        AsyncImage(
                            model = selectedParking.images[index],
                            contentDescription = "Image $index",
                            modifier =
                            Modifier.size(170.dp)
                                .padding(2.dp)
                                .fillMaxWidth()
                                .testTag("ParkingImage$index"), // Test tag for each image
                            contentScale = ContentScale.Crop)
                    }
                }

                // Column for parking info such as capacity, rack type, protection, etc.
                Column(
                    modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("InfoColumn"), // Test tag for info column
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Row for displaying capacity and rack type
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("RowCapacityRack"),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f).testTag("CapacityColumn")) {
                            Text(text = "Capacity:", fontWeight = FontWeight.Bold)
                            Text(
                                text = selectedParking.capacity.description,
                                color = Color.Gray)
                        }
                        Column(modifier = Modifier.weight(1f).testTag("RackTypeColumn")) {
                            Text(text = "Rack Type:", fontWeight = FontWeight.Bold)
                            Text(
                                text = selectedParking.rackType.description,
                                color = Color.Gray)
                        }
                    }

                    // Row for displaying protection and price
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("RowProtectionPrice"),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f).testTag("ProtectionColumn")) {
                            Text(text = "Protection:", fontWeight = FontWeight.Bold)
                            Text(
                                text = selectedParking.protection.description,
                                color = Color.Gray)
                        }
                        Column(modifier = Modifier.weight(1f).testTag("PriceColumn")) {
                            Text(text = "Price:", fontWeight = FontWeight.Bold)
                            val price = selectedParking.price
                            Text(
                                text = if (price == 0.0) "Free" else "$price",
                                color = Color.Gray)
                        }
                    }

                    // Row for displaying if security is present
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("RowSecurity"),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f).testTag("SecurityColumn")) {
                            Text(text = "Security Present:", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (selectedParking.hasSecurity) "Yes" else "No",
                                color = Color.Gray)
                        }
                        Column(modifier = Modifier.weight(1f).testTag("AverageRatingColumn")) {
                            Text(text = "Current Rating", fontWeight = FontWeight.Bold)
                            Text(
                                text = selectedParking.avgScore.toString(),
                                color = Color.Gray)
                        }
                    }

                }

                // Column for action buttons like "Show in Map", "Add A Review", and "Report"
                Column(
                    modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("ButtonsColumn"), // Test tag for buttons column
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { /* Handle Return to Map */},
                        modifier =
                        Modifier.fillMaxWidth()
                            .height(40.dp)
                            .testTag(
                                "ShowInMapButton"), // Test tag for Show in Map button
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.secondary)) {
                        Text(text = "Show in Map")
                    }

                    Button(
                        onClick = { navigationActions.navigateTo(Screen.REVIEW) },
                        modifier =
                        Modifier.fillMaxWidth()
                            .height(40.dp)
                            .testTag("AddReviewButton"), // Test tag for Add Review button
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.secondary)) {
                        Text(text = "Add A Review")
                    }

                    Button(
                        onClick = {},
                        modifier =
                        Modifier.height(30.dp)
                            .testTag("ReportButton"), // Test tag for Report button
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Red,
                            contentColor = MaterialTheme.colorScheme.secondary)) {
                        Text(text = "Report")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
