package com.mobilelens.mobilelens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.toRoute
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.data.displayName
import com.mobilelens.mobilelens.phones.data.filterByQuery
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.core.navigation.TOP_LEVEL_ROUTES
import com.mobilelens.mobilelens.core.ui.BottomNavigationBar
import com.mobilelens.mobilelens.core.ui.SearchAppBar
import com.mobilelens.mobilelens.phones.ui.screens.CatalogueScreen
import com.mobilelens.mobilelens.phones.ui.screens.FavoritesScreen
import com.mobilelens.mobilelens.phones.ui.screens.HomeScreen
import com.mobilelens.mobilelens.phones.ui.screens.PhoneScreen
import com.mobilelens.mobilelens.phones.viewmodel.CameraViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobilelens.mobilelens.reviews.ui.screens.ReviewScreen
import com.mobilelens.mobilelens.reviews.ui.screens.ReviewThreadScreen
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewViewModel
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewThreadUiState
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewDetailsUiState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(cameraViewModel: CameraViewModel) {
    val navController = rememberNavController()
    val textFieldState = rememberTextFieldState()
    val query = textFieldState.text.toString()
    val filteredPhones = remember(query) { PhoneCatalogue.filterByQuery(query) }
    var selectedPhoneId by rememberSaveable { mutableStateOf<Int?>(null) }
    var favoritePhoneIds by rememberSaveable { mutableStateOf(emptyList<Int>()) }


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelRoute = TOP_LEVEL_ROUTES.any { topLevelRoute ->
        currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true
    }

    fun navigateToCatalogue() {
        navController.navigate(Screen.Catalogue) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(

        bottomBar = {
            BottomNavigationBar(navController)
        },
        topBar = {
            SearchAppBar(
                textFieldState = textFieldState,
                searchResults = filteredPhones,
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
            )
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
                        // Assuming phoneId 1 for HomeScreen for now
                        navController.navigate(Screen.ReviewThread(phoneId = 1))
                    }
                )
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
                val favoritePhones = remember(favoritePhoneIds) {
                    PhoneCatalogue.filter { favoritePhoneIds.contains(it.id) }
                }
                FavoritesScreen(
                    favoritePhones = favoritePhones,
                    onPhoneClick = { phone ->
                        navController.navigate(Screen.PhoneDetails(phone.id))
                    }
                )
            }
            composable<Screen.Catalogue> {
                CatalogueScreen(
                    phones = filteredPhones,
                    selectedPhoneId = selectedPhoneId,
                    onPhoneClick = { phone ->
                        navController.navigate(Screen.PhoneDetails(phone.id))
                    }
                )
            }
            composable<Screen.PhoneDetails> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.PhoneDetails>()
                val phone = remember(route.phoneId) {
                    PhoneCatalogue.firstOrNull { it.id == route.phoneId }
                }
                if (phone != null) {
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
                        }
                    )
                }
            }
        }
    }
}
