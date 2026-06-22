package com.mobilelens.mobilelens.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable object Home : Screen
    @Serializable object Favorites : Screen
    @Serializable object Catalogue : Screen
}

data class TopLevelRoute<T : Any>(
    val name: String,
    val route: T,
    val icon: ImageVector
)

val TOP_LEVEL_ROUTES = listOf(
    TopLevelRoute("Home", Screen.Home, Icons.Default.Home),
    TopLevelRoute("Favorites", Screen.Favorites, Icons.Default.Favorite),
    TopLevelRoute("Catalogue", Screen.Catalogue, Icons.AutoMirrored.Filled.List)
)
