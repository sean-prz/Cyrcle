package com.github.se.cyrcle.ui.addParking.attributes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingAttribute
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.user.PARKING_CREATION_REWARD
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.map.MapConfig
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.github.se.cyrcle.ui.theme.Typography
import com.github.se.cyrcle.ui.theme.atoms.ConditionCheckingInputText
import com.github.se.cyrcle.ui.theme.disabledColor
import com.github.se.cyrcle.ui.theme.molecules.BooleanRadioButton
import com.github.se.cyrcle.ui.theme.molecules.EnumDropDown
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings

const val TITLE_MIN_LENGTH = 8
const val TITLE_MAX_LENGTH = 48
const val DESCRIPTION_MIN_LENGTH = 0
const val DESCRIPTION_MAX_LENGTH = 128

@Composable
fun AttributesPicker(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    mapViewModel: MapViewModel,
    addressViewModel: AddressViewModel,
    userViewModel: UserViewModel
) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  // Define padding as a percentage of screen dimensions
  val horizontalPaddingScaleFactor = screenWidth * 0.03f // 3% of screen width
  val verticalPaddingScaleFactor = screenHeight * 0.02f // 2% of screen height

  // Dynamically calculated heights for top and bottom boxes
  val topBoxHeight = screenHeight * 0.10f // 10% of screen height for top box
  val bottomBoxHeight = screenHeight * 0.15f // 15% of screen height for bottom box

  val title = rememberSaveable { mutableStateOf("") }
  val description = rememberSaveable { mutableStateOf("") }
  val rackType = rememberSaveable { mutableStateOf<ParkingAttribute>(ParkingRackType.TWO_TIER) }
  val capacity = rememberSaveable { mutableStateOf<ParkingAttribute>(ParkingCapacity.XSMALL) }
  val protection = rememberSaveable { mutableStateOf<ParkingAttribute>(ParkingProtection.NONE) }
  val hasSecurity = rememberSaveable { mutableStateOf(false) }

  val location = mapViewModel.selectedLocation.collectAsState().value!!
  LaunchedEffect(location) { addressViewModel.search(location.center) }
  val suggestedAddress = addressViewModel.address.collectAsState().value
  LaunchedEffect(suggestedAddress) {
    if (title.value.isEmpty()) title.value = suggestedAddress.displayRelevantFields()
  }
  val context = LocalContext.current
  val parkingAddedWithRewardText =
      stringResource(
          R.string.attributes_picker_parking_added_with_reward_toast, PARKING_CREATION_REWARD)

  fun onSubmit() {
    val parking =
        Parking(
            optName = title.value,
            optDescription = description.value,
            rackType = rackType.value as ParkingRackType,
            capacity = capacity.value as ParkingCapacity,
            protection = protection.value as ParkingProtection,
            hasSecurity = hasSecurity.value,
            location = location,
            images = listOf(),
            imageObjects = emptyList(),
            reportedImages = emptyList(),
            price = 0.0,
            uid = parkingViewModel.getNewUid(),
            owner = userViewModel.currentUser.value?.public?.userId!!,
            reportingUsers = emptyList())
    parkingViewModel.addParking(parking)

    userViewModel.creditCoinsToCurrentUser(PARKING_CREATION_REWARD)
    Toast.makeText(context, parkingAddedWithRewardText, Toast.LENGTH_LONG).show()

    navigationActions.navigateTo(TopLevelDestinations.MAP)
  }

  Scaffold(
      modifier = Modifier.testTag("AttributesPickerScreen"),
      topBar = { AttributePickerTopBar(mapViewModel, title) },
      bottomBar = {
        val validInputs = areInputsValid(title.value, description.value)
        BottomBarAddAttr(navigationActions, validInputs) { onSubmit() }
      }) { padding ->
        // Apply screen-dimension-scaled padding for consistent spacing
        val scaledPaddingValues =
            scaledPadding(
                padding = padding,
                horizontalScaleFactor = horizontalPaddingScaleFactor.value / screenWidth.value,
                verticalScaleFactor = verticalPaddingScaleFactor.value / screenHeight.value)

        Column(
            modifier =
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(scaledPaddingValues)
                    .testTag("AttributesPickerColumn")) {
              // Top Box as padding at the top of the screen, dynamically sized
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(topBoxHeight) // Dynamic height for top box
                          .background(MaterialTheme.colorScheme.background))

              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(
                              horizontal = horizontalPaddingScaleFactor,
                              vertical = verticalPaddingScaleFactor),
                  verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConditionCheckingInputText(
                        value = title.value,
                        onValueChange = { title.value = it },
                        label = stringResource(R.string.attributes_picker_title_label),
                        minCharacters = TITLE_MIN_LENGTH,
                        maxCharacters = TITLE_MAX_LENGTH,
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = horizontalPaddingScaleFactor),
                        testTag = "AttributesPickerTitle")
                    EnumDropDown(
                        options = ParkingProtection.entries.toList(),
                        selectedValue = protection,
                        label = stringResource(R.string.attributes_picker_protection_label))
                    EnumDropDown(
                        options = ParkingCapacity.entries.toList(),
                        selectedValue = capacity,
                        label = stringResource(R.string.attributes_picker_capacity_label),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()) {
                          EnumDropDown(
                              options = ParkingRackType.entries.toList(),
                              selectedValue = rackType,
                              label = stringResource(R.string.attributes_picker_rack_type_label),
                              modifier = Modifier.weight(1f).fillMaxWidth())
                          Icon(
                              imageVector = Icons.Default.Info,
                              contentDescription = "Info",
                              tint = MaterialTheme.colorScheme.primary,
                              modifier =
                                  Modifier.padding(start = 4.dp, end = 10.dp)
                                      .testTag("RackInfoIcon")
                                      .clickable { navigationActions.navigateTo(Screen.RACK_INFO) })
                        }

                    BooleanRadioButton(
                        question =
                            stringResource(R.string.attributes_picker_has_surveillance_question),
                        hasSecurity)
                    ConditionCheckingInputText(
                        value = description.value,
                        onValueChange = { description.value = it },
                        label = stringResource(R.string.attributes_picker_description_label),
                        minCharacters = DESCRIPTION_MIN_LENGTH,
                        maxCharacters = DESCRIPTION_MAX_LENGTH,
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(screenHeight * 0.2f) // Dynamic height for description field
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = horizontalPaddingScaleFactor),
                        testTag = "AttributesPickerDescription")
                  }

              // Bottom Box as padding at the bottom of the screen, dynamically sized
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(bottomBoxHeight) // Dynamic height for bottom box
                          .background(MaterialTheme.colorScheme.background))
            }
      }
}

