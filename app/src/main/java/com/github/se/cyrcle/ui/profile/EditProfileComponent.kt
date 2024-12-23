package com.github.se.cyrcle.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.user.LocalSession
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.ui.theme.atoms.ConditionCheckingInputText
import com.github.se.cyrcle.ui.theme.atoms.Text

const val FIRST_NAME_MIN_LENGTH = 0
const val FIRST_NAME_MAX_LENGTH = 32
const val LAST_NAME_MIN_LENGTH = 0
const val LAST_NAME_MAX_LENGTH = 32
const val USERNAME_MIN_LENGTH = 4
const val USERNAME_MAX_LENGTH = 32

/**
 * Edit the profile content of the user. This composable is a component meant to be used within
 * other screens
 *
 * @param user The user to edit the profile content for.
 * @param saveButton The save button to display.
 * @param cancelButton The cancel button to display.
 */
@Composable
fun EditProfileComponent(
    user: User?,
    saveButton: @Composable (User, Boolean) -> Unit,
    cancelButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    verticalButtonDisplay: Boolean = false
) {

  fun areInputsValid(firstName: String, lastName: String, username: String): Boolean {
    return firstName.length in FIRST_NAME_MIN_LENGTH..FIRST_NAME_MAX_LENGTH &&
        lastName.length in LAST_NAME_MIN_LENGTH..LAST_NAME_MAX_LENGTH &&
        username.length in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH
  }

  if (user == null) {
    Text(stringResource(R.string.profile_is_null), Modifier.testTag("NullUserText"))
    return
  }

  var username by remember { mutableStateOf(user.public.username) }
  var firstName by remember { mutableStateOf(user.details!!.firstName) }
  var lastName by remember { mutableStateOf(user.details!!.lastName) }
  var profilePictureUri by remember { mutableStateOf(user.localSession?.profilePictureUri) }
  val profilePictureUrl by remember { mutableStateOf(user.localSession?.profilePictureUrl) }
  // To display the local profile picture when the user changes it, and hasn't uploaded (saved) it
  var hasTemporarilyChangedProfilePicture by remember { mutableStateOf(false) }

  fun copyUser(user: User): User {
    return user.copy(
        public = user.public.copy(username = username),
        details = user.details?.copy(firstName = firstName, lastName = lastName),
        localSession =
            user.localSession?.copy(profilePictureUri = profilePictureUri)
                ?: LocalSession(profilePictureUri = profilePictureUri))
  }

  val imagePickerLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { profilePictureUri = it.toString() }
        hasTemporarilyChangedProfilePicture = true
      }

  Column(
      modifier = modifier.fillMaxSize().padding(top = 15.dp).testTag("ProfileContent"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileImageComponent(
            url = if (hasTemporarilyChangedProfilePicture) profilePictureUri else profilePictureUrl,
            onClick = { imagePickerLauncher.launch("image/*") },
            isEditable = true,
            modifier = Modifier.testTag("ProfileImage"))

        Spacer(modifier = Modifier.height(24.dp))

        ConditionCheckingInputText(
            value = firstName,
            onValueChange = { firstName = it },
            label = stringResource(R.string.view_profile_screen_first_name_label),
            minCharacters = FIRST_NAME_MIN_LENGTH,
            maxCharacters = FIRST_NAME_MAX_LENGTH,
            testTag = "FirstNameField")

        Spacer(modifier = Modifier.height(8.dp))

        ConditionCheckingInputText(
            value = lastName,
            onValueChange = { lastName = it },
            label = stringResource(R.string.view_profile_screen_last_name_label),
            minCharacters = LAST_NAME_MIN_LENGTH,
            maxCharacters = LAST_NAME_MAX_LENGTH,
            testTag = "LastNameField")

        Spacer(modifier = Modifier.height(8.dp))

        ConditionCheckingInputText(
            value = username,
            onValueChange = { username = it },
            label = stringResource(R.string.view_profile_screen_username_label),
            minCharacters = USERNAME_MIN_LENGTH,
            maxCharacters = USERNAME_MAX_LENGTH,
            testTag = "UsernameField")

        Spacer(modifier = Modifier.height(16.dp))

        val validUserInput = areInputsValid(firstName, lastName, username)
        if (verticalButtonDisplay) {
          saveButton(copyUser(user), validUserInput)
          cancelButton()
        } else {
          Row(
              modifier = Modifier.testTag("EditComponentButtonRow"),
              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cancelButton()
                saveButton(copyUser(user), validUserInput)
              }
        }
      }
}
