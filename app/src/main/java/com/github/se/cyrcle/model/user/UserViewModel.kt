package com.github.se.cyrcle.model.user

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class UserViewModel(
    private val userRepository: UserRepository,
    private val parkingRepository: ParkingRepository,
    private val imageRepository: ImageRepository,
    private val authenticator: AuthenticationRepository
) : ViewModel() {

  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser

  var _selectedUserImageUrls = MutableStateFlow<List<String>>(emptyList())
  var selectedUserImageUrls: StateFlow<List<String>> = _selectedUserImageUrls

  var _selectedUserAssociatedImages = MutableStateFlow<List<String>>(emptyList())
  var selectedUserAssociatedImages: StateFlow<List<String>> = _selectedUserAssociatedImages

  val isSignedIn: Flow<Boolean>
    get() = currentUser.map { it != null }

  // === States for Offline mode  ===

  // Keep track of the user's online status, can only be changed with the consent of the user.
  private val _isOnlineMode = MutableStateFlow(true)
  val isOnlineMode: StateFlow<Boolean> = _isOnlineMode

  // Keep track of whether the user has connection to the internet or not.
  private val _hasConnection = MutableStateFlow(true)
  val hasConnection: StateFlow<Boolean> = _hasConnection

  // Combine the connectivity status and the mode selected to decide if we show ui elements that
  // require internet.
  // (i.e the search bars)
  val displayOnlineElementFlow =
      combine(_isOnlineMode, _hasConnection) { isOnlineMode, hasConnection ->
        isOnlineMode && hasConnection
      }
  // === === === === === === ===

  /**
   * Sets the current user.
   *
   * @param user the user to set as the current user
   */
  fun setCurrentUser(user: User?) {
    Log.d("UserViewModel", "Setting the current user in viewmodel : $user")
    _currentUser.value = user
  }

  /**
   * Loads the current user's images and updates the associated state flows. Clears the existing
   * image URLs and paths, then retrieves URLs for the image paths listed in
   * `currentUser.details.userImages`. Updates `_selectedUserImageUrls` with the retrieved URLs and
   * `_selectedUserAssociatedImages` with the image paths. Logs an error if URL retrieval fails.
   */
  fun loadSelectedUserImages() {
    // clear the list of URLs each time we request the images (prevent duplicates and old images
    // from being displayed)
    _selectedUserImageUrls.value = emptyList()
    _selectedUserAssociatedImages.value = emptyList()

    currentUser.value!!.details?.userImages?.forEach { imagePath ->
      _selectedUserAssociatedImages.value = _selectedUserAssociatedImages.value.plus(imagePath)
      imageRepository.getUrl(
          path = imagePath,
          onSuccess = { url ->
            _selectedUserImageUrls.value = _selectedUserImageUrls.value.plus(url)
          },
          onFailure = { Log.e("ParkingViewModel", "Error getting image URL for path") })
    }
  }

  private val _favoriteParkings = MutableStateFlow<List<Parking>>(emptyList())
  val favoriteParkings: StateFlow<List<Parking>> = _favoriteParkings
  /**
   * Set the current user by fetching the user from the Firestore database with the given ID.
   *
   * @param userId the ID of the user to get
   */
  fun setCurrentUserById(userId: String) {
    userRepository.getUserById(
        userId,
        onSuccess = { setCurrentUser(it) },
        onFailure = { exception ->
          Log.e(
              "com.github.se.cyrcle.model.user.UserViewModel",
              "Failed to fetch user by ID: $userId",
              exception)
        })
  }

  /**
   * Gets a user by ID from the Firestore database.. Fetches the profile picture url from the cloud
   * storage and updates the user object with before calling the callback.
   *
   * @param userId the ID of the user to get
   * @param onSuccess the callback to call with the user
   */
  fun getUserById(userId: String, onSuccess: (User) -> Unit, onFailure: () -> Unit = {}) {
    Log.d("UserViewModel", "Getting the current user by ID: $userId")
    userRepository.getUserById(
        userId,
        onSuccess = {
          transformPathToUrl(
              it,
              onSuccess = onSuccess,
              onFailure = { Log.e("UserViewModel", "Failed to transform path to url") })
        },
        onFailure = { exception ->
          Log.e(
              "com.github.se.cyrcle.model.user.UserViewModel",
              "Failed to fetch user by ID: $userId",
              exception)
          onFailure()
        })
  }

  /**
   * Checks if a user exists in the database.
   *
   * @param user the user to check
   * @param onSuccess the callback to call with a boolean indicating if the user exists
   */
  fun doesUserExist(user: User, onSuccess: (Boolean) -> Unit) {
    userRepository.userExists(user, onSuccess = onSuccess, onFailure = {})
  }

  /**
   * Adds a user to the database.
   *
   * @param user the user to add
   * @param onComplete the callback to call on completion
   * @param onFailure the callback to call if the user addition fails
   */
  fun addUser(user: User, onComplete: () -> Unit, onFailure: () -> Unit) {
    userRepository.addUser(user, onComplete) {
      Log.e("UserViewModel", "Failed to add user: $user", it)
      onFailure()
    }
  }

  /**
   * Updates a user in the Firestore database. If a user has Uri for the profile picture, it will
   * upload the image to the cloud storage and then delete the Uri.
   *
   * @param user the user to update
   * @param context the context used to resolve the image uri (if null the profile picture will not
   *   upload)
   * @param onSuccess the callback to call with the updated user
   */
  fun updateUser(user: User, context: Context? = null, onSuccess: () -> Unit = {}) {
    if (user.public.userId != currentUser.value?.public?.userId) {
      Log.e("UserViewModel", "Trying to update a user that is not the current user")
      return
    }

    Log.d("UserViewModel", "Updating user: $user")
    // First update the user object in the view model.
    setCurrentUser(user)
    // Then update the user object in the database
    if (user.localSession?.profilePictureUri.isNullOrBlank() || context == null) {
      userRepository.updateUser(
          user,
          onSuccess = {
            Log.d("UserViewModel", "User updated in the database")
            onSuccess()
          },
          onFailure = { exception ->
            Log.e(
                "com.github.se.cyrcle.model.user.UserViewModel",
                "Failed to update user: ${user.public.userId}",
                exception)
          })
    } else {
      Log.d("UserViewModel", "User has a profile picture to upload")
      // Even if the upload fails, uploadProfilePicture will still call updateUser to back up the
      // updated User.
      uploadProfilePicture(context)
    }
  }

  /**
   * Adds parking to the list of favorite parkings for the selected user and updates the favorite
   * parkings state.
   *
   * @param parking the parking to add to the list of favorite parkings
   */
  fun addFavoriteParkingToSelectedUser(parking: Parking) {
    currentUser.value?.let { user ->
      val updatedDetails =
          user.details?.copy(favoriteParkings = user.details.favoriteParkings + parking.uid)
      val updatedUser = user.copy(details = updatedDetails)
      updateUser(updatedUser) { _favoriteParkings.value += parking }
    }
  }

  /**
   * Adds a parking to the list of reported parkings for the selected user and updates the reported
   * parkings state.
   *
   * @param parking The parking ID to add to the list of reported parkings.
   */
  fun addReportedParkingToSelectedUser(parking: String) {
    currentUser.value?.let { user ->
      val updatedDetails =
          user.details?.copy(reportedParkings = user.details.reportedParkings + parking)
      val updatedUser = user.copy(details = updatedDetails)
      updateUser(updatedUser) {}
      updateUser(updatedUser) {}
    }
  }

  /**
   * Adds a parking to the list of reviewed parkings for the selected user and updates the reviewed
   * parkings state.
   *
   * @param parking The parking ID to add to the list of reviewed parkings.
   */
  fun addReviewedParkingToSelectedUser(parking: String) {
    currentUser.value?.let { user ->
      val updatedDetails =
          user.details?.copy(reviewedParkings = user.details.reviewedParkings + parking)
      val updatedUser = user.copy(details = updatedDetails)
      updateUser(updatedUser) {}
    }
  }

  /**
   * Adds an image to the list of reported images for the selected user and updates the reported
   * images state.
   *
   * @param image The image ID to add to the list of reported images.
   */
  fun addReportedImageToSelectedUser(image: String) {
    currentUser.value?.let { user ->
      val updatedDetails = user.details?.copy(reportedImages = user.details.reportedImages + image)
      val updatedUser = user.copy(details = updatedDetails)
      updateUser(updatedUser) {}
    }
  }

  /**
   * Adds an image to the current user's list of images. Updates the user's `userImages` field and
   * saves the updated user in the database. Also updates the `_reportedParkings` state.
   *
   * @param image The image path to add to the user's list of images.
   */
  fun addImageToUserImages(image: String) {
    currentUser.value?.let { user ->
      val updatedDetails = user.details?.copy(userImages = user.details.userImages + image)
      val updatedUser = user.copy(details = updatedDetails)
      updateUser(updatedUser) {}
    }
  }

  /**
   * Removes an image from the current user's list of images. Updates the user's `userImages` field
   * and saves the updated user in the database. Also updates the `_selectedUserAssociatedImages`
   * state.
   *
   * @param image The image path to remove from the user's list of images.
   */
  fun removeImageFromUserImages(image: String) {
    currentUser.value?.let { user ->
      val updatedDetails = user.details?.copy(userImages = user.details.userImages - image)
      val updatedUser = user.copy(details = updatedDetails)
      updateUser(updatedUser) { _selectedUserAssociatedImages.value -= image }
    }
  }

  /**
   * Removes parking from the list of favorite parkings for the selected user and updates the
   * favorite parkings state.
   *
   * @param parking the parking to remove from the list of favorite parkings
   */
  fun removeFavoriteParkingFromSelectedUser(parking: Parking) {
    currentUser.value?.let { user ->
      val updatedDetails =
          user.details?.copy(
              favoriteParkings = user.details.favoriteParkings.filter { it != parking.uid })
      val updatedUser = user.copy(details = updatedDetails)
      updateUser(updatedUser) {
        _favoriteParkings.value = _favoriteParkings.value.filter { it.uid != parking.uid }
      }
    }
  }

  // =============================== AUTHENTICATION ===============================

  /** Describes the reason for a sign in failure. */
  enum class SignInFailureReason {
    ACCOUNT_NOT_FOUND,
    ERROR
  }

  /**
   * Authenticates a user and provides the user ID to the onSuccess callback. The given User ID is
   * unique to the authentication provider, it has no link to our database
   *
   * @param onSuccess the callback to call with the userID
   * @param onFailure the callback to call if the sign in fails
   */
  fun authenticate(
      onSuccess: (String) -> Unit,
      onFailure: () -> Unit,
  ) {
    authenticator.authenticate(
        // On successful authentication
        {
          Log.d("UserViewModel", "User authenticated successfully, userId: $it")
          onSuccess(it)
        },
        // On failed authentication
        {
          Log.e("UserViewModel", "Failed to sign in user", it)
          onFailure()
        })
  }

  /**
   * Sign in a User: Signs the user into the app and calls on success with it. If the user is not
   * found in the database, it calls onFailure with ACCOUNT_NOT_FOUND. If the sign in fails, it
   * calls onFailure with ERROR.
   *
   * @param onSuccess the callback to call with the user
   * @param onFailure the callback to call if the sign in fails
   */
  fun signIn(onSuccess: (User) -> Unit, onFailure: (SignInFailureReason) -> Unit) {
    authenticator.authenticate(
        // On successful authentication
        { userID ->
          getUserById(
              userID,
              // On successful user retrieval
              {
                Log.d("UserViewModel", "User signed in successfully, user: $it")
                setCurrentUser(it)
                getSelectedUserFavoriteParkings()
                onSuccess(it)
              },
              // User not found in the database
              {
                Log.d("UserViewModel", "User not found in the database")
                onFailure(SignInFailureReason.ACCOUNT_NOT_FOUND)
              })
        },
        // On failed authentication
        {
          Log.e("UserViewModel", "Failed to sign in user", it)
          onFailure(SignInFailureReason.ERROR)
        })
  }

  /**
   * Selects a parking owner based on the given user ID.
   *
   * @param userId The ID of the user to set as the selected parking owner.
   */
  fun selectSelectedParkingUser(userId: String?) {
    if (userId.isNullOrEmpty()) {
      Log.e("UserViewModel", "Invalid userId: $userId")
      return
    }

    userRepository.getUserById(
        userId,
        onSuccess = {},
        onFailure = { exception ->
          Log.e(
              "com.github.se.cyrcle.model.user.UserViewModel",
              "Failed to fetch user by ID: $userId",
              exception)
        })
  }

  /**
   * Sign in a user anonymously.
   *
   * @param onComplete the callback to call on completion
   */
  fun signInAnonymously(onComplete: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    authenticator.authenticateAnonymously(
        onComplete = {
          Log.d("UserViewModel", "User signed in anonymously")
          onComplete()
        },
        onFailure = { onFailure(it) })
  }

  /**
   * Signs the user out of the app
   *
   * @param onComplete the callback to call on completion
   */
  fun signOut(onComplete: () -> Unit) {
    authenticator.signOut {
      setCurrentUser(null)
      _favoriteParkings.value = emptyList()
      onComplete()
    }
  }

  // ============================== HELPER FUNCTIONS ==============================

  /**
   * Gets the favorite parkings of the current user and sets them in the favorite parkings state
   * Should only be called once, when the user is set in the viewmodel.
   */
  private fun getSelectedUserFavoriteParkings() {
    currentUser.value?.details?.let { details ->
      parkingRepository.getParkingsByListOfIds(
          details.favoriteParkings,
          onSuccess = { _favoriteParkings.value = it },
          onFailure = { exception ->
            Log.e("UserViewModel", "Failed to fetch favorite parkings of current user", exception)
          })
    }
  }

  /**
   * Edits the personal note of the current user for the given parking. If the new note is blank,
   * the personal note is removed (if it exists). If it doesn't exist, the function does nothing. If
   * the new note is not empty, the personal note is added or updated.
   *
   * @param parking the parking to edit the personal note for
   * @param newNote the new personal note for the parking
   */
  fun editCurrentUserPersonalNoteForParking(parking: Parking, newNote: String) {
    currentUser.value?.let { user ->
      user.details?.let { details ->
        val key = parking.uid
        val updatedNotes = details.personalNotes.toMutableMap()
        if (newNote.isBlank()) updatedNotes.remove(key) else updatedNotes[key] = newNote
        updateUser(user.copy(details = details.copy(personalNotes = updatedNotes)))
      }
    }
  }

  /**
   * Upload the picture to the cloud storage update the user object in the database with the cloud
   * path created. update the user object in the viewmodel with the url of the image and the cloud
   * path
   *
   * @param context the context used to resolve the image uri
   */
  private fun uploadProfilePicture(context: Context) {
    if (currentUser.value == null) {
      Log.e("UserViewModel", "Current user is null, should not upload image")
      return
    }
    if (currentUser.value?.localSession?.profilePictureUri == null) {
      Log.e("UserViewModel", "Profile picture uri is null, should not upload image")
      return
    }

    val user = currentUser.value!!
    val fileUri = user.localSession!!.profilePictureUri!!
    val destinationPath = "profile_pictures/${user.public.userId}"
    imageRepository.uploadImage(
        context = context,
        fileUri = fileUri,
        destinationPath = destinationPath,
        onSuccess = { url ->
          Log.d("UserViewModel", "Image uploaded successfully to $url")
          val updatedUser =
              user.copy(
                  public = user.public.copy(profilePictureCloudPath = destinationPath),
                  localSession =
                      user.localSession.copy(profilePictureUri = null, profilePictureUrl = url))
          updateUser(updatedUser)
        },
        onFailure = {
          val updatedUser =
              user.copy(localSession = user.localSession.copy(profilePictureUri = null))
          updateUser(updatedUser)
          Log.e("UserViewModel", "Failed to upload image, still updating user without the picture")
        })
  }

  /**
   * Updates the user object with the profile picture url fetched from the repo.
   *
   * @param user the user object to update (doesn't have to be the current user)
   * @param onSuccess the callback to call with the updated user
   * @param onFailure the callback to call if the image fetching fails
   */
  private fun transformPathToUrl(user: User, onSuccess: (User) -> Unit, onFailure: () -> Unit) {
    // For user with the old version of the app (i.e the M2)
    // the new attribute profilePictureCloudPath is set to null by the deserializer.
    // We skip the fetching of the image for those users.
    if (user.public.profilePictureCloudPath == null) {
      onSuccess(user)
      return
    }

    val cloudPath = user.public.profilePictureCloudPath
    if (cloudPath.isBlank()) {
      Log.d("UserViewModel", "No image to fetch for user")
      onSuccess(user) // No image to fetch
      return
    }

    Log.d("UserViewModel", "Getting the url for the image at $cloudPath")
    imageRepository.getUrl(
        cloudPath,
        onSuccess = { url ->
          Log.d("UserViewModel", "Got the url for the image: $url")
          val updatedUser =
              user.copy(
                  localSession =
                      user.localSession?.copy(profilePictureUrl = url)
                          ?: LocalSession(profilePictureUrl = url))
          onSuccess(updatedUser)
        },
        onFailure = onFailure)
  }

  // ============================== WALLET FUNCTIONS ==============================

  /**
   * Credits coins to the current user's wallet and updates the user in the database.
   *
   * @param amount the amount of coins to credit
   * @param onSuccess callback called when the operation is successful
   */
  fun creditCoinsToCurrentUser(amount: Coin, onSuccess: () -> Unit = {}) {
    currentUser.value?.let { user ->
      try {
        user.details?.wallet?.creditCoins(amount)
        updateUser(
            user,
            onSuccess = {
              Log.d("UserViewModel", "Successfully credited $amount coins to user")
              onSuccess()
            })
      } catch (e: IllegalArgumentException) {
        Log.e("UserViewModel", "Failed to credit coins: ${e.message}")
      }
    } ?: Log.e("UserViewModel", "Attempted to credit coins but no current user")
  }

  /**
   * Checks if the current user can afford a transaction and debits the coins if possible.
   *
   * @param amount the amount of coins needed for the transaction
   * @param creditThreshold the minimum amount of coins that should remain after the transaction
   * @param onSuccess callback called when the transaction is successful
   */
  fun tryDebitCoinsFromCurrentUser(
      amount: Coin,
      creditThreshold: Coin = 0,
      onSuccess: () -> Unit = {}
  ) {
    currentUser.value?.let { user ->
      try {
        if (user.details?.wallet?.isSolvable(amount, creditThreshold) == true) {
          user.details.wallet.debitCoins(amount)
          updateUser(
              user,
              onSuccess = {
                Log.d("UserViewModel", "Successfully debited $amount coins from user")
                onSuccess()
              })
        } else {
          Log.d("UserViewModel", "User cannot afford transaction of $amount coins")
        }
      } catch (e: IllegalArgumentException) {
        Log.e("UserViewModel", "Failed to debit coins: ${e.message}")
      }
    } ?: Log.e("UserViewModel", "Attempted to debit coins but no current user")
  }
  // ============================== ONLINE STATUS ==============================
  /**
   * Sets the online status of the user.
   *
   * @param isOnline the new online status of the user
   */
  fun setIsOnlineMode(isOnline: Boolean) {
    _isOnlineMode.value = isOnline
  }
  /**
   * Set the has connection status of the user.
   *
   * @param hasConnection the new connection status of the user
   */
  fun setHasConnection(hasConnection: Boolean) {
    _hasConnection.value = hasConnection
  }
}
