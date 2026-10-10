package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.theme.Motion
import com.mobilelens.mobilelens.phones.data.MockGalleryPhotos
import com.mobilelens.mobilelens.phones.model.GalleryPhoto

/**
 * A gallery photo cropped to fill the tile. The placeholder shapes show until it has loaded, and
 * on their own when [photo] is null, which stands in for a photo that is still being fetched.
 * A failed load shows a broken-image icon instead of those shapes. The photo fades in over the
 * shapes as they fade out.
 */
@Composable
fun GalleryPhotoTile(
    photo: GalleryPhoto?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    var isLoaded by remember(photo) { mutableStateOf(false) }
    var isFailed by remember(photo) { mutableStateOf(false) }

    GalleryPhotoTileContent(
        showPlaceholder = !isLoaded && !isFailed,
        showError = isFailed,
        modifier = modifier,
    ) {
        if (photo != null && !isFailed) {
            AsyncImage(
                model = photo.imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onSuccess = { isLoaded = true },
                onError = { isFailed = true },
            )
        }
    }
}

@Composable
private fun GalleryPhotoTileContent(
    showPlaceholder: Boolean,
    showError: Boolean,
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        // Only ever leaves, in step with the image loader's crossfade
        AnimatedVisibility(
            visible = showPlaceholder,
            enter = EnterTransition.None,
            exit = fadeOut(tween(Motion.DURATION_MEDIUM)),
        ) {
            PhotoPlaceholder(modifier = Modifier.fillMaxSize(0.6f))
        }
        AnimatedVisibility(
            visible = showError,
            enter = fadeIn(tween(Motion.DURATION_MEDIUM)),
            exit = fadeOut(tween(Motion.DURATION_SHORT)),
        ) {
            Icon(
                imageVector = Icons.Outlined.BrokenImage,
                contentDescription = stringResource(R.string.gallery_photo_failed),
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        image()
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryPhotoTilePreview() {
    MaterialTheme {
        GalleryPhotoTile(
            photo = MockGalleryPhotos[0],
            contentDescription = null,
            modifier = Modifier
                .size(width = 300.dp, height = 180.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryPhotoTileFailedPreview() {
    MaterialTheme {
        GalleryPhotoTileContent(
            showPlaceholder = false,
            showError = true,
            modifier = Modifier
                .size(width = 300.dp, height = 180.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
    }
}
