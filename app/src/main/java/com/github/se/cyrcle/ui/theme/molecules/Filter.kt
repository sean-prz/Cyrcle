package com.github.se.cyrcle.ui.theme.molecules

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.address.Address
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.permission.PermissionHandler
import com.github.se.cyrcle.ui.list.NumberOfSuggestionsForMenu
import com.github.se.cyrcle.ui.list.maxSuggestionDisplayNameLengthList
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.atoms.ToggleButton
import com.github.se.cyrcle.ui.theme.defaultOnColor
import com.github.se.cyrcle.ui.theme.getCheckBoxColors
import com.github.se.cyrcle.ui.theme.getOutlinedTextFieldColorsSearchBar
import kotlinx.coroutines.runBlocking

/**
 * FilterPanel is a composable that displays the filter options for the parking list screen. It
 * contains the following filter options:
 * - Protection
 * - Rack type
 * - Capacity
 * - CCTV
 *
 * @param parkingViewModel The view model that contains the filter options
 * @param displayHeader A boolean that determines if the filter header should be displayed. If true,
 *   a floating action button will be displayed to show/hide the filter options. If false, the
 *   filter the filter options and sections will be expanded and not collapsible.
 * @param addressViewModel The view model that contains the address search functionality
 * @param myLocation A state that indicates whether the user has selected "My Location"
 * @param chosenLocation A state that holds the address chosen by the user
 * @param permissionHandler The handler for managing permissions
 */
@Composable
fun FilterPanel(
    parkingViewModel: ParkingViewModel,
    displayHeader: Boolean,
    addressViewModel: AddressViewModel,
    myLocation: MutableState<Boolean> = mutableStateOf(false),
    chosenLocation: MutableState<Address> =
        mutableStateOf(Address(latitude = "46.518467", longitude = "6.566397")),
    permissionHandler: PermissionHandler
) {
  val showProtectionOptions = remember { mutableStateOf(false) }
  val showRackTypeOptions = remember { mutableStateOf(false) }
  val showCapacityOptions = remember { mutableStateOf(false) }
  var showFilters by remember { mutableStateOf(false) }

  val selectedProtection by parkingViewModel.selectedProtection.collectAsState()
  val selectedRackTypes by parkingViewModel.selectedRackTypes.collectAsState()
  val selectedCapacities by parkingViewModel.selectedCapacities.collectAsState()
  val onlyWithCCTV by parkingViewModel.onlyWithCCTV.collectAsState()

  // Text field visibility
  val isTextFieldVisible = remember { mutableStateOf(false) }

  // Text field value
  val textFieldValue = remember { mutableStateOf("") }

  val radius = parkingViewModel.radius.collectAsState()

  Column(modifier = Modifier.padding(if (displayHeader) 16.dp else 0.dp)) {
    if (displayHeader) {

      // Header. Contains the title and the show/hide filters floating action button
      Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically) {
            // Button transforms into a close button
            SmallFloatingActionButton(
                icon = if (isTextFieldVisible.value) Icons.Default.Close else Icons.Default.Search,
                onClick = { isTextFieldVisible.value = !isTextFieldVisible.value },
                modifier = Modifier.testTag("ShowSearchButton"),
                contentDescription = "Search List Screen",
            )

            // Text field slides out to the right of the button
            if (isTextFieldVisible.value) {
              SearchBarListScreen(
                  addressViewModel,
                  isTextFieldVisible,
                  myLocation,
                  chosenLocation,
                  permissionHandler,
                  textFieldValue)
            } else {
              Text(
                  text = stringResource(R.string.all_parkings_radius, radius.value.toInt()),
                  modifier = Modifier.weight(1f).padding(end = 8.dp),
                  style = MaterialTheme.typography.headlineMedium,
                  color = MaterialTheme.colorScheme.onSurface)
            }
            SmallFloatingActionButton(
                onClick = { showFilters = !showFilters },
                icon = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
                contentDescription = "Filter",
                testTag = "ShowFiltersButton")
          }
    }

    // Filter options. Contains the protection, rack type, capacity, and CCTV filters.
    // We display the filters if the showFilters is true or if the displayHeader is false. This is
    // because if displayHeader is false, we want to display the filters expanded and not
    // collapsible.
    if (showFilters || !displayHeader) {
      Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween) {
            ClickableText(
                text = AnnotatedString(stringResource(R.string.list_screen_clear_all)),
                onClick = { parkingViewModel.clearAllFilterOptions() },
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(horizontal = 8.dp).testTag("ClearAllFiltersButton"))
            ClickableText(
                text = AnnotatedString(stringResource(R.string.list_screen_apply_all)),
                onClick = { parkingViewModel.selectAllFilterOptions() },
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(horizontal = 8.dp).testTag("ApplyAllFiltersButton"))
          }

      // Protection filter
      FilterSection(
          title = stringResource(R.string.list_screen_protection),
          expandedState = showProtectionOptions,
          collapsible = displayHeader,
          onReset = { parkingViewModel.clearProtection() },
          onApply = { parkingViewModel.selectAllProtection() }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("ProtectionFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingProtection.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { parkingViewModel.toggleProtection(option) },
                        value = selectedProtection.contains(option),
                        testTag = "ProtectionFilterItem")
                  }
                }
          }

      // Rack type filter
      FilterSection(
          title = stringResource(R.string.list_screen_rack_type),
          expandedState = showRackTypeOptions,
          collapsible = displayHeader,
          onReset = { parkingViewModel.clearRackType() },
          onApply = { parkingViewModel.selectAllRackTypes() }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("RackTypeFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingRackType.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { parkingViewModel.toggleRackType(option) },
                        value = selectedRackTypes.contains(option),
                        testTag = "RackTypeFilterItem")
                  }
                }
          }

      // Capacity filter
      FilterSection(
          title = stringResource(R.string.list_screen_capacity),
          expandedState = showCapacityOptions,
          collapsible = displayHeader,
          onReset = { parkingViewModel.clearCapacity() },
          onApply = { parkingViewModel.selectAllCapacities() }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("CapacityFilter"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(ParkingCapacity.entries.toTypedArray()) { option ->
                    ToggleButton(
                        text = option.description,
                        onClick = { parkingViewModel.toggleCapacity(option) },
                        value = selectedCapacities.contains(option),
                        testTag = "CapacityFilterItem")
                  }
                }
          }

      // CCTV filter with checkbox
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Checkbox(
                checked = onlyWithCCTV,
                onCheckedChange = { parkingViewModel.setOnlyWithCCTV(it) },
                modifier = Modifier.testTag("CCTVCheckbox"),
                colors = getCheckBoxColors(ColorLevel.PRIMARY))
            Text(
                text = stringResource(R.string.list_screen_display_only_cctv),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier.testTag("CCTVCheckboxLabel"))
          }
    }
  }
}

