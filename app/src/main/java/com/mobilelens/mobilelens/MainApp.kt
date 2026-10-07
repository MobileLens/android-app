package com.mobilelens.mobilelens

import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
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
import com.mobilelens.mobilelens.core.ui.BottomNavigationBar
import com.mobilelens.mobilelens.core.ui.SearchAppBar
import com.mobilelens.mobilelens.phones.navigation.phonesRoutes
import com.mobilelens.mobilelens.phones.viewmodel.CameraViewModel
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueUiState
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueViewModel
import com.mobilelens.mobilelens.reviews.navigation.reviewsRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    cameraViewModel: CameraViewModel,
    catalogueViewModel: CatalogueViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val textFieldState = rememberTextFieldState()
    val query = textFieldState.text.toString()

    val catalogueState by catalogueViewModel.catalogueState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var selectedPhoneId by rememberSaveable { mutableStateOf<String?>(null) }
    var favoritePhoneIds by rememberSaveable { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(query) {
        catalogueViewModel.searchPhones(query)
    }

    LaunchedEffect(favoritePhoneIds) {
        catalogueViewModel.loadFavorites(favoritePhoneIds)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelRoute = TOP_LEVEL_ROUTES.any { topLevelRoute ->
        currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true
    }

    val isAuthOrSettingsScreen = currentDestination?.hasRoute<Screen.Login>() == true ||
            currentDestination?.hasRoute<Screen.Register>() == true ||
            currentDestination?.hasRoute<Screen.UserSettings>() == true

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

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        topBar = {
            if (!isAuthOrSettingsScreen) {
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
                        // The backend searches only `modelName`, so the brand must not be in the
                        // query, otherwise the follow-up search matches nothing.
                        textFieldState.setTextAndPlaceCursorAtEnd(phone.deviceInfo.model)
                        selectedPhoneId = phone.id
                        navController.navigate(Screen.PhoneDetails(phone.id))
                    },
                    onClear = { selectedPhoneId = null },
                    onAccountClick = { handleAccountClick() },
                    accountInitial = currentUser?.username?.take(1)?.uppercase() ?: "U"
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            phonesRoutes(
                navController = navController,
                cameraViewModel = cameraViewModel,
                catalogueViewModel = catalogueViewModel,
                selectedPhoneId = { selectedPhoneId },
                favoritePhoneIds = { favoritePhoneIds },
                onToggleFavorite = { phoneId ->
                    favoritePhoneIds = if (phoneId in favoritePhoneIds) {
                        favoritePhoneIds - phoneId
                    } else {
                        favoritePhoneIds + phoneId
                    }
                }
            )
            reviewsRoutes(navController)
            authRoutes(navController, authViewModel)
        }
    }
}
