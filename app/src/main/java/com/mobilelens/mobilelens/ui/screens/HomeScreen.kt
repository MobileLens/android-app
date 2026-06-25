package com.mobilelens.mobilelens.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.model.Lens
import com.mobilelens.mobilelens.model.Stabilization
import com.mobilelens.mobilelens.ui.components.SpecCard
import com.mobilelens.mobilelens.viewmodel.CameraUiState
import com.mobilelens.mobilelens.viewmodel.CameraViewModel

@Composable
fun HomeScreen(
    cameraViewModel: CameraViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by cameraViewModel.uiState.collectAsState()

    when (val state = uiState) {
        CameraUiState.Checking -> CameraChecking(modifier)
        is CameraUiState.Fallback -> CameraFallback(state.message, modifier)
        is CameraUiState.Success -> CameraLensTabs(state.lenses, modifier)
    }
}

// Loading screen
@Composable
private fun CameraChecking(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            text = "Checking this device's cameras",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

// Temporary message
// TODO: Implement EXIF data extraction for user-uploaded picture
@Composable
private fun CameraFallback(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Camera details were not available from this device.",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CameraLensTabs(
    lenses: List<Lens>,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            lenses.forEachIndexed { index, _ ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text("Camera ${index + 1}") }
                )
            }
        }

        if (lenses.isNotEmpty()) {
            LensSpecs(lenses[selectedTabIndex])
        }
    }
}

@Composable
private fun LensSpecs(lens: Lens) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                title = "Crop factor",
                value = "%.2fx".format(lens.cropFactor),
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
                title = "AF Zones",
                value = lens.afZones.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // TODO: add pixel pitch + video resolutions


        // TODO: Gallery here
    }
}