private fun scaledPadding(
    padding: PaddingValues,
    horizontalScaleFactor: Float,
    verticalScaleFactor: Float
): PaddingValues {
  return PaddingValues(
      start = padding.calculateStartPadding(LayoutDirection.Ltr) * horizontalScaleFactor,
      top = padding.calculateTopPadding() * verticalScaleFactor,
      end = padding.calculateEndPadding(LayoutDirection.Ltr) * horizontalScaleFactor,
      bottom = padding.calculateBottomPadding() * verticalScaleFactor)
}

@Composable
fun BottomBarAddAttr(
    navigationActions: NavigationActions,
    validInputs: Boolean,
    onSubmit: () -> Unit
) {
  Box(
      Modifier.background(Color.White)
          .height(100.dp) // Matching the height of LocationPickerBottomBar
          .testTag("AttributesPickerBottomBar")) {
        Row(
            Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp).background(Color.White),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Button(
                  onClick = { navigationActions.navigateTo(TopLevelDestinations.MAP) },
                  modifier = Modifier.testTag("cancelButton"),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text(
                        text = stringResource(R.string.attributes_picker_bottom_bar_cancel_button),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = Typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(100.dp))
                  }

              VerticalDivider(
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.height(32.dp).width(1.dp),
                  thickness = 2.dp)

              val toast =
                  Toast.makeText(
                      LocalContext.current,
                      stringResource(R.string.attributes_picker_bottom_bar_submit_button_toast),
                      Toast.LENGTH_SHORT)

              Button(
                  onClick = { if (validInputs) onSubmit() else toast.show() },
                  modifier = Modifier.testTag("submitButton"),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text(
                        text = stringResource(R.string.attributes_picker_bottom_bar_submit_button),
                        color =
                            if (validInputs) MaterialTheme.colorScheme.primary else disabledColor(),
                        fontWeight = FontWeight.Bold,
                        style = Typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(100.dp))
                  }
            }
      }
}

