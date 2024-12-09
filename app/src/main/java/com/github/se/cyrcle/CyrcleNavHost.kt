import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.addParking.attributes.AttributesPicker
import com.github.se.cyrcle.ui.addParking.location.LocationPicker
import com.github.se.cyrcle.ui.authentication.SignInScreen
import com.github.se.cyrcle.ui.list.SpotListScreen
import com.github.se.cyrcle.ui.map.MapScreen
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.parkingDetails.ParkingDetailsScreen
import com.github.se.cyrcle.ui.profile.ProfileScreen
import com.github.se.cyrcle.ui.review.AllReviewsScreen
import com.github.se.cyrcle.ui.review.ReviewScreen

@Composable
fun CyrcleNavHost(
    navigationActions: NavigationActions,
    navController: NavHostController,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel,
    userViewModel: UserViewModel,
    mapViewModel: MapViewModel,
    addressViewModel: AddressViewModel,
    permissionGranted: Boolean
) {
  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions, userViewModel) }
    }

    navigation(
        startDestination = Screen.LIST,
        route = Route.LIST,
    ) {
      composable(Screen.LIST) { SpotListScreen(navigationActions, parkingViewModel) }
      composable(Screen.CARD) {
        ParkingDetailsScreen(navigationActions, parkingViewModel, userViewModel)
      }
      composable(Screen.REVIEW) {
        ReviewScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      }
      composable(Screen.ALL_REVIEWS) {
        AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel)
      }
    }

    navigation(
        startDestination = Screen.MAP,
        route = Route.MAP,
    ) {
      composable(Screen.MAP) {
        MapScreen(
            navigationActions,
            parkingViewModel,
            userViewModel,
            mapViewModel,
            permissionGranted = permissionGranted)
      }
    }

    navigation(startDestination = Screen.LOCATION_PICKER, route = Route.ADD_SPOTS) {
      composable(Screen.LOCATION_PICKER) { LocationPicker(navigationActions, mapViewModel) }
      composable(Screen.ATTRIBUTES_PICKER) {
        AttributesPicker(navigationActions, parkingViewModel, mapViewModel, addressViewModel)
      }
    }

    // Add this new navigation block for Profile
    navigation(
        startDestination = Screen.PROFILE,
        route = Route.PROFILE,
    ) {
      composable(Screen.PROFILE) { ProfileScreen(navigationActions, userViewModel) }
    }
  }
}
