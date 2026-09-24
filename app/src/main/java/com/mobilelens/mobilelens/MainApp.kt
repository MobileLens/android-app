package com.mobilelens.mobilelens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mobilelens.mobilelens.auth.ui.screens.LoginScreen
import com.mobilelens.mobilelens.auth.ui.screens.RegisterScreen
import com.mobilelens.mobilelens.auth.ui.screens.UserSettingsScreen
import com.mobilelens.mobilelens.auth.viewmodel.AuthViewModel
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.core.navigation.TOP_LEVEL_ROUTES
import com.mobilelens.mobilelens.core.ui.BottomNavigationBar
import com.mobilelens.mobilelens.core.ui.SearchAppBar
import com.mobilelens.mobilelens.phones.data.displayName
import com.mobilelens.mobilelens.phones.ui.screens.CatalogueScreen
import com.mobilelens.mobilelens.phones.ui.screens.FavoritesScreen
import com.mobilelens.mobilelens.phones.ui.screens.HomeScreen
import com.mobilelens.mobilelens.phones.ui.screens.PhoneScreen
import com.mobilelens.mobilelens.phones.viewmodel.CameraViewModel
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueUiState
import com.mobilelens.mobilelens.phones.viewmodel.CatalogueViewModel
import com.mobilelens.mobilelens.reviews.ui.screens.ReviewScreen
import com.mobilelens.mobilelens.reviews.ui.screens.ReviewThreadScreen
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewDetailsUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewThreadUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewViewModel

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
    val favoritePhones by catalogueViewModel.favoritePhones.collectAsState()
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
                        textFieldState.setTextAndPlaceCursorAtEnd(phone.displayName)
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
            composable<Screen.Home> {
                HomeScreen(
                    cameraViewModel = cameraViewModel,
                    onNavigateToReviews = {
                        navController.navigate(Screen.ReviewThread(phoneId = "1"))
                    }
                )
            }
            composable<Screen.Login> {
                LoginScreen(
                    onLogin = { email, password ->
                        authViewModel.login(email, password)
                        navController.navigate(Screen.UserSettings) {
                            popUpTo(Screen.Home) { saveState = false }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register)
                    }
                )
            }
            composable<Screen.Register> {
                RegisterScreen(
                    onRegister = { username, email, password ->
                        authViewModel.register(username, email, password)
                        navController.navigate(Screen.UserSettings) {
                            popUpTo(Screen.Home) { saveState = false }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login)
                    }
                )
            }
            composable<Screen.UserSettings> {
                val user = currentUser
                if (user != null) {
                    val userReviews by authViewModel.userReviews.collectAsState()
                    UserSettingsScreen(
                        user = user,
                        userReviews = userReviews,
                        onBackClick = { navController.popBackStack() },
                        onUpdateUsername = { newName -> authViewModel.updateUsername(newName) },
                        onUpdateEmail = { newEmail -> authViewModel.updateEmail(newEmail) },
                        onDeleteAccount = {
                            authViewModel.deleteAccount()
                            navController.navigate(Screen.Login) {
                                popUpTo(Screen.Home)
                            }
                        },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login) {
                                popUpTo(Screen.Home)
                            }
                        },
                        onDeleteReview = { reviewId -> authViewModel.deleteUserReview(reviewId) }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login) {
                            popUpTo(Screen.Home)
                        }
                    }
                }
            }
            composable<Screen.ReviewThread> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.ReviewThread>()
                val reviewViewModel: ReviewViewModel = viewModel()
                val threadState by reviewViewModel.thread.collectAsState()

                LaunchedEffect(route.phoneId) {
                    reviewViewModel.loadReviewsForPhone(route.phoneId)
                }

                when (val state = threadState) {
                    is ReviewThreadUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ReviewThreadUiState.Success -> {
                        ReviewThreadScreen(
                            thread = state.thread,
                            onReviewClick = { review ->
                                navController.navigate(Screen.ReviewDetails(reviewId = review.id))
                            }
                        )
                    }
                    is ReviewThreadUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Error: ${state.message}", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
            composable<Screen.ReviewDetails> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.ReviewDetails>()
                val reviewViewModel: ReviewViewModel = viewModel()
                val selectedReviewState by reviewViewModel.selectedReview.collectAsState()

                LaunchedEffect(route.reviewId) {
                    reviewViewModel.loadReview(route.reviewId)
                }

                when (val state = selectedReviewState) {
                    is ReviewDetailsUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ReviewDetailsUiState.Success -> {
                        ReviewScreen(review = state.review)
                    }
                    is ReviewDetailsUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Error: ${state.message}", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
            composable<Screen.Favorites> {
                FavoritesScreen(
                    favoritePhones = favoritePhones,
                    onPhoneClick = { phone ->
                        navController.navigate(Screen.PhoneDetails(phone.id))
                    }
                )
            }
            composable<Screen.Catalogue> {
                val displayResults = if (catalogueState is CatalogueUiState.Success) {
                    (catalogueState as CatalogueUiState.Success).phones
                } else {
                    emptyList()
                }

                if (catalogueState is CatalogueUiState.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (catalogueState is CatalogueUiState.Error) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: ${(catalogueState as CatalogueUiState.Error).message}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    CatalogueScreen(
                        phones = displayResults,
                        selectedPhoneId = selectedPhoneId,
                        onPhoneClick = { phone ->
                            navController.navigate(Screen.PhoneDetails(phone.id))
                        }
                    )
                }
            }
            composable<Screen.PhoneDetails> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.PhoneDetails>()
                val selectedPhone by catalogueViewModel.selectedPhone.collectAsState()

                LaunchedEffect(route.phoneId) {
                    catalogueViewModel.loadPhoneDetails(route.phoneId)
                }

                if (selectedPhone != null && selectedPhone?.id == route.phoneId) {
                    val phone = selectedPhone!!
                    val isFavorited = favoritePhoneIds.contains(phone.id)
                    PhoneScreen(
                        phone = phone,
                        isFavorited = isFavorited,
                        onFavoriteClick = {
                            favoritePhoneIds = if (isFavorited) {
                                favoritePhoneIds - phone.id
                            } else {
                                favoritePhoneIds + phone.id
                            }
                        },
                        onNavigateToReviews = {
                            navController.navigate(Screen.ReviewThread(phoneId = phone.id))
                        }
                    )
                }
            }
        }
    }
}
