package com.mobilelens.mobilelens.phones.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.LoadingContent
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.DeviceInfo
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.ui.components.LensSpecs
import com.mobilelens.mobilelens.phones.ui.tabLabel
import com.mobilelens.mobilelens.phones.viewmodel.UploadDeviceUiState
import com.mobilelens.mobilelens.phones.viewmodel.UploadSubmitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDeviceScreen(
    uiState: UploadDeviceUiState,
    submitState: UploadSubmitState,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    onAddPhoto: (lensIndex: Int) -> Unit,
    onRemovePhoto: (lensIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val isSubmitting = submitState == UploadSubmitState.Submitting

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(stringResource(R.string.upload_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose, enabled = !isSubmitting) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.upload_screen_close),
                        )
                    }
                },
                actions = {
                    val canSubmit = uiState is UploadDeviceUiState.Ready && !isSubmitting
                    Button(
                        onClick = onSubmit,
                        enabled = canSubmit,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(stringResource(R.string.upload_submit))
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
            )

            if (isSubmitting) {
                val uploadingDescription = stringResource(R.string.upload_submitting)
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = uploadingDescription },
                )
            }

            when (val state = uiState) {
                UploadDeviceUiState.Loading -> LoadingContent()
                is UploadDeviceUiState.Error -> ErrorContent(messageRes = state.messageRes)
                is UploadDeviceUiState.Ready -> UploadReadyContent(
                    deviceInfo = state.deviceInfo,
                    lenses = state.lenses,
                    photoUris = state.photoUris,
                    onAddPhoto = onAddPhoto,
                    onRemovePhoto = onRemovePhoto,
                    enabled = !isSubmitting,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun UploadReadyContent(
    deviceInfo: DeviceInfo,
    lenses: List<Lens>,
    photoUris: Map<Int, Uri>,
    onAddPhoto: (lensIndex: Int) -> Unit,
    onRemovePhoto: (lensIndex: Int) -> Unit,
    enabled: Boolean,
) {
    // Indices of lens rows whose specs are expanded (list so rememberSaveable can restore it)
    var expandedIndices by rememberSaveable { mutableStateOf(emptyList<Int>()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Text(
                text = stringResource(
                    R.string.upload_device_label,
                    "${deviceInfo.brand} ${deviceInfo.model}",
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            Text(
                text = stringResource(R.string.upload_lenses_heading),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.upload_lenses_supporting),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        itemsIndexed(lenses) { index, lens ->
            val expanded = index in expandedIndices
            UploadLensRow(
                lens = lens,
                expanded = expanded,
                onToggle = {
                    expandedIndices = if (expanded) {
                        expandedIndices - index
                    } else {
                        expandedIndices + index
                    }
                },
            )
            if (index < lenses.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.upload_photos_heading),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.upload_photos_supporting),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        itemsIndexed(lenses) { index, lens ->
            LensPhotoRow(
                label = lens.tabLabel(),
                hasPhoto = photoUris.containsKey(index),
                enabled = enabled,
                onAddPhoto = { onAddPhoto(index) },
                onRemovePhoto = { onRemovePhoto(index) },
            )
        }
    }
}

@Composable
private fun UploadLensRow(
    lens: Lens,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val toggleDescription = stringResource(
        if (expanded) R.string.upload_collapse_lens else R.string.upload_expand_lens
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = lens.tabLabel(),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
            IconButton(
                onClick = onToggle,
                modifier = Modifier.semantics { contentDescription = toggleDescription },
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            LensSpecs(lens = lens)
        }
    }
}

@Composable
private fun LensPhotoRow(
    label: String,
    hasPhoto: Boolean,
    enabled: Boolean,
    onAddPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (hasPhoto) {
                Text(
                    text = stringResource(R.string.upload_photo_attached),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (hasPhoto) {
            TextButton(onClick = onRemovePhoto, enabled = enabled) {
                Text(stringResource(R.string.upload_remove_photo))
            }
            OutlinedButton(onClick = onAddPhoto, enabled = enabled) {
                Text(stringResource(R.string.upload_change_photo))
            }
        } else {
            OutlinedButton(onClick = onAddPhoto, enabled = enabled) {
                Text(stringResource(R.string.upload_add_photo))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UploadDeviceScreenPreview() {
    MaterialTheme {
        UploadDeviceScreen(
            uiState = UploadDeviceUiState.Ready(
                deviceInfo = PhoneCatalogue[0].deviceInfo,
                lenses = PhoneCatalogue[0].lenses,
            ),
            submitState = UploadSubmitState.Idle,
            onClose = {},
            onSubmit = {},
            onAddPhoto = {},
            onRemovePhoto = {},
        )
    }
}
