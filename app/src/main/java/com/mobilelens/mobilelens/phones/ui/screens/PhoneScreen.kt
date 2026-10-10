package com.mobilelens.mobilelens.phones.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.FabMenu
import com.mobilelens.mobilelens.core.ui.FabMenuItem
import com.mobilelens.mobilelens.phones.data.MockGalleryPhotos
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.phones.ui.components.DeviceLensDetail
import com.mobilelens.mobilelens.phones.ui.components.PhoneGallerySection
import com.mobilelens.mobilelens.phones.viewmodel.PhoneGalleryUiState

@Composable
fun PhoneScreen(
    phone: Phone,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    galleryState: PhoneGalleryUiState,
    modifier: Modifier = Modifier,
    onNavigateToReviews: () -> Unit = {},
    onOpenGallery: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        DeviceLensDetail(
            lenses = phone.lenses,
            deviceInfo = phone.deviceInfo,
            isFavorite = isFavorited,
            onFavoriteClick = onFavoriteClick,
            footer = {
                PhoneGallerySection(
                    uiState = galleryState,
                    onOpenGallery = onOpenGallery,
                    // Room to scroll the carousel out from under the FAB
                    modifier = Modifier.padding(bottom = 88.dp),
                )
            },
        )

        FabMenu(
            items = listOf(
                FabMenuItem(Icons.Filled.Star, R.string.action_reviews, onNavigateToReviews),
                FabMenuItem(Icons.AutoMirrored.Filled.CompareArrows, R.string.action_compare) {},
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            itemSpacing = 8.dp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneScreenPreview() {
    MaterialTheme {
        PhoneScreen(
            phone = PhoneCatalogue[0],
            isFavorited = false,
            onFavoriteClick = {},
            galleryState = PhoneGalleryUiState.Success(MockGalleryPhotos),
        )
    }
}
