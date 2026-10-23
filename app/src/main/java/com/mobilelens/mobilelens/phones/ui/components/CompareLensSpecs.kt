package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.LensPair
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.data.betterSide
import com.mobilelens.mobilelens.phones.data.betterStabilization
import com.mobilelens.mobilelens.phones.data.pairLenses
import com.mobilelens.mobilelens.phones.model.VideoResolution
import com.mobilelens.mobilelens.phones.ui.activeResolutionText
import com.mobilelens.mobilelens.phones.ui.aperture35mmText
import com.mobilelens.mobilelens.phones.ui.apertureText
import com.mobilelens.mobilelens.phones.ui.cropFactorText
import com.mobilelens.mobilelens.phones.ui.focalLength35mmText
import com.mobilelens.mobilelens.phones.ui.focalLengthText
import com.mobilelens.mobilelens.phones.ui.labelRes
import com.mobilelens.mobilelens.phones.ui.pixelPitchText
import com.mobilelens.mobilelens.phones.ui.resolutionText
import com.mobilelens.mobilelens.phones.ui.sensorTypeText

/**
 * The specs of one [LensPair], the first phone's lens on the left and the second's on the right.
 * Where one value is clearly better (more megapixels, a brighter aperture, a bigger sensor...) it
 * is picked out. The decimals match what the value strings show, so equal-looking values tie.
 */
@Composable
fun CompareLensSpecs(
    pair: LensPair,
    modifier: Modifier = Modifier,
) {
    val first = pair.first
    val second = pair.second

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompareSpecCard(
            title = stringResource(R.string.spec_focal_length),
            first = first?.focalLengthText(),
            second = second?.focalLengthText(),
        )
        CompareSpecCard(
            title = stringResource(R.string.spec_focal_length_35mm),
            first = first?.focalLength35mmText(),
            second = second?.focalLength35mmText(),
        )
        CompareSpecCard(
            title = stringResource(R.string.spec_aperture),
            first = first?.apertureText(),
            second = second?.apertureText(),
            // The brightest stop, for a lens with a variable aperture
            better = betterSide(
                first?.aperture?.minOrNull(),
                second?.aperture?.minOrNull(),
                decimals = 1,
                higherIsBetter = false,
            ),
        )
        CompareSpecCard(
            title = stringResource(R.string.spec_aperture_35mm),
            first = first?.aperture35mmText(),
            second = second?.aperture35mmText(),
            better = betterSide(
                first?.aperture35mm?.minOrNull(),
                second?.aperture35mm?.minOrNull(),
                decimals = 2,
                higherIsBetter = false,
            ),
        )
        CompareSpecCard(
            title = stringResource(R.string.spec_resolution),
            first = first?.resolutionText(),
            second = second?.resolutionText(),
            better = betterSide(first?.resolution, second?.resolution, decimals = 0, higherIsBetter = true),
        )
        CompareSpecCard(
            title = stringResource(R.string.spec_active_resolution),
            first = first?.activeResolutionText(),
            second = second?.activeResolutionText(),
            better = betterSide(
                first?.activeResolution,
                second?.activeResolution,
                decimals = 0,
                higherIsBetter = true,
            ),
        )
        CompareSpecCard(
            title = stringResource(R.string.spec_pixel_pitch),
            first = first?.pixelPitchText(),
            second = second?.pixelPitchText(),
            better = betterSide(first?.pixelPitchUm, second?.pixelPitchUm, decimals = 2, higherIsBetter = true),
        )
        // 1/x": a smaller x is a bigger sensor
        CompareSpecCard(
            title = stringResource(R.string.spec_sensor_type),
            first = first?.sensorTypeText(),
            second = second?.sensorTypeText(),
            better = betterSide(
                first?.sensorTypeDenominator,
                second?.sensorTypeDenominator,
                decimals = 2,
                higherIsBetter = false,
            ),
        )
        // A smaller crop factor is a bigger sensor
        CompareSpecCard(
            title = stringResource(R.string.spec_crop_factor),
            first = first?.cropFactorText(),
            second = second?.cropFactorText(),
            better = betterSide(first?.cropFactor, second?.cropFactor, decimals = 2, higherIsBetter = false),
        )
        CompareSpecCard(
            title = stringResource(R.string.spec_stabilization),
            first = first?.let { stringResource(it.stabilization.labelRes()) },
            second = second?.let { stringResource(it.stabilization.labelRes()) },
            better = betterStabilization(first?.stabilization, second?.stabilization),
        )
        CompareSpecCard(
            title = stringResource(R.string.spec_af_zones),
            first = first?.afZones?.toString(),
            second = second?.afZones?.toString(),
            better = betterSide(
                first?.afZones?.toFloat(),
                second?.afZones?.toFloat(),
                decimals = 0,
                higherIsBetter = true,
            ),
        )
        CompareVideoCard(
            first = first?.videoResolutions,
            second = second?.videoResolutions,
        )
    }
}

/** Each phone's video modes in a column of its own. */
@Composable
private fun CompareVideoCard(
    first: List<VideoResolution>?,
    second: List<VideoResolution>?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(CardShadowElevation, MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.spec_video_resolutions),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CompareRow(
            first = { VideoModes(first, modifier = it) },
            second = { VideoModes(second, modifier = it) },
        )
    }
}

@Composable
private fun VideoModes(
    resolutions: List<VideoResolution>?,
    modifier: Modifier = Modifier,
) {
    val notAvailable = stringResource(R.string.compare_not_available)
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (resolutions.isNullOrEmpty()) {
            Text(
                text = MISSING_VALUE,
                modifier = Modifier.semantics { contentDescription = notAvailable },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        resolutions.orEmpty().forEach { resolution ->
            Text(
                text = resolution.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompareLensSpecsPreview() {
    MaterialTheme {
        CompareLensSpecs(
            pair = pairLenses(PhoneCatalogue[0].lenses, PhoneCatalogue[1].lenses).first(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompareLensSpecsMissingPreview() {
    MaterialTheme {
        // The second phone has no front camera
        CompareLensSpecs(
            pair = pairLenses(PhoneCatalogue[0].lenses, PhoneCatalogue[1].lenses).last(),
        )
    }
}
