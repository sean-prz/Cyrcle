package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.theme.atoms.RadioButton
import com.github.se.cyrcle.ui.theme.atoms.Text

/**
 * Create a question and two radio buttons for a boolean value.
 *
 * @param question The question to display.
 * @param state The MutableState that stores the boolean value.
 */
@Composable
fun BooleanRadioButton(
    question: String,
    state: MutableState<Boolean>,
    testTag: String = "BooleanRadioButton"
) {
  Column(Modifier.testTag(testTag)) {
    Text(
        text = question,
        Modifier.testTag("${testTag}Question"),
        color = MaterialTheme.colorScheme.onBackground)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()) {
          RadioButton(
              selected = state.value,
              onClick = { state.value = true },
              modifier = Modifier.padding(2.dp),
              testTag = "${testTag}YesRadioButton")
          Text(text = stringResource(R.string.yes), testTag = "${testTag}YesText")
          RadioButton(
              selected = !state.value,
              onClick = { state.value = false },
              modifier = Modifier.padding(start = 20.dp),
              testTag = "${testTag}NoRadioButton")
          Text(text = stringResource(R.string.no), testTag = "${testTag}NoText")
        }
  }
}
