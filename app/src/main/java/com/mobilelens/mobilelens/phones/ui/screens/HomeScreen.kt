package com.mobilelens.mobilelens.phones.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.FabMenu
import com.mobilelens.mobilelens.core.ui.FabMenuItem
import com.mobilelens.mobilelens.core.ui.UiStateCrossfade
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.ui.components.DeviceLensDetail
import com.mobilelens.mobilelens.phones.viewmodel.CameraUiState
import com.mobilelens.mobilelens.phones.viewmodel.CameraViewModel

@Composable
fun HomeScreen(
    cameraViewModel: CameraViewModel,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
    onWriteReview: () -> Unit = {},
    onUpload: () -> Unit = {},
) {
    val uiState by cameraViewModel.uiState.collectAsState()
    HomeScreenContent(
        uiState = uiState,
        isLoggedIn = isLoggedIn,
        modifier = modifier,
        onWriteReview = onWriteReview,
        onUpload = onUpload,
    )
}

@Composable
fun HomeScreenContent(
    uiState: CameraUiState,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
    onWriteReview: () -> Unit = {},
    onUpload: () -> Unit = {},
) {
    var showLoginRequired by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        UiStateCrossfade(state = uiState) { state ->
            when (state) {
                CameraUiState.Checking -> CameraChecking()
                is CameraUiState.Fallback -> CameraFallback(stringResource(state.messageRes))
                is CameraUiState.Success -> DeviceLensDetail(
                    lenses = state.lenses,
                    deviceInfo = state.deviceInfo,
                    // Room to scroll the last card out from under the FAB
                    footer = { Spacer(modifier = Modifier.height(88.dp)) },
                )
            }
        }

        FabMenu(
            items = listOf(
                FabMenuItem(Icons.Filled.RateReview, R.string.action_write_review, onWriteReview),
                FabMenuItem(Icons.Filled.Upload, R.string.action_upload) {
                    if (isLoggedIn) onUpload() else showLoginRequired = true
                },
                FabMenuItem(Icons.AutoMirrored.Filled.CompareArrows, R.string.action_compare) {},
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    if (showLoginRequired) {
        AlertDialog(
            onDismissRequest = { showLoginRequired = false },
            title = { Text(stringResource(R.string.upload_login_required_title)) },
            text = { Text(stringResource(R.string.upload_login_required_message)) },
            confirmButton = {
                TextButton(onClick = { showLoginRequired = false }) {
                    Text(stringResource(R.string.upload_login_required_ok))
                }
            },
        )
    }
}

@Composable
private fun CameraChecking(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.home_checking_cameras),
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

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
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.PhoneAndroid,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.home_camera_details_unavailable),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreenContent(
            uiState = CameraUiState.Success(
                lenses = PhoneCatalogue[0].lenses,
                deviceInfo = PhoneCatalogue[0].deviceInfo
            ),
            isLoggedIn = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenCheckingPreview() {
    MaterialTheme {
        HomeScreenContent(uiState = CameraUiState.Checking, isLoggedIn = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenFallbackPreview() {
    MaterialTheme {
        HomeScreenContent(
            uiState = CameraUiState.Fallback(R.string.home_no_cameras),
            isLoggedIn = false,
        )
    }
}
