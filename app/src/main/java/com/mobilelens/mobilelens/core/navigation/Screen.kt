package com.mobilelens.mobilelens.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.mobilelens.mobilelens.R
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable object Home : Screen
    @Serializable object Favorites : Screen
    @Serializable object Catalogue : Screen
    @Serializable data class ReviewThread(val phoneId: String) : Screen
    /** [scrollToComments] opens the review already scrolled down to its comments. */
    @Serializable data class ReviewDetails(
        val reviewId: String,
        val scrollToComments: Boolean = false,
    ) : Screen
    /** Review editor; a null [phoneId] reviews this device. */
    @Serializable data class WriteReview(val phoneId: String? = null) : Screen
    /** Search for the catalogue phone this device is, when the review editor couldn't match it. */
    @Serializable object ReviewPhonePicker : Screen
    @Serializable data class PhoneDetails(val phoneId: String) : Screen
    /** All photos of a phone. [phoneModel] is only for the title, so the phone isn't fetched again. */
    @Serializable data class PhoneGallery(val phoneId: String, val phoneModel: String) : Screen
    /** Submit this device's Camera2 lenses (and optional full-res photos) to the catalogue phone. */
    @Serializable object UploadDeviceCameras : Screen
    /** Search for the phone to compare with [firstPhoneId]; a null id is this device's own cameras. */
    @Serializable data class ComparePicker(val firstPhoneId: String? = null) : Screen
    /** Two phones side by side; a null [firstPhoneId] is this device's own cameras. */
    @Serializable data class ComparePhones(val firstPhoneId: String?, val secondPhoneId: String) : Screen
    /**
     * [returnToPrevious]: opened by an action that needs an account (e.g. liking a review), so
     * logging in goes back to that screen instead of on to the account screen.
     */
    @Serializable data class Login(val returnToPrevious: Boolean = false) : Screen
    /** Opened from [Login], whose [Login.returnToPrevious] it carries on. */
    @Serializable data class Register(val returnToPrevious: Boolean = false) : Screen
    @Serializable object UserSettings : Screen
    /** The app's own preferences (theme, language), opened from the account menu. */
    @Serializable object AppSettings : Screen
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

/** Login, Register, UserSettings and AppSettings, opened from the account menu on top of the current tab. */
fun NavDestination.isAccountScreen(): Boolean =
    hasRoute<Screen.Login>() || hasRoute<Screen.Register>() || hasRoute<Screen.UserSettings>() ||
            hasRoute<Screen.AppSettings>()

/** The bottom bar's tabs. */
fun NavDestination.isTopLevel(): Boolean = TOP_LEVEL_ROUTES.any { hasRoute(it.route::class) }

/**
 * Shows the tab at [route]. If it's already on the back stack (the user is on it, or on a screen
 * opened from it) everything above it is dropped, so the tab shows its root again. Otherwise the
 * tab is switched to, restoring its saved state.
 */
fun NavController.navigateToTopLevel(route: Screen) {
    if (popBackStack(route, inclusive = false)) return
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/** Full-screen tasks that rise over the screen that opened them and close back down to it. */
fun NavDestination.isSheet(): Boolean =
    hasRoute<Screen.WriteReview>() || hasRoute<Screen.UploadDeviceCameras>() ||
            hasRoute<Screen.ComparePicker>() || hasRoute<Screen.ReviewPhonePicker>()
