package com.mobilelens.mobilelens.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.mobilelens.mobilelens.model.DeviceInfo
import com.mobilelens.mobilelens.model.Lens
import com.mobilelens.mobilelens.viewmodel.CameraUiState
import com.mobilelens.mobilelens.viewmodel.CameraViewModel
import com.mobilelens.mobilelens.ui.components.LensSpecs
import com.mobilelens.mobilelens.ui.components.DeviceInfoSection

@Composable
fun HomeScreen(
    cameraViewModel: CameraViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by cameraViewModel.uiState.collectAsState()

    when (val state = uiState) {
        CameraUiState.Checking -> CameraChecking(modifier)
        is CameraUiState.Fallback -> CameraFallback(state.message, modifier)
        is CameraUiState.Success -> CameraLensTabs(state.lenses, state.deviceInfo, modifier)
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
    deviceInfo: DeviceInfo,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        DeviceInfoSection(deviceInfo)

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
