package com.mobilelens.mobilelens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobilelens.mobilelens.auth.navigation.authRoutes
import com.mobilelens.mobilelens.auth.viewmodel.AuthViewModel
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.core.navigation.TOP_LEVEL_ROUTES
import com.mobilelens.mobilelens.core.navigation.isAccountScreen
import com.mobilelens.mobilelens.core.navigation.rememberNavTransitions
import com.mobilelens.mobilelens.core.ui.BottomNavigationBar
import com.mobilelens.mobilelens.core.ui.SearchAppBar
import com.mobilelens.mobilelens.core.ui.theme.Motion
import com.mobilelens.mobilelens.favorites.viewmodel.FavoritesViewModel
import com.mobilelens.mobilelens.phones.navigation.phonesRoutes
import com.mobilelens.mobilelens.phones.viewmodel.CameraViewModel
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueUiState
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueViewModel
import com.mobilelens.mobilelens.reviews.navigation.reviewsRoutes
import com.mobilelens.mobilelens.settings.navigation.settingsRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    cameraViewModel: CameraViewModel,
    catalogueViewModel: CatalogueViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val textFieldState = rememberTextFieldState()
    val query = textFieldState.text.toString()

    val catalogueState by catalogueViewModel.catalogueState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()
    val favoritePhones by favoritesViewModel.favoritePhones.collectAsState()

    var selectedPhoneId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(query) {
        catalogueViewModel.searchPhones(query)
    }

    LaunchedEffect(currentUser?.id) {
        if (currentUser != null) {
            favoritesViewModel.onSignedIn()
        } else {
            favoritesViewModel.onSignedOut()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelRoute = TOP_LEVEL_ROUTES.any { topLevelRoute ->
        currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true
    }

    val isAuthOrSettingsScreen = currentDestination?.isAccountScreen() == true

    // The review editor has its own top bar and needs the room the bottom bar takes
    val isReviewEditor = currentDestination?.hasRoute<Screen.WriteReview>() == true
    // The gallery has its own top bar
    val isGallery = currentDestination?.hasRoute<Screen.PhoneGallery>() == true
    // Device camera upload has its own top bar
    val isUploadDevice = currentDestination?.hasRoute<Screen.UploadDeviceCameras>() == true

    fun navigateToCatalogue() {
        navController.navigate(Screen.Catalogue) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun handleAccountClick() {
        if (currentUser != null) {
            navController.navigate(Screen.UserSettings)
        } else {
            navController.navigate(Screen.Login)
        }
    }

    fun handleSettingsClick() {
        navController.navigate(Screen.AppSettings) { launchSingleTop = true }
    }

    val showTopBar = !isAuthOrSettingsScreen && !isReviewEditor && !isGallery && !isUploadDevice
    val showBottomBar = !isReviewEditor
    val navTransitions = rememberNavTransitions()
    // Scaffold pads the content by the system bars when a bar slot is empty, but by the bar's
    // height otherwise. The slots keep at least the system bars' height while a bar animates away,
    // so the padding follows the bar down to that inset instead of jumping back up to it.
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier.defaultMinSize(minHeight = systemBarsPadding.calculateBottomPadding()),
                contentAlignment = Alignment.BottomStart,
            ) {
                // Slides down out of the way, giving its height back to the content as it goes
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = expandVertically(barAnimationSpec(), expandFrom = Alignment.Top),
                    exit = shrinkVertically(barAnimationSpec(), shrinkTowards = Alignment.Top),
                ) {
                    BottomNavigationBar(navController)
                }
            }
        },
        topBar = {
            Box(modifier = Modifier.defaultMinSize(minHeight = systemBarsPadding.calculateTopPadding())) {
                AnimatedVisibility(
                    visible = showTopBar,
                    enter = expandVertically(barAnimationSpec(), expandFrom = Alignment.Bottom) +
                            fadeIn(tween(Motion.DURATION_MEDIUM)),
                    exit = shrinkVertically(barAnimationSpec(), shrinkTowards = Alignment.Bottom) +
                            fadeOut(tween(Motion.DURATION_SHORT)),
                ) {
                    val displayResults = if (catalogueState is CatalogueUiState.Success) {
                        (catalogueState as CatalogueUiState.Success).phones
                    } else {
                        emptyList()
                    }
                    SearchAppBar(
                        textFieldState = textFieldState,
                        searchResults = displayResults,
                        showBackButton = !isTopLevelRoute && navController.previousBackStackEntry != null,
                        onBackClick = { navController.popBackStack() },
                        onSearch = {
                            selectedPhoneId = null
                            navigateToCatalogue()
                        },
                        onResultSelected = { phone ->
                            // The model name alone identifies the phone and keeps the follow-up search
                            // narrow; the backend would also match the brand.
                            textFieldState.setTextAndPlaceCursorAtEnd(phone.deviceInfo.model)
                            selectedPhoneId = phone.id
                            navController.navigate(Screen.PhoneDetails(phone.id))
                        },
                        onClear = { selectedPhoneId = null },
                        onAccountClick = { handleAccountClick() },
                        onSettingsClick = { handleSettingsClick() },
                        accountInitial = currentUser?.username?.take(1)?.uppercase()
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            // Keeps a sheet sliding up from below from drawing over the bottom bar's area
            modifier = Modifier
                .padding(innerPadding)
                .clipToBounds(),
            enterTransition = navTransitions.enter,
            exitTransition = navTransitions.exit,
            popEnterTransition = navTransitions.popEnter,
            popExitTransition = navTransitions.popExit,
            predictivePopEnterTransition = navTransitions.predictivePopEnter,
            predictivePopExitTransition = navTransitions.predictivePopExit,
        ) {
            phonesRoutes(
                navController = navController,
                cameraViewModel = cameraViewModel,
                catalogueViewModel = catalogueViewModel,
                isLoggedIn = { currentUser != null },
                selectedPhoneId = { selectedPhoneId },
                favoriteIds = { favoriteIds },
                favoritePhones = { favoritePhones },
                onToggleFavorite = { phone -> favoritesViewModel.toggleFavorite(phone) },
            )
            reviewsRoutes(navController, authViewModel)
            authRoutes(navController, authViewModel)
            settingsRoutes(navController)
        }
    }
}

// The bars change size together with the screen transition under them
private fun <T> barAnimationSpec() = tween<T>(Motion.DURATION_MEDIUM, easing = Motion.Emphasized)
