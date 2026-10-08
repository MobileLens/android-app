package com.mobilelens.mobilelens.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.mobilelens.mobilelens.R
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable object Home : Screen
    @Serializable object Favorites : Screen
    @Serializable object Catalogue : Screen
    @Serializable data class ReviewThread(val phoneId: String) : Screen
    @Serializable data class ReviewDetails(val reviewId: String) : Screen
    /** Review editor; a null [phoneId] reviews this device. */
    @Serializable data class WriteReview(val phoneId: String? = null) : Screen
    @Serializable data class PhoneDetails(val phoneId: String) : Screen
    @Serializable object Login : Screen
    @Serializable object Register : Screen
    @Serializable object UserSettings : Screen
}


data class TopLevelRoute<T : Any>(
    @StringRes val labelRes: Int,
    val route: T,
    val icon: ImageVector
)

val TOP_LEVEL_ROUTES = listOf(
    TopLevelRoute(R.string.nav_home, Screen.Home, Icons.Default.Home),
    TopLevelRoute(R.string.nav_favorites, Screen.Favorites, Icons.Default.Favorite),
    TopLevelRoute(R.string.nav_catalogue, Screen.Catalogue, Icons.AutoMirrored.Filled.List)
)

/** Login, Register and UserSettings, opened by the account button on top of the current tab. */
fun NavDestination.isAccountScreen(): Boolean =
    hasRoute<Screen.Login>() || hasRoute<Screen.Register>() || hasRoute<Screen.UserSettings>()
