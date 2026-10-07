package com.mobilelens.mobilelens.auth.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mobilelens.mobilelens.auth.ui.screens.LoginScreen
import com.mobilelens.mobilelens.auth.ui.screens.RegisterScreen
import com.mobilelens.mobilelens.auth.ui.screens.UserSettingsScreen
import com.mobilelens.mobilelens.auth.viewmodel.AuthViewModel
import com.mobilelens.mobilelens.core.navigation.Screen

/**
 * Login, Register and UserSettings. Navigation between them follows `currentUser`: Login/Register
 * move on once a user is set, and UserSettings falls back to Login once it's cleared.
 */
fun NavGraphBuilder.authRoutes(
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    composable<Screen.Login> {
        val currentUser by authViewModel.currentUser.collectAsState()

        LaunchedEffect(currentUser) {
            if (currentUser != null) navController.navigateToUserSettings()
        }
        LoginScreen(
            onLogin = { email, password ->
                authViewModel.login(email, password)
            },
            onNavigateToRegister = {
                navController.navigate(Screen.Register)
            }
        )
    }
    composable<Screen.Register> {
        val currentUser by authViewModel.currentUser.collectAsState()

        LaunchedEffect(currentUser) {
            if (currentUser != null) navController.navigateToUserSettings()
        }
        RegisterScreen(
            onRegister = { username, email, password ->
                authViewModel.register(username, email, password)
            },
            onNavigateToLogin = {
                navController.navigate(Screen.Login)
            }
        )
    }
    composable<Screen.UserSettings> {
        val currentUser by authViewModel.currentUser.collectAsState()
        val user = currentUser

        if (user != null) {
            val userReviews by authViewModel.userReviews.collectAsState()
            val profileError by authViewModel.profileError.collectAsState()
            UserSettingsScreen(
                user = user,
                userReviews = userReviews,
                onBackClick = { navController.popBackStack() },
                onUpdateUsername = { newName -> authViewModel.updateUsername(newName) },
                onUpdateEmail = { newEmail -> authViewModel.updateEmail(newEmail) },
                onDeleteAccount = {
                    authViewModel.deleteAccount()
                },
                onLogout = {
                    authViewModel.logout()
                },
                onDeleteReview = { reviewId -> authViewModel.deleteUserReview(reviewId) },
                profileError = profileError,
                onClearProfileError = { authViewModel.clearProfileError() }
            )
        } else {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.Login) {
                    popUpTo(Screen.Home)
                }
            }
        }
    }
}

private fun NavController.navigateToUserSettings() {
    navigate(Screen.UserSettings) {
        popUpTo(Screen.Home) { saveState = false }
    }
}
