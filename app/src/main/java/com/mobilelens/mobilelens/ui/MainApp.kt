package com.mobilelens.mobilelens.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobilelens.mobilelens.navigation.Screen
import com.mobilelens.mobilelens.ui.components.BottomNavigationBar
import com.mobilelens.mobilelens.ui.screens.CatalogueScreen
import com.mobilelens.mobilelens.ui.screens.FavoritesScreen
import com.mobilelens.mobilelens.ui.screens.HomeScreen

@Composable
fun MainApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
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