/**
 * FilterSection is a composable that displays a filter section with a title, clear and apply
 * buttons, and the filter options. The filter options are displayed in a column and can be expanded
 * or collapsed by clicking on the title if the collapsible parameter is true.
 *
 * @param title The title of the filter section
 * @param expandedState A mutable state that determines if the filter options are expanded or
 *   collapsed
 * @param collapsible A boolean that determines if the filter options can be expanded or collapsed
 * @param onReset A lambda that is called when the clear button is clicked
 * @param onApply A lambda that is called when the apply button is clicked
 * @param content A lambda that contains the filter options
 */
@Composable
fun FilterSection(
    title: String,
    expandedState: MutableState<Boolean>,
    collapsible: Boolean,
    onReset: () -> Unit,
    onApply: () -> Unit,
    content: @Composable () -> Unit
) {
  Column(
      modifier =
          Modifier.padding(vertical = 8.dp)
              .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
              .background(
                  MaterialTheme.colorScheme.surfaceBright, shape = MaterialTheme.shapes.medium)
              .padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              ClickableText(
                  text = AnnotatedString(stringResource(R.string.list_screen_clear)),
                  onClick = { onReset() },
                  style =
                      MaterialTheme.typography.labelSmall.copy(
                          color = MaterialTheme.colorScheme.primary),
                  modifier = Modifier.padding(horizontal = 4.dp).testTag("Clear${title}Button"))
              Text(
                  text = title,
                  style = MaterialTheme.typography.titleMedium,
                  modifier =
                      Modifier.clickable(
                              onClick = {
                                if (collapsible) expandedState.value = !expandedState.value
                              })
                          .padding(6.dp),
                  testTag = title)
              ClickableText(
                  text = AnnotatedString(stringResource(R.string.list_screen_apply)),
                  onClick = { onApply() },
                  style =
                      MaterialTheme.typography.labelSmall.copy(
                          color = MaterialTheme.colorScheme.primary),
                  modifier = Modifier.padding(horizontal = 4.dp).testTag("Apply${title}Button"))
            }

        if (expandedState.value || !collapsible) {
          Box(modifier = Modifier.padding(top = 8.dp)) { content() }
        }
      }
}

/**
 * Composable function that displays a search bar for the list screen.
 *
 * @param addressViewModel The ViewModel responsible for managing addresses.
 * @param isTextFieldVisible A state that controls the visibility of the text field.
 * @param myLocation A state that indicates whether the user has selected "My Location".
 * @param chosenLocation A state that holds the address chosen by the user.
 * @param permissionHandler The handler for managing permissions.
 * @param textFieldValue The current value of the text field.
 */
