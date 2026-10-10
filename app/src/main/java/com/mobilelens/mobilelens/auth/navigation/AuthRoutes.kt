package com.mobilelens.mobilelens.auth.navigation

import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mobilelens.mobilelens.auth.ui.screens.LoginScreen
import com.mobilelens.mobilelens.auth.ui.screens.RegisterScreen
import com.mobilelens.mobilelens.auth.ui.screens.UserSettingsScreen
import com.mobilelens.mobilelens.auth.viewmodel.AccountEditState
import com.mobilelens.mobilelens.auth.viewmodel.AuthViewModel
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.phones.viewmodel.MyCamerasViewModel
import com.mobilelens.mobilelens.reviews.viewmodel.MyReviewsViewModel

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
        val accountEdit by authViewModel.accountEdit.collectAsState()
        val user = currentUser
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }

        val savedMessage = (accountEdit as? AccountEditState.Saved)?.let { stringResource(it.messageRes) }
        LaunchedEffect(savedMessage) {
            if (savedMessage != null) {
                // A deleted account leaves this screen at once, so it gets a toast rather than a snackbar
                if (authViewModel.currentUser.value == null) {
                    Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                } else {
                    snackbarHostState.showSnackbar(savedMessage)
                }
                // Cleared only afterwards: clearing changes this effect's key, which would cancel it
                authViewModel.clearAccountEdit()
            }
        }
        // AuthViewModel outlives this screen, so don't bring a stale result back on the next visit
        DisposableEffect(Unit) {
            onDispose { authViewModel.clearAccountEdit() }
        }

        if (user != null) {
            val myReviewsViewModel: MyReviewsViewModel = viewModel()
            val reviewsState by myReviewsViewModel.reviews.collectAsState()
            val reviewMessageRes by myReviewsViewModel.message.collectAsState()
            val myCamerasViewModel: MyCamerasViewModel = viewModel()
            val camerasState by myCamerasViewModel.cameras.collectAsState()

            // Also runs on the way back from a review, refreshing the list in place
            LaunchedEffect(Unit) {
                myReviewsViewModel.loadReviews()
                myCamerasViewModel.loadCameras()
            }
            val reviewMessage = reviewMessageRes?.let { stringResource(it) }
            LaunchedEffect(reviewMessage) {
                if (reviewMessage != null) {
                    snackbarHostState.showSnackbar(reviewMessage)
                    myReviewsViewModel.messageShown()
                }
            }

            UserSettingsScreen(
                user = user,
                reviewsState = reviewsState,
                camerasState = camerasState,
                editState = accountEdit,
                onBackClick = dropUnlessResumed { navController.popBackStack() },
                onUpdateUsername = authViewModel::updateUsername,
                onUpdateEmail = authViewModel::updateEmail,
                onChangePassword = authViewModel::changePassword,
                onDeleteAccount = authViewModel::deleteAccount,
                onClearEditState = authViewModel::clearAccountEdit,
                onLogout = authViewModel::logout,
                onReviewClick = { reviewId ->
                    navController.navigate(Screen.ReviewDetails(reviewId = reviewId))
                },
                onDeleteReview = myReviewsViewModel::deleteReview,
                onRetryReviews = myReviewsViewModel::loadReviews,
                onCameraClick = { phoneId ->
                    navController.navigate(Screen.PhoneDetails(phoneId = phoneId))
                },
                onRetryCameras = myCamerasViewModel::loadCameras,
                snackbarHostState = snackbarHostState,
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
