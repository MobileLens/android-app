package com.mobilelens.mobilelens.phones.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.core.navigation.screen
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.LoadingContent
import com.mobilelens.mobilelens.core.ui.UiStateCrossfade
import com.mobilelens.mobilelens.core.ui.performOutcome
import com.mobilelens.mobilelens.core.ui.performReject
import com.mobilelens.mobilelens.phones.data.JPEG_MIME_TYPE
import com.mobilelens.mobilelens.phones.model.CatalogueSort
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.phones.ui.components.CatalogueFilterChips
import com.mobilelens.mobilelens.phones.ui.components.CatalogueSortTabs
import com.mobilelens.mobilelens.phones.ui.screens.CatalogueScreen
import com.mobilelens.mobilelens.phones.ui.screens.ComparePickerScreen
import com.mobilelens.mobilelens.phones.ui.screens.CompareScreen
import com.mobilelens.mobilelens.phones.ui.screens.FavoritesScreen
import com.mobilelens.mobilelens.phones.ui.screens.HomeScreen
import com.mobilelens.mobilelens.phones.ui.screens.PhoneGalleryScreen
import com.mobilelens.mobilelens.phones.ui.screens.PhoneScreen
import com.mobilelens.mobilelens.phones.ui.screens.UploadDeviceScreen
import com.mobilelens.mobilelens.phones.viewmodel.CameraUiState
import com.mobilelens.mobilelens.phones.viewmodel.CameraViewModel
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueUiState
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueViewModel
import com.mobilelens.mobilelens.phones.viewmodel.ComparePickerViewModel
import com.mobilelens.mobilelens.phones.viewmodel.CompareViewModel
import com.mobilelens.mobilelens.phones.viewmodel.PhoneDetailsUiState
import com.mobilelens.mobilelens.phones.viewmodel.PhoneDetailsViewModel
import com.mobilelens.mobilelens.phones.viewmodel.PhoneGalleryViewModel
import com.mobilelens.mobilelens.phones.viewmodel.UploadDeviceViewModel

