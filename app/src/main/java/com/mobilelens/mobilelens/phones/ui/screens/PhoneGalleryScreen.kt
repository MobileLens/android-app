package com.mobilelens.mobilelens.phones.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.UiStateCrossfade
import com.mobilelens.mobilelens.phones.data.MockGalleryPhotos
import com.mobilelens.mobilelens.phones.model.GalleryPhoto
import com.mobilelens.mobilelens.phones.ui.GALLERY_QUILT_COLUMNS
import com.mobilelens.mobilelens.phones.ui.components.GalleryEmptyState
import com.mobilelens.mobilelens.phones.ui.components.GalleryPhotoTile
import com.mobilelens.mobilelens.phones.ui.galleryQuiltSpan
import com.mobilelens.mobilelens.phones.viewmodel.PhoneGalleryUiState

private val QuiltRowHeight = 176.dp

// Placeholder tiles shown while the photos load
private const val LOADING_TILE_COUNT = 6

/** All photos of one phone. Has its own top bar, so MainApp hides the search bar here. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneGalleryScreen(
    phoneModel: String,
    uiState: PhoneGalleryUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LargeTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.gallery_screen_title, phoneModel),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                    )
                }
            },
            // Insets are handled by the parent Scaffold
            windowInsets = WindowInsets(0, 0, 0, 0),
            scrollBehavior = scrollBehavior,
        )

        // Space left under the bar, so the last row stays on screen
        UiStateCrossfade(state = uiState, modifier = Modifier.weight(1f).fillMaxSize()) { state ->
            when (state) {
                is PhoneGalleryUiState.Loading -> PhotoQuilt(photos = List(LOADING_TILE_COUNT) { null })
                is PhoneGalleryUiState.Error -> ErrorContent(messageRes = state.messageRes)
                is PhoneGalleryUiState.Success -> if (state.photos.isEmpty()) {
                    GalleryEmptyState(modifier = Modifier.fillMaxSize())
                } else {
                    PhotoQuilt(photos = state.photos)
                }
            }
        }
    }
}

@Composable
private fun PhotoQuilt(
    // Null items are placeholders for photos that are still loading
    photos: List<GalleryPhoto?>,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GALLERY_QUILT_COLUMNS),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(
            items = photos,
            span = { index, _ -> GridItemSpan(galleryQuiltSpan(index, photos.size)) },
        ) { index, photo ->
            GalleryPhotoTile(
                photo = photo,
                contentDescription = photo?.let {
                    stringResource(R.string.gallery_photo, index + 1, photos.size)
                },
                modifier = Modifier
                    .height(QuiltRowHeight)
                    .clip(MaterialTheme.shapes.large),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneGalleryScreenPreview() {
    MaterialTheme {
        PhoneGalleryScreen(
            phoneModel = "iPhone 17 Pro",
            uiState = PhoneGalleryUiState.Success(MockGalleryPhotos),
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneGalleryScreenEmptyPreview() {
    MaterialTheme {
        PhoneGalleryScreen(
            phoneModel = "iPhone 17 Pro",
            uiState = PhoneGalleryUiState.Success(emptyList()),
            onBackClick = {},
        )
    }
}
