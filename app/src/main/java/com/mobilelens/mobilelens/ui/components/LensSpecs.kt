package com.mobilelens.mobilelens.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.model.Lens
import com.mobilelens.mobilelens.model.Stabilization


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
                value = "%.1f MP".format(lens.resolution),
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = "Active resolution",
                value = "%.1f MP".format(lens.activeResolution),
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
                value = "%.2f μm".format(lens.pixelPitchUm),
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = "Sensor type",
                value = "1/%.2f\"".format(lens.sensorTypeDenominator),
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
                value = when(lens.ois) {
                    Stabilization.OIS -> "Yes"
                    Stabilization.SENSORSHIFT -> "Sensor-shift"
                    Stabilization.NONE -> "No"
                },
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                title = "Crop factor",
                value = "%.2fx".format(lens.cropFactor),
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