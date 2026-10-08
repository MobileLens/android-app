package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.MockGalleryPhotos
import com.mobilelens.mobilelens.phones.model.GalleryPhoto
import com.mobilelens.mobilelens.phones.viewmodel.PhoneGalleryUiState

private val CarouselHeight = 200.dp

// Placeholder items shown while the photos load
private const val LOADING_ITEM_COUNT = 3

/** "Gallery" heading with a carousel of the phone's photos. Tapping either opens the full gallery. */
@Composable
fun PhoneGallerySection(
    uiState: PhoneGalleryUiState,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.gallery_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            FilledTonalIconButton(onClick = onOpenGallery) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.gallery_open),
                )
            }
        }

        when (uiState) {
            is PhoneGalleryUiState.Loading -> PhotoCarousel(
                photos = List(LOADING_ITEM_COUNT) { null },
                onPhotoClick = onOpenGallery,
            )
            is PhoneGalleryUiState.Error -> Text(
                text = stringResource(uiState.messageRes),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            is PhoneGalleryUiState.Success -> if (uiState.photos.isEmpty()) {
                GalleryEmptyState(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                )
            } else {
                PhotoCarousel(photos = uiState.photos, onPhotoClick = onOpenGallery)
            }
        }
    }
}

/** Material's multi-browse carousel: one large photo followed by smaller ones that grow as they scroll in. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoCarousel(
    // Null items are placeholders for photos that are still loading
    photos: List<GalleryPhoto?>,
    onPhotoClick: () -> Unit,
) {
    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { photos.size },
        preferredItemWidth = 280.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(CarouselHeight),
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) { index ->
        val photo = photos[index]
        GalleryPhotoTile(
            photo = photo,
            contentDescription = photo?.let {
                stringResource(R.string.gallery_photo, index + 1, photos.size)
            },
            modifier = Modifier
                .fillMaxSize()
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable(onClick = onPhotoClick),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneGallerySectionPreview() {
    MaterialTheme {
        PhoneGallerySection(
            uiState = PhoneGalleryUiState.Success(MockGalleryPhotos),
            onOpenGallery = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneGallerySectionEmptyPreview() {
    MaterialTheme {
        PhoneGallerySection(
            uiState = PhoneGalleryUiState.Success(emptyList()),
            onOpenGallery = {},
        )
    }
}
