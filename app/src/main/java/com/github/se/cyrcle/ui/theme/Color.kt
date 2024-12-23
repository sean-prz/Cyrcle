package com.github.se.cyrcle.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.github.se.cyrcle.ui.theme.ColorLevel.ERROR
import com.github.se.cyrcle.ui.theme.ColorLevel.INVERSE_PRIMARY
import com.github.se.cyrcle.ui.theme.ColorLevel.PRIMARY
import com.github.se.cyrcle.ui.theme.ColorLevel.SECONDARY
import com.github.se.cyrcle.ui.theme.ColorLevel.TERTIARY

val Aero = Color(0xFF5BC0EB)
val ShamrockGreen = Color(0xFF229C6B)
val YInMnBlue = Color(0xFF23538F)

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Red = Color(0xFFFF0000)

val Cerulean = Color(0xFF22799B)
val CeruleanLowest = Color(0xFF134558)
val CeruleanLow = Color(0xFF1B5F7A)
val CeruleanHigh = Color(0xFF2687AE)
val CeruleanHighest = Color(0xFFF3FBFE)

/**
 * Invert a color, without changing its alpha channel.
 *
 * @param color The color to inverse
 * @return The color with all but the alpha one inverted.
 */
fun invertColor(color: Color): Color {
  val r = 255 - (color.red * 255).toInt()
  val g = 255 - (color.green * 255).toInt()
  val b = 255 - (color.blue * 255).toInt()
  return Color(r, g, b, (color.alpha * 255).toInt())
}

/**
 * A class containing the choice of color for an element.
 *
 * @property PRIMARY The primary color "scheme"
 * @property INVERSE_PRIMARY The primary color "scheme", but with the `onPrimary` and `primary`
 *   inverted (same for `container`)
 * @property SECONDARY The secondary color "scheme"
 * @property TERTIARY The tertiary color "scheme"
 * @property ERROR The error color "scheme"
 */
enum class ColorLevel {
  PRIMARY,
  INVERSE_PRIMARY,
  SECONDARY,
  TERTIARY,
  ERROR
}

/**
 * Get the color for a content from a ColorLevel.
 *
 * @param colorLevel The chosen ColorLevel
 * @return The base color from the `colorLevel`.
 */
@Composable
fun getColor(colorLevel: ColorLevel): Color {
  return when (colorLevel) {
    PRIMARY -> MaterialTheme.colorScheme.primary
    INVERSE_PRIMARY -> MaterialTheme.colorScheme.onPrimary
    SECONDARY -> MaterialTheme.colorScheme.secondary
    TERTIARY -> MaterialTheme.colorScheme.tertiary
    ERROR -> MaterialTheme.colorScheme.error
  }
}

/**
 * Get the color for content on a color from a ColorLevel.
 *
 * @param colorLevel The chosen ColorLevel
 * @return The container color from the `colorLevel`.
 */
@Composable
fun getOnColor(colorLevel: ColorLevel): Color {
  return when (colorLevel) {
    PRIMARY -> MaterialTheme.colorScheme.onPrimary
    INVERSE_PRIMARY -> MaterialTheme.colorScheme.primary
    SECONDARY -> MaterialTheme.colorScheme.onSecondary
    TERTIARY -> MaterialTheme.colorScheme.onTertiary
    ERROR -> MaterialTheme.colorScheme.onError
  }
}

/**
 * Get the color for a container from a ColorLevel.
 *
 * @param colorLevel The chosen ColorLevel
 * @return The container color from the `colorLevel`.
 */
@Composable
fun getContainerColor(colorLevel: ColorLevel): Color {
  return when (colorLevel) {
    PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    INVERSE_PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
    TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
    ERROR -> MaterialTheme.colorScheme.errorContainer
  }
}

/**
 * Get the color for the content on a container from a ColorLevel.
 *
 * @param colorLevel The chosen ColorLevel
 * @return The onContainer color from the `colorLevel`.
 */
@Composable
fun getOnContainerColor(colorLevel: ColorLevel): Color {
  return when (colorLevel) {
    PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    INVERSE_PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    ERROR -> MaterialTheme.colorScheme.onErrorContainer
  }
}

/**
 * Get the colors for a button from a ColorScheme.
 *
 * @param colorLevel The chosen ColorScheme
 * @return The `ButtonColors` from the `colorScheme`
 */
@Composable
fun getButtonColors(colorLevel: ColorLevel): ButtonColors {
  return ButtonDefaults.buttonColors(
      containerColor = getColor(colorLevel),
      contentColor = getOnColor(colorLevel),
      disabledContainerColor = disabledColor(),
      disabledContentColor = onDisabledColor())
}

/**
 * Get the colors for a button from a ColorScheme.
 *
 * @param colorLevel The chosen ColorScheme
 * @return The `ButtonColors` from the `colorScheme`
 */
@Composable
fun getIconButtonColors(colorLevel: ColorLevel): IconButtonColors {
  return IconButtonDefaults.iconButtonColors(
      containerColor = getColor(colorLevel),
      contentColor = getOnColor(colorLevel),
      disabledContainerColor = disabledColor(),
      disabledContentColor = onDisabledColor())
}

@Composable
fun getInvertedIconButtonColors(colorLevel: ColorLevel): IconButtonColors {
  return IconButtonDefaults.iconButtonColors(
      containerColor = getOnColor(colorLevel),
      contentColor = getColor(colorLevel),
      disabledContainerColor = disabledColor(),
      disabledContentColor = onDisabledColor())
}

/**
 * Get the colors for a text field from a ColorScheme.
 *
 * @param colorLevel The chosen ColorScheme
 * @return The `TextFieldColors` from the `colorScheme`
 */
@Composable
fun getOutlinedTextFieldColors(colorLevel: ColorLevel): TextFieldColors {
  return OutlinedTextFieldDefaults.colors()
      .copy(
          unfocusedIndicatorColor = defaultOnColor(),
          focusedIndicatorColor = getColor(colorLevel),
          unfocusedTextColor = defaultOnColor(),
          focusedTextColor = defaultOnColor())
}

/**
 * Get the colors for a text field from a ColorScheme (Destined for Search Text Fields).
 *
 * @param colorLevel The chosen ColorScheme
 * @return The `TextFieldColors` from the `colorScheme`
 */
@Composable
fun getOutlinedTextFieldColorsSearchBar(colorLevel: ColorLevel): TextFieldColors {
  return OutlinedTextFieldDefaults.colors(
      focusedTextColor = defaultOnColor(),
      focusedLabelColor = defaultOnColor(),
      focusedBorderColor = getColor(colorLevel),
      unfocusedBorderColor = getColor(colorLevel),
      cursorColor = defaultOnColor(),
      unfocusedContainerColor = invertColor(defaultOnColor()),
      focusedContainerColor = invertColor(defaultOnColor()),
      unfocusedPlaceholderColor = defaultOnColor())
}

/**
 * Get the colors for a checkbox from a ColorScheme.
 *
 * @param colorLevel The chosen ColorScheme
 * @return The `CheckboxColors` from the `colorScheme`
 */
@Composable
fun getCheckBoxColors(colorLevel: ColorLevel): CheckboxColors {
  return CheckboxDefaults.colors()
      .copy(
          checkedCheckmarkColor = getOnColor(colorLevel),
          uncheckedCheckmarkColor = getOnColor(colorLevel),
          checkedBoxColor = getColor(colorLevel),
          uncheckedBoxColor = Color.Transparent,
          checkedBorderColor = defaultOnColor(),
          uncheckedBorderColor = defaultOnColor())
}