@Composable
fun AttributePickerTopBar(mapViewModel: MapViewModel, title: MutableState<String>) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Reduced the top bar height for tighter bounds
  val topBarHeight = screenWidth * 0.20f // Reduced height to 18% of screen width
  val titleFontSize = (screenWidth * 0.045f).value.sp // Slightly smaller font size
  val subtitleFontSize = (screenWidth * 0.035f).value.sp

  var annotationManager by remember { mutableStateOf<PolygonAnnotationManager?>(null) }
  val location = mapViewModel.selectedLocation.collectAsState().value!!
  val mapViewportState = rememberMapViewportState {
    setCameraOptions {
      center(Point.fromLngLat(location.center.longitude() - 0.0015, location.center.latitude()))
      zoom(16.0)
      bearing(0.0)
      pitch(0.0)
    }
  }

  Box(
      modifier =
          Modifier.fillMaxWidth()
              .height(topBarHeight)
              .clipToBounds()
              .testTag("AttributesPickerTopBar")) {
        val mapState = rememberMapState()
        mapState.gesturesSettings = GesturesSettings {
          scrollEnabled = false
          quickZoomEnabled = false
          pinchToZoomEnabled = false
          doubleTapToZoomInEnabled = false
          doubleTouchToZoomOutEnabled = false
          rotateEnabled = false
          pitchEnabled = false
        }

        MapboxMap(
            mapViewportState = mapViewportState,
            style = { MapConfig.DefaultStyle() },
            modifier = Modifier.fillMaxWidth().height(topBarHeight),
            mapState = mapState,
            attribution = {},
            scaleBar = {}) {
              DisposableMapEffect { mapView ->
                annotationManager = mapView.annotations.createPolygonAnnotationManager()
                onDispose { annotationManager!!.deleteAll() }
              }
            }

        Box(
            modifier =
                Modifier.matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors =
                                listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.3f)))))
        Column(modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
          Text(
              text = stringResource(R.string.attributes_picker_top_bar_new_parking_spot),
              fontSize = titleFontSize,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              maxLines = 1)
          Text(
              text = title.value,
              fontSize = subtitleFontSize,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              maxLines = 1)
          Text(
              text = stringResource(R.string.attributes_picker_top_bar_set_attributes),
              fontSize = subtitleFontSize,
              color = MaterialTheme.colorScheme.tertiary,
              fontWeight = FontWeight.Bold,
              maxLines = 1)
        }
        // To draw a rectangles to preview the location of the future parking spot,
        // we need to wrap the location in a Parking object to pass it to the drawRectangles
        // function
        val placeholderParking = TestInstancesParking.parking1.copy(location = location)
        mapViewModel.drawRectangles(annotationManager, null, listOf(placeholderParking))
      }
}

private fun areInputsValid(title: String, description: String): Boolean {
  return title.length in TITLE_MIN_LENGTH..TITLE_MAX_LENGTH &&
      description.length in DESCRIPTION_MIN_LENGTH..DESCRIPTION_MAX_LENGTH
}