@Composable
fun SearchBarListScreen(
    addressViewModel: AddressViewModel,
    isTextFieldVisible: MutableState<Boolean>,
    myLocation: MutableState<Boolean>,
    chosenLocation: MutableState<Address>,
    permissionHandler: PermissionHandler,
    textFieldValue: MutableState<String>
) {

  val context: Context = LocalContext.current

  // Initialize FocusManager
  val focusManager = LocalFocusManager.current

  // Initialize the keyboard controller
  val virtualKeyboardManager = LocalSoftwareKeyboardController.current

  // List of suggestions from NominatimAPI
  val listOfSuggestions = addressViewModel.addressList.collectAsState()

  val uniqueSuggestions = remember { mutableStateOf(listOf<Address>()) }

  // Show suggestions screen
  val showSuggestions = remember { mutableStateOf(false) }

  // value that contain the size fo the textfield to align the suggestion menu below
  val textFieldSize = remember { mutableStateOf(IntSize.Zero) }

  val alpha by animateFloatAsState(targetValue = if (isTextFieldVisible.value) 1f else 0f)

  // Animation values
  val slideOffset by
      animateDpAsState(targetValue = if (isTextFieldVisible.value) 0.dp else (-200).dp)

  Box {
    OutlinedTextField(
        value = textFieldValue.value,
        onValueChange = { textFieldValue.value = it },
        placeholder = { Text(text = stringResource(R.string.search_bar_placeholder)) },
        modifier =
            Modifier.padding(start = 4.dp, end = 8.dp)
                .offset(x = slideOffset)
                .alpha(alpha)
                .onGloballyPositioned { coordinates -> textFieldSize.value = coordinates.size }
                .testTag("SearchBarListScreen"),
        colors = getOutlinedTextFieldColorsSearchBar(ColorLevel.PRIMARY),
        trailingIcon = {
          if (textFieldValue.value.isNotEmpty()) {
            Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = "Clear search",
                tint = defaultOnColor(),
                modifier =
                    Modifier.clickable {
                          textFieldValue.value = ""
                          showSuggestions.value = false
                        }
                        .testTag("ClearSearchButtonListScreen"))
          }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions =
            KeyboardActions(
                onSearch = {
                  virtualKeyboardManager?.hide()

                  runBlocking { addressViewModel.search(textFieldValue.value) }

                  showSuggestions.value = true
                }))

    if (listOfSuggestions.value.size + 1 > 0) {
      DropdownMenu(
          expanded = showSuggestions.value,
          onDismissRequest = { showSuggestions.value = false },
          modifier =
              Modifier.width(with(LocalDensity.current) { textFieldSize.value.width.toDp() })
                  .testTag("SuggestionsMenuListScreen")) {
            if (permissionHandler.getLocalisationPerm().collectAsState().value) {
              DropdownMenuItem(
                  text = {
                    Text(stringResource(R.string.my_location), textAlign = TextAlign.Start)
                  },
                  modifier = Modifier.testTag("MyLocationSuggestionMenuItem"),
                  contentPadding = PaddingValues(8.dp),
                  onClick = {
                    myLocation.value = true
                    showSuggestions.value = false
                    textFieldValue.value = context.resources.getString(R.string.my_location)
                    isTextFieldVisible.value = false
                    focusManager.clearFocus()
                  })
            }

            val seenNames = mutableSetOf<String>()
            uniqueSuggestions.value =
                listOfSuggestions.value.filter { suggestion ->
                  val displayName =
                      suggestion.suggestionFormatDisplayName(
                          maxSuggestionDisplayNameLengthList, Address.Mode.LIST)
                  if (displayName in seenNames) {
                    false
                  } else {
                    seenNames.add(displayName)
                    true
                  }
                }

            for (address in uniqueSuggestions.value.take(NumberOfSuggestionsForMenu)) {
              DropdownMenuItem(
                  text = {
                    Text(
                        address.suggestionFormatDisplayName(
                            maxSuggestionDisplayNameLengthList, Address.Mode.LIST),
                        textAlign = TextAlign.Start)
                  },
                  modifier = Modifier.testTag("SuggestionMenuItem${address.city}"),
                  contentPadding = PaddingValues(8.dp),
                  onClick = {
                    chosenLocation.value = address
                    showSuggestions.value = false
                    myLocation.value = false
                    isTextFieldVisible.value = false
                    textFieldValue.value =
                        address.suggestionFormatDisplayName(
                            maxSuggestionDisplayNameLengthList, Address.Mode.LIST)
                    focusManager.clearFocus()
                  })
            }
          }
    }
  }
}