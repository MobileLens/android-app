package com.mobilelens.mobilelens.phones.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.LoadingButtonContent
import com.mobilelens.mobilelens.core.ui.LoadingContent
import com.mobilelens.mobilelens.core.ui.UiStateCrossfade
import com.mobilelens.mobilelens.core.ui.theme.Motion
import com.mobilelens.mobilelens.core.ui.theme.rememberSlideDistance
import com.mobilelens.mobilelens.core.ui.theme.sharedAxisXIn
import com.mobilelens.mobilelens.core.ui.theme.sharedAxisXOut
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.GalleryCamera
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.ui.components.LensSpecs
import com.mobilelens.mobilelens.phones.ui.components.PhotoCameraDialog
import com.mobilelens.mobilelens.phones.ui.lensKindLabel
import com.mobilelens.mobilelens.phones.ui.tabLabel
import com.mobilelens.mobilelens.phones.viewmodel.PhotoCamera
import com.mobilelens.mobilelens.phones.viewmodel.UploadDeviceUiState
import com.mobilelens.mobilelens.phones.viewmodel.UploadGalleryUiState

private val PickedPhotoSize = 88.dp

/** The upload screen's tabs, in order: what each one sends is separate from the other. */
private enum class UploadTab(@StringRes val labelRes: Int) {
    Specs(R.string.upload_tab_specs),
    Photos(R.string.upload_tab_photos),
}

