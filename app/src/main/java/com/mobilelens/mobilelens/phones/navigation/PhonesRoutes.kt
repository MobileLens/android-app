package com.mobilelens.mobilelens.phones.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.LoadingContent
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.phones.ui.screens.CatalogueScreen
import com.mobilelens.mobilelens.phones.ui.screens.FavoritesScreen
import com.mobilelens.mobilelens.phones.ui.screens.HomeScreen
import com.mobilelens.mobilelens.phones.ui.screens.PhoneGalleryScreen
import com.mobilelens.mobilelens.phones.ui.screens.PhoneScreen
import com.mobilelens.mobilelens.phones.ui.screens.UploadDeviceScreen
import com.mobilelens.mobilelens.phones.viewmodel.CameraUiState
import com.mobilelens.mobilelens.phones.viewmodel.CameraViewModel
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueUiState
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueViewModel
import com.mobilelens.mobilelens.phones.viewmodel.PhoneDetailsUiState
import com.mobilelens.mobilelens.phones.viewmodel.PhoneDetailsViewModel
import com.mobilelens.mobilelens.phones.viewmodel.PhoneGalleryViewModel
import com.mobilelens.mobilelens.phones.viewmodel.UploadDeviceUiState
import com.mobilelens.mobilelens.phones.viewmodel.UploadDeviceViewModel
import com.mobilelens.mobilelens.phones.viewmodel.UploadSubmitState

/**
 * Home, Favorites, Catalogue, PhoneDetails, PhoneGallery and UploadDeviceCameras.
 *
 * State owned by MainApp is passed as getters, so it's read when a route composes rather than
 * when the graph is built.
 */
fun NavGraphBuilder.phonesRoutes(
    navController: NavController,
    cameraViewModel: CameraViewModel,
    catalogueViewModel: CatalogueViewModel,
    isLoggedIn: () -> Boolean,
    selectedPhoneId: () -> String?,
    favoriteIds: () -> Set<String>,
    favoritePhones: () -> List<Phone>,
    onToggleFavorite: (Phone) -> Unit,
) {
    composable<Screen.Home> {
        val context = LocalContext.current
        val camerasUnavailable = stringResource(R.string.upload_cameras_unavailable)
        HomeScreen(
            cameraViewModel = cameraViewModel,
            isLoggedIn = isLoggedIn(),
            onWriteReview = { navController.navigate(Screen.WriteReview()) },
            onUpload = {
                val state = cameraViewModel.uiState.value
                if (state is CameraUiState.Success) {
                    navController.navigate(Screen.UploadDeviceCameras)
                } else {
                    Toast.makeText(context, camerasUnavailable, Toast.LENGTH_SHORT).show()
                }
            },
        )
    }
    composable<Screen.Favorites> {
        FavoritesScreen(
            favoritePhones = favoritePhones(),
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
                    isFavorited = phone.id in favoriteIds(),
                    onFavoriteClick = { onToggleFavorite(phone) },
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
        // The phone screen already loaded this gallery. Reuse its ViewModel so this screen
        // opens on those photos instead of the loading quilt and a second fetch.
        val galleryOwner = remember(backStackEntry) {
            try {
                navController.getBackStackEntry(Screen.PhoneDetails(route.phoneId))
            } catch (_: IllegalArgumentException) {
                backStackEntry
            }
        }
        val galleryViewModel: PhoneGalleryViewModel = viewModel(galleryOwner)
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
    composable<Screen.UploadDeviceCameras> {
        val context = LocalContext.current
        val uploadViewModel: UploadDeviceViewModel = viewModel()
        val uiState by uploadViewModel.uiState.collectAsState()
        val submitState by uploadViewModel.submitState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        var pickingForLens by remember { mutableIntStateOf(-1) }

        val cameraState by cameraViewModel.uiState.collectAsState()
        LaunchedEffect(cameraState) {
            val success = cameraState as? CameraUiState.Success ?: return@LaunchedEffect
            uploadViewModel.prepare(success.lenses, success.deviceInfo)
        }

        val pickImage = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            val index = pickingForLens
            pickingForLens = -1
            if (index >= 0) {
                uploadViewModel.setPhoto(index, uri, context.contentResolver)
            }
        }

        val photoError = (uiState as? UploadDeviceUiState.Ready)
            ?.photoErrorRes
            ?.let { stringResource(it) }
        LaunchedEffect(photoError) {
            if (photoError != null) {
                snackbarHostState.showSnackbar(photoError)
                uploadViewModel.clearPhotoError()
            }
        }

        val submitFailed = (submitState as? UploadSubmitState.Failed)?.messageRes
            ?.let { stringResource(it) }
        LaunchedEffect(submitFailed) {
            if (submitFailed != null) {
                snackbarHostState.showSnackbar(submitFailed)
                uploadViewModel.submitMessageShown()
            }
        }

        val succeededMessage = stringResource(R.string.upload_succeeded)
        LaunchedEffect(submitState) {
            if (submitState == UploadSubmitState.Succeeded) {
                Toast.makeText(context, succeededMessage, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }

        UploadDeviceScreen(
            uiState = uiState,
            submitState = submitState,
            snackbarHostState = snackbarHostState,
            onClose = dropUnlessResumed { navController.popBackStack() },
            onSubmit = { uploadViewModel.submit(context.contentResolver) },
            onAddPhoto = { index ->
                pickingForLens = index
                pickImage.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = { index ->
                uploadViewModel.setPhoto(index, null, context.contentResolver)
            },
        )
    }
}
