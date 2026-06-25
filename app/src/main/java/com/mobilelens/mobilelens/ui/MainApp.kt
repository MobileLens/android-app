package com.mobilelens.mobilelens.ui

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
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.mobilelens.mobilelens.data.PhoneCatalogue
import com.mobilelens.mobilelens.data.displayName
import com.mobilelens.mobilelens.data.filterByQuery
import com.mobilelens.mobilelens.navigation.Screen
import com.mobilelens.mobilelens.ui.components.BottomNavigationBar
import com.mobilelens.mobilelens.ui.components.SearchAppBar
import com.mobilelens.mobilelens.ui.screens.CatalogueScreen
import com.mobilelens.mobilelens.ui.screens.FavoritesScreen
import com.mobilelens.mobilelens.ui.screens.HomeScreen
import com.mobilelens.mobilelens.viewmodel.CameraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(cameraViewModel: CameraViewModel) {
    val navController = rememberNavController()
    val textFieldState = rememberTextFieldState()
    val query = textFieldState.text.toString()
    val filteredPhones = remember(query) { PhoneCatalogue.filterByQuery(query) }
    var selectedPhoneId by rememberSaveable { mutableStateOf<Int?>(null) }

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
                onSearch = {
                    selectedPhoneId = null
                    navigateToCatalogue()
                },
                onResultSelected = { phone ->
                    textFieldState.setTextAndPlaceCursorAtEnd(phone.displayName)
                    selectedPhoneId = phone.id
                    navigateToCatalogue()
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
                HomeScreen(cameraViewModel = cameraViewModel)
            }
            composable<Screen.Favorites> {
                FavoritesScreen()
            }
            composable<Screen.Catalogue> {
                CatalogueScreen(
                    phones = filteredPhones,
                    selectedPhoneId = selectedPhoneId,
                )
            }
        }
    }
}
