package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobilelens.mobilelens.phones.data.MockGalleryPhotos
import com.mobilelens.mobilelens.phones.model.GalleryPhoto

/**
 * A gallery photo cropped to fill the tile. The placeholder shapes show until it has loaded, and
 * on their own when [photo] is null, which stands in for a photo that is still being fetched.
 */
@Composable
fun GalleryPhotoTile(
    photo: GalleryPhoto?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    var isLoaded by remember(photo) { mutableStateOf(false) }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (!isLoaded) {
            PhotoPlaceholder(modifier = Modifier.fillMaxSize(0.6f))
        }
        if (photo != null) {
            AsyncImage(
                model = photo.imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onSuccess = { isLoaded = true },
            )
        }
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
