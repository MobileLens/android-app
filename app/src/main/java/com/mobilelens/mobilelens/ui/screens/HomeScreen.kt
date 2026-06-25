package com.mobilelens.mobilelens.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.model.Lens
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
        is CameraUiState.Success -> CameraLensList(state.lenses, modifier)
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

// Main app screen
// Temporarly shows lenses as a List item with basic specs instead of full Home Screen
// TODO: Add phone info (manufacturer, model, OS version)
@Composable
private fun CameraLensList(
    lenses: List<Lens>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                Text(
                    text = "This device",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${lenses.size} detected cameras",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        itemsIndexed(lenses, key = { index, _ -> index }) { index, lens ->
            ListItem(
                headlineContent = {
                    Text("Camera ${index + 1}")
                },
                supportingContent = {
                    Text(
                        "${lens.facingLabel} • ${lens.activeResolutionLabel} MP • ${lens.apertureLabel}"
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                },
                trailingContent = {
                    Text(
                        text = lens.focalLengthLabel,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
