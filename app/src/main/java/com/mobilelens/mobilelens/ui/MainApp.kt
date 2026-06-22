package com.mobilelens.mobilelens.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobilelens.mobilelens.navigation.Screen
import com.mobilelens.mobilelens.navigation.TOP_LEVEL_ROUTES
import com.mobilelens.mobilelens.ui.components.BottomNavigationBar
import com.mobilelens.mobilelens.ui.screens.CatalogueScreen
import com.mobilelens.mobilelens.ui.screens.FavoritesScreen
import com.mobilelens.mobilelens.ui.screens.HomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentTitle = TOP_LEVEL_ROUTES.find { route ->
        currentDestination?.hierarchy?.any { it.hasRoute(route.route::class) } == true
    }?.name ?: "MobileLens"

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(currentTitle) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Home> {
                HomeScreen()
            }
            composable<Screen.Favorites> {
                FavoritesScreen()
            }
            composable<Screen.Catalogue> {
                CatalogueScreen()
            }
        }
    }
}
