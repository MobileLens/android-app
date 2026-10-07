package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.Lens
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


@Composable
fun LensSpecs(
    lens: Lens,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Focal lengths row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecCard(
                title = stringResource(R.string.spec_focal_length),
                value = lens.focalLengthText(),
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = stringResource(R.string.spec_focal_length_35mm),
                value = lens.focalLength35mmText(),
                modifier = Modifier.weight(1f)
            )
        }

        // Apertures row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecCard(
                title = stringResource(R.string.spec_aperture),
                value = lens.apertureText(),
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = stringResource(R.string.spec_aperture_35mm),
                value = lens.aperture35mmText(),
                modifier = Modifier.weight(1f)
            )
        }

        // Resolution row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecCard(
                title = stringResource(R.string.spec_resolution),
                value = lens.resolutionText(),
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = stringResource(R.string.spec_active_resolution),
                value = lens.activeResolutionText(),
                modifier = Modifier.weight(1f)
            )
        }

        // Pixel pitch + sensor type row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecCard(
                title = stringResource(R.string.spec_pixel_pitch),
                value = lens.pixelPitchText(),
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = stringResource(R.string.spec_sensor_type),
                value = lens.sensorTypeText(),
                modifier = Modifier.weight(1f)
            )
        }

        // OIS + crop factor + AF zones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecCard(
                title = stringResource(R.string.spec_stabilization),
                value = stringResource(lens.stabilization.labelRes()),
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = stringResource(R.string.spec_crop_factor),
                value = lens.cropFactorText(),
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = stringResource(R.string.spec_af_zones),
                value = lens.afZones.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        VideoResolutionsCard(lens.videoResolutions)

        // TODO: add gallery below
    }
}
@Preview(showBackground = true)
@Composable
private fun LensSpecsPreview() {
    MaterialTheme {
        LensSpecs(
            lens = PhoneCatalogue[0].lenses.first()
        )
    }
}