/**
 * Sends this device's lens specs to the catalogue (Specs), or photos taken with its cameras to the
 * phone's gallery (Photos). The send button in the top bar sends what the open tab shows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDeviceScreen(
    uiState: UploadDeviceUiState,
    gallery: UploadGalleryUiState,
    isSubmitting: Boolean,
    onClose: () -> Unit,
    onSendSpecs: () -> Unit,
    onSendPhotos: () -> Unit,
    onAddPhotoCamera: () -> Unit,
    onConfirmPhotoCamera: (LensType, Facing, Stabilization) -> Unit,
    onDismissPhotoCamera: () -> Unit,
    onRemovePhotoCamera: (id: Int) -> Unit,
    onAddGalleryPhotos: (cameraId: String) -> Unit,
    onRemoveGalleryPhoto: (cameraId: String, photo: Uri) -> Unit,
    onRetryGallery: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var selectedTab by rememberSaveable { mutableStateOf(UploadTab.Specs) }
    val slideDistance = rememberSlideDistance()

    // Like the close button, back does nothing while sending: leaving would stop the send partway,
    // and sending again from a new screen would submit the same cameras twice
    BackHandler(enabled = isSubmitting) {}

    val canSend = !isSubmitting && when (selectedTab) {
        UploadTab.Specs -> (uiState as? UploadDeviceUiState.Ready)?.hasUnsentSpecs == true
        UploadTab.Photos -> (gallery as? UploadGalleryUiState.Ready)?.hasPhotos == true
    }

    // Opaque, since it rises over the screen that opened it
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
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
                    Button(
                        onClick = {
                            when (selectedTab) {
                                UploadTab.Specs -> onSendSpecs()
                                UploadTab.Photos -> onSendPhotos()
                            }
                        },
                        shapes = ButtonDefaults.shapes(),
                        enabled = canSend,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        LoadingButtonContent(
                            text = stringResource(R.string.upload_submit),
                            loading = isSubmitting,
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
            )

            AnimatedVisibility(visible = isSubmitting) {
                val sendingDescription = stringResource(R.string.upload_submitting)
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = sendingDescription },
                )
            }

            PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                UploadTab.entries.forEach { tab ->
                    Tab(
                        selected = tab == selectedTab,
                        onClick = { selectedTab = tab },
                        text = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }

            // Slides the way the tabs are laid out, as in the review editor
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    val forward = targetState > initialState
                    sharedAxisXIn(forward, slideDistance) togetherWith sharedAxisXOut(forward, slideDistance)
                },
                label = "UploadTab",
            ) { tab ->
                when (tab) {
                    UploadTab.Specs -> UiStateCrossfade(state = uiState) { state ->
                        when (state) {
                            UploadDeviceUiState.Loading -> LoadingContent()
                            is UploadDeviceUiState.Error -> ErrorContent(messageRes = state.messageRes)
                            is UploadDeviceUiState.Ready -> SpecsContent(
                                state = state,
                                onAddPhotoCamera = onAddPhotoCamera,
                                onRemovePhotoCamera = onRemovePhotoCamera,
                                enabled = !isSubmitting,
                            )
                        }
                    }
                    UploadTab.Photos -> UiStateCrossfade(state = gallery) { state ->
                        when (state) {
                            UploadGalleryUiState.Loading -> LoadingContent()
                            is UploadGalleryUiState.Error -> ErrorContent(
                                messageRes = state.messageRes,
                                onRetry = onRetryGallery,
                            )
                            is UploadGalleryUiState.Ready -> if (state.cameras.isEmpty()) {
                                NoGalleryCameras(onGoToSpecs = { selectedTab = UploadTab.Specs })
                            } else {
                                GalleryContent(
                                    state = state,
                                    enabled = !isSubmitting,
                                    onAddPhotos = onAddGalleryPhotos,
                                    onRemovePhoto = onRemoveGalleryPhoto,
                                )
                            }
                        }
                    }
                }
            }
        }

        (uiState as? UploadDeviceUiState.Ready)?.pendingPhotoCamera?.let { pending ->
            PhotoCameraDialog(
                lens = pending.lens,
                onConfirm = onConfirmPhotoCamera,
                onDismiss = onDismissPhotoCamera,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SpecsContent(
    state: UploadDeviceUiState.Ready,
    onAddPhotoCamera: () -> Unit,
    onRemovePhotoCamera: (id: Int) -> Unit,
    enabled: Boolean,
) {
    // Indices of lens rows whose specs are expanded (list so rememberSaveable can restore it)
    var expandedIndices by rememberSaveable { mutableStateOf(emptyList<Int>()) }
    var expandedPhotoCameraIds by rememberSaveable { mutableStateOf(emptyList<Int>()) }
    val sentLabel = stringResource(R.string.upload_sent)
    val fromPhotoLabel = stringResource(R.string.upload_photo_camera_caption)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Text(
                text = stringResource(
                    R.string.upload_device_label,
                    "${state.deviceInfo.brand} ${state.deviceInfo.model}",
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            SectionHeading(
                title = stringResource(R.string.upload_lenses_heading),
                supporting = stringResource(R.string.upload_lenses_supporting),
            )
        }

        itemsIndexed(state.lenses) { index, lens ->
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
                supportingText = sentLabel.takeIf { index in state.sentLensIndices },
            )
            if (index < state.lenses.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeading(
                title = stringResource(R.string.upload_photo_cameras_heading),
                supporting = stringResource(R.string.upload_photo_cameras_supporting),
            )
        }

        items(state.photoCameras) { photoCamera ->
            val expanded = photoCamera.id in expandedPhotoCameraIds
            UploadLensRow(
                lens = photoCamera.lens,
                expanded = expanded,
                onToggle = {
                    expandedPhotoCameraIds = if (expanded) {
                        expandedPhotoCameraIds - photoCamera.id
                    } else {
                        expandedPhotoCameraIds + photoCamera.id
                    }
                },
                supportingText = if (photoCamera.submitted) "$fromPhotoLabel · $sentLabel" else fromPhotoLabel,
                onRemove = if (photoCamera.submitted) null else ({ onRemovePhotoCamera(photoCamera.id) }),
                enabled = enabled,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        item {
            OutlinedButton(
                onClick = onAddPhotoCamera,
                shapes = ButtonDefaults.shapes(),
                enabled = enabled,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.upload_add_photo_camera))
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, supporting: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = supporting,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun UploadLensRow(
    lens: Lens,
    expanded: Boolean,
    onToggle: () -> Unit,
    supportingText: String? = null,
    onRemove: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val toggleDescription = stringResource(
        if (expanded) R.string.upload_collapse_lens else R.string.upload_expand_lens
    )
    // The chevron turns over to point up while the specs are open
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(Motion.DURATION_MEDIUM, easing = Motion.Emphasized),
        label = "LensChevronRotation",
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            ) {
                Text(text = lens.tabLabel(), style = MaterialTheme.typography.bodyLarge)
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (onRemove != null) {
                TextButton(onClick = onRemove, shapes = ButtonDefaults.shapes(), enabled = enabled) {
                    Text(stringResource(R.string.upload_remove_photo_camera))
                }
            }
            IconButton(
                onClick = onToggle,
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.semantics { contentDescription = toggleDescription },
            ) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(chevronRotation),
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            LensSpecs(lens = lens)
        }
    }
}

@Composable
private fun GalleryContent(
    state: UploadGalleryUiState.Ready,
    enabled: Boolean,
    onAddPhotos: (cameraId: String) -> Unit,
    onRemovePhoto: (cameraId: String, photo: Uri) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.upload_photos_intro),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        itemsIndexed(state.cameras, key = { _, camera -> camera.id }) { index, camera ->
            GalleryCameraRow(
                camera = camera,
                photos = state.photos[camera.id].orEmpty(),
                enabled = enabled,
                onAddPhotos = { onAddPhotos(camera.id) },
                onRemovePhoto = { photo -> onRemovePhoto(camera.id, photo) },
            )
            if (index < state.cameras.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

/** A camera of the phone, with the photos picked for it underneath. */
@Composable
private fun GalleryCameraRow(
    camera: GalleryCamera,
    photos: List<Uri>,
    enabled: Boolean,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (Uri) -> Unit,
) {
    // e.g. "6.90 mm · f/1.7 · In review"
    val details = listOfNotNull(
        stringResource(R.string.value_focal_length, camera.focalLengthMm),
        stringResource(R.string.value_aperture, camera.aperture),
        stringResource(R.string.upload_camera_in_review).takeIf { camera.status == "pending" },
    ).joinToString(" · ")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lensKindLabel(camera.facing, camera.type),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onAddPhotos, shapes = ButtonDefaults.shapes(), enabled = enabled) {
                Icon(
                    imageVector = Icons.Filled.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.upload_photos_add))
            }
        }
        AnimatedVisibility(visible = photos.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(photos, key = { it.toString() }) { photo ->
                    PickedPhoto(
                        photo = photo,
                        enabled = enabled,
                        onRemove = { onRemovePhoto(photo) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PickedPhoto(
    photo: Uri,
    enabled: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(PickedPhotoSize)) {
        AsyncImage(
            model = photo,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium),
        )
        FilledTonalIconButton(
            onClick = onRemove,
            enabled = enabled,
            shapes = IconButtonDefaults.shapes(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.upload_photos_remove),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** The phone has no cameras in the catalogue yet, so photos have nothing to belong to. */
@Composable
private fun NoGalleryCameras(onGoToSpecs: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.upload_photos_no_cameras),
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        TextButton(
            onClick = onGoToSpecs,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(stringResource(R.string.upload_photos_go_to_specs))
        }
    }
}

private val previewGalleryCameras = listOf(
    previewGalleryCamera("cam_1", LensType.WIDE, Facing.BACK, focalLengthMm = 6.9, aperture = 1.68),
    previewGalleryCamera("cam_2", LensType.TELEPHOTO, Facing.BACK, focalLengthMm = 18.0, aperture = 2.8, status = "pending"),
    previewGalleryCamera("cam_3", LensType.WIDE, Facing.FRONT, focalLengthMm = 2.7, aperture = 2.2),
)

private fun previewGalleryCamera(
    id: String,
    type: LensType,
    facing: Facing,
    focalLengthMm: Double,
    aperture: Double,
    status: String = "approved",
) = GalleryCamera(
    id = id,
    type = type,
    facing = facing,
    focalLengthMm = focalLengthMm,
    aperture = aperture,
    cropFactor = 4.0,
    resolutionMp = 50.0,
    status = status,
)

@Preview(showBackground = true)
@Composable
private fun UploadDeviceScreenPreview() {
    MaterialTheme {
        UploadDeviceScreen(
            uiState = UploadDeviceUiState.Ready(
                deviceInfo = PhoneCatalogue[0].deviceInfo,
                lenses = PhoneCatalogue[0].lenses.dropLast(1),
                sentLensIndices = setOf(0),
                photoCameras = listOf(
                    PhotoCamera(id = 0, lens = PhoneCatalogue[0].lenses.last())
                ),
            ),
            gallery = UploadGalleryUiState.Ready(previewGalleryCameras),
            isSubmitting = false,
            onClose = {},
            onSendSpecs = {},
            onSendPhotos = {},
            onAddPhotoCamera = {},
            onConfirmPhotoCamera = { _, _, _ -> },
            onDismissPhotoCamera = {},
            onRemovePhotoCamera = {},
            onAddGalleryPhotos = {},
            onRemoveGalleryPhoto = { _, _ -> },
            onRetryGallery = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryContentPreview() {
    MaterialTheme {
        GalleryContent(
            state = UploadGalleryUiState.Ready(previewGalleryCameras),
            enabled = true,
            onAddPhotos = {},
            onRemovePhoto = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoGalleryCamerasPreview() {
    MaterialTheme {
        NoGalleryCameras(onGoToSpecs = {})
    }
}
