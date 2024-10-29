import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.ui.addParking.attributes.AttributesPicker
import com.github.se.cyrcle.ui.addParking.location.LocationPicker
import com.github.se.cyrcle.ui.authentication.SignInScreen
import com.github.se.cyrcle.ui.card.CardScreen
import com.github.se.cyrcle.ui.list.SpotListScreen
import com.github.se.cyrcle.ui.map.MapScreen
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.review.ReviewScreen

@Composable
fun CyrcleNavHost(
    navigationActions: NavigationActions,
    navController: NavHostController,
    parkingViewModel: ParkingViewModel,
    reviewViewModel: ReviewViewModel,
    mapViewModel: MapViewModel
) {
  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions) }
    }

    navigation(
        startDestination = Screen.LIST,
        route = Route.LIST,
    ) {
      composable(Screen.LIST) { SpotListScreen(navigationActions, parkingViewModel) }
      composable(Screen.CARD) { CardScreen(navigationActions, parkingViewModel) }
      composable(Screen.REVIEW) {
        ReviewScreen(navigationActions, parkingViewModel, reviewViewModel)
      }
    }

    navigation(
        startDestination = Screen.MAP,
        route = Route.MAP,
    ) {
      composable(Screen.MAP) { MapScreen(navigationActions, parkingViewModel, mapViewModel) }
    }
    navigation(startDestination = Screen.LOCATION_PICKER, route = Route.ADD_SPOTS) {
      composable(Screen.LOCATION_PICKER) { LocationPicker(navigationActions, mapViewModel) }
      composable(Screen.ATTRIBUTES_PICKER) {
        AttributesPicker(navigationActions, parkingViewModel, mapViewModel)
      }
    }
  }
}
