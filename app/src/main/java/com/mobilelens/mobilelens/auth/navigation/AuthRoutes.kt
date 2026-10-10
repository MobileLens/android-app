package com.mobilelens.mobilelens.auth.navigation

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
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
        val loginError by authViewModel.loginError.collectAsState()

        LaunchedEffect(currentUser) {
            if (currentUser != null) navController.navigateToUserSettings()
        }
        // AuthViewModel outlives this screen, so drop the error rather than show it on the next visit
        DisposableEffect(Unit) {
            onDispose { authViewModel.clearLoginError() }
        }
        LoginScreen(
            onLogin = { email, password ->
                authViewModel.login(email, password)
            },
            onNavigateToRegister = {
                navController.navigate(Screen.Register)
            },
            errorMessage = loginError?.let { stringResource(it) },
            onClearError = { authViewModel.clearLoginError() }
        )
    }
    composable<Screen.Register> {
        val currentUser by authViewModel.currentUser.collectAsState()
        val registerError by authViewModel.registerError.collectAsState()

        LaunchedEffect(currentUser) {
            if (currentUser != null) navController.navigateToUserSettings()
        }
        DisposableEffect(Unit) {
            onDispose { authViewModel.clearRegisterError() }
        }
        RegisterScreen(
            onRegister = { username, email, password ->
                authViewModel.register(username, email, password)
            },
            onNavigateToLogin = {
                navController.navigate(Screen.Login)
            },
            errorMessage = registerError?.let { stringResource(it) },
            onClearError = { authViewModel.clearRegisterError() }
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
                profileError = profileError?.let { stringResource(it) },
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