/**
 * Home, Favorites, Catalogue, PhoneDetails, PhoneGallery, UploadDeviceCameras, ComparePicker and
 * ComparePhones.
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
    screen<Screen.Home>(navController) {
        val context = LocalContext.current
        val haptic = LocalHapticFeedback.current
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
                    haptic.performReject()
                    Toast.makeText(context, camerasUnavailable, Toast.LENGTH_SHORT).show()
                }
            },
            // Compares this device's own lenses, so they have to be read first
            onCompare = {
                val state = cameraViewModel.uiState.value
                if (state is CameraUiState.Success) {
                    navController.navigate(Screen.ComparePicker()) { launchSingleTop = true }
                } else {
                    haptic.performReject()
                    Toast.makeText(context, camerasUnavailable, Toast.LENGTH_SHORT).show()
                }
            },
        )
    }
    screen<Screen.Favorites>(navController) {
        FavoritesScreen(
            favoritePhones = favoritePhones(),
            onPhoneClick = { phone ->
                navController.navigate(Screen.PhoneDetails(phone.id))
            }
        )
    }
    screen<Screen.Catalogue>(navController) {
        val context = LocalContext.current
        val haptic = LocalHapticFeedback.current
        val catalogueState by catalogueViewModel.catalogueState.collectAsState()
        val sort by catalogueViewModel.sort.collectAsState()
        val filters by catalogueViewModel.filters.collectAsState()
        val brands by catalogueViewModel.brands.collectAsState()
        val errorMessageRes by catalogueViewModel.errorMessageRes.collectAsState()

        // A failed pull-to-refresh leaves the old results up, so say why nothing changed
        val errorMessage = errorMessageRes?.let { stringResource(it) }
        LaunchedEffect(errorMessage) {
            if (errorMessage != null) {
                haptic.performReject()
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                catalogueViewModel.errorMessageShown()
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            CatalogueSortTabs(
                selected = sort,
                onSelect = catalogueViewModel::selectSort,
            )
            AnimatedVisibility(visible = sort == CatalogueSort.ALL) {
                CatalogueFilterChips(
                    filters = filters,
                    brands = brands,
                    onFiltersChange = catalogueViewModel::setFilters,
                )
            }
            UiStateCrossfade(state = catalogueState) { state ->
                when (state) {
                    is CatalogueUiState.Loading -> LoadingContent()
                    is CatalogueUiState.Error -> ErrorContent(
                        messageRes = state.messageRes,
                        onRetry = catalogueViewModel::refresh,
                    )
                    is CatalogueUiState.Success -> {
                        CatalogueScreen(
                            phones = state.phones,
                            selectedPhoneId = selectedPhoneId(),
                            onPhoneClick = { phone ->
                                navController.navigate(Screen.PhoneDetails(phone.id))
                            },
                            onRefresh = catalogueViewModel::refresh,
                            isRefreshing = state.isRefreshing,
                            isPullRefreshing = state.isPullRefreshing,
                        )
                    }
                }
            }
        }
    }
    screen<Screen.PhoneDetails>(navController) { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.PhoneDetails>()
        val phoneDetailsViewModel: PhoneDetailsViewModel = viewModel()
        val phoneState by phoneDetailsViewModel.uiState.collectAsState()
        val galleryViewModel: PhoneGalleryViewModel = viewModel()
        val galleryState by galleryViewModel.uiState.collectAsState()

        LaunchedEffect(route.phoneId) {
            phoneDetailsViewModel.loadPhone(route.phoneId)
            galleryViewModel.loadPhotos(route.phoneId)
        }

        UiStateCrossfade(state = phoneState) { state ->
            when (state) {
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
                        onCompare = dropUnlessResumed {
                            navController.navigate(Screen.ComparePicker(firstPhoneId = phone.id)) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
        }
    }
    screen<Screen.PhoneGallery>(navController) { backStackEntry ->
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
    screen<Screen.ComparePicker>(navController) { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.ComparePicker>()
        val pickerViewModel: ComparePickerViewModel = viewModel()
        val uiState by pickerViewModel.uiState.collectAsState()
        val searchState = rememberTextFieldState()
        val query = searchState.text.toString()

        LaunchedEffect(query) {
            pickerViewModel.search(query)
        }

        ComparePickerScreen(
            searchState = searchState,
            uiState = uiState,
            excludedPhoneId = route.firstPhoneId,
            onClose = dropUnlessResumed { navController.popBackStack() },
            onPhoneSelected = { phone ->
                // Ignores a second tap while this sheet is already on its way out
                if (backStackEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    // The comparison takes the picker's place, so going back leaves both
                    navController.navigate(Screen.ComparePhones(route.firstPhoneId, phone.id)) {
                        popUpTo<Screen.ComparePicker> { inclusive = true }
                    }
                }
            },
            onRetry = pickerViewModel::retry,
        )
    }
    screen<Screen.ComparePhones>(navController) { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.ComparePhones>()
        val compareViewModel: CompareViewModel = viewModel()
        val uiState by compareViewModel.uiState.collectAsState()
        val cameraState by cameraViewModel.uiState.collectAsState()

        LaunchedEffect(route, cameraState) {
            // This device's lenses may not be read yet, e.g. when the app was restored on this screen
            if (route.firstPhoneId == null && cameraState == CameraUiState.Checking) return@LaunchedEffect
            compareViewModel.load(
                firstPhoneId = route.firstPhoneId,
                secondPhoneId = route.secondPhoneId,
                thisDevice = cameraState as? CameraUiState.Success,
            )
        }

        CompareScreen(
            uiState = uiState,
            // Ignores repeated taps, which would otherwise pop past the previous screen
            onBackClick = dropUnlessResumed { navController.popBackStack() },
            onRetry = compareViewModel::retry,
            // The picker takes the comparison's place, so choosing a phone there replaces this screen
            onChangePhone = dropUnlessResumed {
                navController.navigate(Screen.ComparePicker(route.firstPhoneId)) {
                    popUpTo<Screen.ComparePhones> { inclusive = true }
                }
            },
        )
    }
    screen<Screen.UploadDeviceCameras>(navController) {
        val context = LocalContext.current
        val uploadViewModel: UploadDeviceViewModel = viewModel()
        val uiState by uploadViewModel.uiState.collectAsState()
        val gallery by uploadViewModel.gallery.collectAsState()
        val isSubmitting by uploadViewModel.isSubmitting.collectAsState()
        val userMessage by uploadViewModel.message.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        // Saved, since the activity can be recreated while the photo picker is open (e.g. a rotation)
        var pickingForCamera by rememberSaveable { mutableStateOf<String?>(null) }

        val cameraState by cameraViewModel.uiState.collectAsState()
        LaunchedEffect(cameraState) {
            val success = cameraState as? CameraUiState.Success ?: return@LaunchedEffect
            uploadViewModel.prepare(success.lenses, success.deviceInfo)
        }

        val pickGalleryPhotos = rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->
            val cameraId = pickingForCamera
            pickingForCamera = null
            if (cameraId != null) {
                uploadViewModel.addGalleryPhotos(cameraId, uris, context.contentResolver)
            }
        }
        val pickPhotoCamera = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                uploadViewModel.readPhotoCamera(uri, context.contentResolver)
            }
        }

        // Sends stay on this screen, so the other tab can be used next
        val haptic = LocalHapticFeedback.current
        val message = userMessage
        val messageText = message?.let { stringResource(it.textRes) }
        LaunchedEffect(message) {
            if (message != null && messageText != null) {
                haptic.performOutcome(message)
                snackbarHostState.showSnackbar(messageText)
                uploadViewModel.messageShown()
            }
        }

        UploadDeviceScreen(
            uiState = uiState,
            gallery = gallery,
            isSubmitting = isSubmitting,
            snackbarHostState = snackbarHostState,
            onClose = dropUnlessResumed { navController.popBackStack() },
            onSendSpecs = uploadViewModel::sendSpecs,
            onSendPhotos = { uploadViewModel.sendPhotos(context.contentResolver) },
            onAddPhotoCamera = {
                pickPhotoCamera.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onConfirmPhotoCamera = uploadViewModel::confirmPhotoCamera,
            onDismissPhotoCamera = uploadViewModel::dismissPhotoCamera,
            onRemovePhotoCamera = uploadViewModel::removePhotoCamera,
            onAddGalleryPhotos = { cameraId ->
                pickingForCamera = cameraId
                // Only JPEGs can be sent, since only their metadata can be removed without re-encoding
                pickGalleryPhotos.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(JPEG_MIME_TYPE))
                )
            },
            onRemoveGalleryPhoto = uploadViewModel::removeGalleryPhoto,
            onRetryGallery = uploadViewModel::loadGallery,
        )
    }
}
