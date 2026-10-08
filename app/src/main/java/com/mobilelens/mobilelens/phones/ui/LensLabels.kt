package com.mobilelens.mobilelens.phones.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization

// Localized labels for the lens model. Numbers are formatted by the string resources,
// so they use the app locale's decimal separator.

@StringRes
fun Facing.labelRes(): Int = when (this) {
    Facing.FRONT -> R.string.facing_front
    Facing.BACK -> R.string.facing_back
    Facing.OTHER -> R.string.facing_other
}

@StringRes
fun LensType.labelRes(): Int = when (this) {
    LensType.WIDE -> R.string.lens_type_wide
    LensType.ULTRAWIDE -> R.string.lens_type_ultrawide
    LensType.TELEPHOTO -> R.string.lens_type_telephoto
    LensType.MACRO -> R.string.lens_type_macro
    LensType.OTHER -> R.string.lens_type_other
}

@StringRes
fun Stabilization.labelRes(): Int = when (this) {
    Stabilization.NONE -> R.string.stabilization_none
    Stabilization.OIS -> R.string.stabilization_ois
    Stabilization.SENSORSHIFT -> R.string.stabilization_sensor_shift
}

/** Tab title, e.g. "Back wide". */
@Composable
fun Lens.tabLabel(): String {
    val locale = LocalConfiguration.current.locales[0]
    return stringResource(
        R.string.lens_tab_label,
        stringResource(facing.labelRes()),
        stringResource(type.labelRes()).lowercase(locale),
    )
}

@Composable
fun Lens.focalLengthText(): String = focalLength.formatOptics(
    singleRes = R.string.value_focal_length,
    rangeRes = R.string.value_focal_length_range,
)

@Composable
fun Lens.focalLength35mmText(): String = focalLength35mm.formatOptics(
    singleRes = R.string.value_focal_length_35mm,
    rangeRes = R.string.value_focal_length_35mm_range,
)

@Composable
fun Lens.apertureText(): String = aperture.formatOptics(
    singleRes = R.string.value_aperture,
    rangeRes = R.string.value_aperture_range,
)

@Composable
fun Lens.aperture35mmText(): String = aperture35mm.formatOptics(
    singleRes = R.string.value_aperture_35mm,
    rangeRes = R.string.value_aperture_35mm_range,
)

@Composable
fun Lens.resolutionText(): String = stringResource(R.string.value_megapixels, resolution)

@Composable
fun Lens.activeResolutionText(): String = stringResource(R.string.value_megapixels, activeResolution)

@Composable
fun Lens.sensorTypeText(): String = stringResource(R.string.value_sensor_type, sensorTypeDenominator)

@Composable
fun Lens.pixelPitchText(): String = stringResource(R.string.value_pixel_pitch, pixelPitchUm)

@Composable
fun Lens.cropFactorText(): String = stringResource(R.string.value_crop_factor, cropFactor)

// One reading, or min–max when Camera2 reports several available stops
@Composable
private fun List<Float>.formatOptics(
    @StringRes singleRes: Int,
    @StringRes rangeRes: Int,
): String {
    val (min, max) = opticsSpan(this) ?: return ""
    return if (min == max) {
        stringResource(singleRes, min)
    } else {
        stringResource(rangeRes, min, max)
    }
}
