package com.mobilelens.mobilelens.phones.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.LoadingContent
import com.mobilelens.mobilelens.phones.ui.screens.CatalogueScreen
import com.mobilelens.mobilelens.phones.ui.screens.FavoritesScreen
import com.mobilelens.mobilelens.phones.ui.screens.HomeScreen
import com.mobilelens.mobilelens.phones.ui.screens.PhoneGalleryScreen
import com.mobilelens.mobilelens.phones.ui.screens.PhoneScreen
import com.mobilelens.mobilelens.phones.viewmodel.CameraViewModel
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueUiState
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueViewModel
import com.mobilelens.mobilelens.phones.viewmodel.PhoneDetailsUiState
import com.mobilelens.mobilelens.phones.viewmodel.PhoneDetailsViewModel
import com.mobilelens.mobilelens.phones.viewmodel.PhoneGalleryViewModel

/**
 * Home, Favorites, Catalogue, PhoneDetails and PhoneGallery.
 *
 * State owned by MainApp is passed as getters, so it's read when a route composes rather than
 * when the graph is built.
 */
fun NavGraphBuilder.phonesRoutes(
    navController: NavController,
    cameraViewModel: CameraViewModel,
    catalogueViewModel: CatalogueViewModel,
    selectedPhoneId: () -> String?,
    favoritePhoneIds: () -> List<String>,
    onToggleFavorite: (phoneId: String) -> Unit,
) {
    composable<Screen.Home> {
        HomeScreen(
            cameraViewModel = cameraViewModel,
            onWriteReview = { navController.navigate(Screen.WriteReview()) },
        )
    }
    composable<Screen.Favorites> {
        val favoritePhones by catalogueViewModel.favoritePhones.collectAsState()

        FavoritesScreen(
            favoritePhones = favoritePhones,
            onPhoneClick = { phone ->
                navController.navigate(Screen.PhoneDetails(phone.id))
            }
        )
    }
    composable<Screen.Catalogue> {
        val catalogueState by catalogueViewModel.catalogueState.collectAsState()

        when (val state = catalogueState) {
            is CatalogueUiState.Loading -> LoadingContent()
            is CatalogueUiState.Error -> ErrorContent(messageRes = state.messageRes)
            is CatalogueUiState.Success -> {
                CatalogueScreen(
                    phones = state.phones,
                    selectedPhoneId = selectedPhoneId(),
                    onPhoneClick = { phone ->
                        navController.navigate(Screen.PhoneDetails(phone.id))
                    },
                    isRefreshing = state.isRefreshing
                )
            }
        }
    }
    composable<Screen.PhoneDetails> { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.PhoneDetails>()
        val phoneDetailsViewModel: PhoneDetailsViewModel = viewModel()
        val phoneState by phoneDetailsViewModel.uiState.collectAsState()
        val galleryViewModel: PhoneGalleryViewModel = viewModel()
        val galleryState by galleryViewModel.uiState.collectAsState()

        LaunchedEffect(route.phoneId) {
            phoneDetailsViewModel.loadPhone(route.phoneId)
            galleryViewModel.loadPhotos(route.phoneId)
        }

        when (val state = phoneState) {
            is PhoneDetailsUiState.Loading -> LoadingContent()
            is PhoneDetailsUiState.Error -> ErrorContent(messageRes = state.messageRes)
            is PhoneDetailsUiState.Success -> {
                val phone = state.phone
                PhoneScreen(
                    phone = phone,
                    isFavorited = phone.id in favoritePhoneIds(),
                    onFavoriteClick = { onToggleFavorite(phone.id) },
                    galleryState = galleryState,
                    onNavigateToReviews = {
                        navController.navigate(Screen.ReviewThread(phoneId = phone.id))
                    },
                    // Ignores a second tap, which would open another copy of this gallery.
                    // Single-top covers a second tap that lands before this screen leaves resumed.
                    onOpenGallery = dropUnlessResumed {
                        navController.navigate(
                            Screen.PhoneGallery(phoneId = phone.id, phoneModel = phone.deviceInfo.model)
                        ) {
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
    composable<Screen.PhoneGallery> { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.PhoneGallery>()
        val galleryViewModel: PhoneGalleryViewModel = viewModel()
        val galleryState by galleryViewModel.uiState.collectAsState()

        LaunchedEffect(route.phoneId) {
            galleryViewModel.loadPhotos(route.phoneId)
        }

        PhoneGalleryScreen(
            phoneModel = route.phoneModel,
            uiState = galleryState,
            // Ignores repeated taps, which would otherwise pop past the previous screen
            onBackClick = dropUnlessResumed { navController.popBackStack() },
        )
    }
}
