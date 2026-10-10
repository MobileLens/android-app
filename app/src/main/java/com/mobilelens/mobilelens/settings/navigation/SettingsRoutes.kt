package com.mobilelens.mobilelens.settings.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mobilelens.mobilelens.BuildConfig
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.settings.data.AppLanguages
import com.mobilelens.mobilelens.settings.ui.screens.AppSettingsScreen
import com.mobilelens.mobilelens.settings.viewmodel.AppSettingsViewModel

/** The app's Settings screen, opened from the account menu. */
fun NavGraphBuilder.settingsRoutes(navController: NavController) {
    composable<Screen.AppSettings> {
        val viewModel: AppSettingsViewModel = viewModel()
        val settings by viewModel.settings.collectAsState()
        val language by viewModel.language.collectAsState()
        val activity = LocalActivity.current

        // Changing the language anywhere recreates the activity, which runs this again
        LaunchedEffect(Unit) { viewModel.loadLanguage() }

        AppSettingsScreen(
            settings = settings,
            language = language,
            languages = AppLanguages.supported,
            version = stringResource(
                R.string.app_settings_version_value,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
            ),
            onBackClick = { navController.popBackStack() },
            onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
            onDynamicColorChange = { enabled -> viewModel.setDynamicColor(enabled) },
            onLanguageChange = { tag ->
                viewModel.setLanguage(tag)
                // MainActivity puts itself in the stored language when it's created
                if (!AppLanguages.isAppliedBySystem) activity?.recreate()
            },
        )
    }
}
