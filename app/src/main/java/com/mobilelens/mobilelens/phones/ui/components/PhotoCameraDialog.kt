package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.ui.apertureText
import com.mobilelens.mobilelens.phones.ui.focalLength35mmText
import com.mobilelens.mobilelens.phones.ui.focalLengthText
import com.mobilelens.mobilelens.phones.ui.labelRes
import com.mobilelens.mobilelens.phones.ui.resolutionText

// "Other" facing stands for external cameras, which aren't photographed through the stock app
private val PhotoCameraFacings = listOf(Facing.BACK, Facing.FRONT)

/**
 * Confirms a camera read from a photo's EXIF. EXIF doesn't record the lens type, facing or
 * stabilization, so the user sets them, starting from the type its focal length suggests.
 */
@Composable
fun PhotoCameraDialog(
    lens: Lens,
    onConfirm: (LensType, Facing, Stabilization) -> Unit,
    onDismiss: () -> Unit,
) {
    var type by rememberSaveable { mutableStateOf(lens.type) }
    var facing by rememberSaveable { mutableStateOf(lens.facing) }
    var stabilization by rememberSaveable { mutableStateOf(lens.stabilization) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.upload_photo_camera_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(
                        R.string.upload_photo_camera_summary,
                        lens.focalLengthText(),
                        lens.focalLength35mmText(),
                        lens.apertureText(),
                        lens.resolutionText(),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.upload_photo_camera_check),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ChoiceChips(
                    title = stringResource(R.string.upload_photo_camera_type),
                    options = LensType.entries,
                    selected = type,
                    optionLabel = { stringResource(it.labelRes()) },
                    onSelect = { type = it },
                )
                ChoiceChips(
                    title = stringResource(R.string.upload_photo_camera_facing),
                    options = PhotoCameraFacings,
                    selected = facing,
                    optionLabel = { stringResource(it.labelRes()) },
                    onSelect = { facing = it },
                )
                ChoiceChips(
                    title = stringResource(R.string.upload_photo_camera_stabilization),
                    options = Stabilization.entries,
                    selected = stabilization,
                    optionLabel = { stringResource(it.labelRes()) },
                    onSelect = { stabilization = it },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(type, facing, stabilization) }) {
                Text(stringResource(R.string.upload_photo_camera_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

/** A titled set of chips with exactly one of [options] selected. */
@Composable
private fun <T> ChoiceChips(
    title: String,
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(optionLabel(option)) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun PhotoCameraDialogPreview() {
    MaterialTheme {
        PhotoCameraDialog(
            lens = PhoneCatalogue[0].lenses.last(),
            onConfirm = { _, _, _ -> },
            onDismiss = {},
        )
    }
}
