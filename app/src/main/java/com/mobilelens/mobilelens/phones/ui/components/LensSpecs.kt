package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.Lens


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
                title = "Focal length",
                value = lens.focalLengthLabel,
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = "Focal length (35mm)",
                value = lens.focalLength35mmLabel,
                modifier = Modifier.weight(1f)
            )
        }

        // Apertures row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecCard(
                title = "Aperture",
                value = lens.apertureLabel,
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = "Aperture (35mm)",
                value = lens.aperture35mmLabel,
                modifier = Modifier.weight(1f)
            )
        }

        // Resolution row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecCard(
                title = "Resolution",
                value = lens.resolutionLabel,
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = "Active resolution",
                value = lens.activeResolutionLabel,
                modifier = Modifier.weight(1f)
            )
        }

        // Crop factor + sensor type row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecCard(
                title = "Pixel pitch",
                value = lens.pixelPitchLabel,
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = "Sensor type",
                value = lens.sensorSizeLabel,
                modifier = Modifier.weight(1f)
            )
        }

        // AF zones + OIS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecCard(
                title = "OIS",
                value = lens.stabilization.displayName,
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = "Crop factor",
                value = lens.cropFactorLabel,
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = "AF Zones",
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
