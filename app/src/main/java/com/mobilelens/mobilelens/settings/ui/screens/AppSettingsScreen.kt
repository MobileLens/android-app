package com.mobilelens.mobilelens.settings.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.settings.model.AppSettings
import com.mobilelens.mobilelens.settings.model.ThemeMode
import com.mobilelens.mobilelens.settings.ui.components.LanguageDialog
import com.mobilelens.mobilelens.settings.ui.components.SettingsSectionHeader
import com.mobilelens.mobilelens.settings.ui.components.languageName

/**
 * App preferences: theme, Material You colors and language. Has its own top bar, so MainApp hides
 * the search bar here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    settings: AppSettings,
    // Chosen language tag; null follows the system
    language: String?,
    languages: List<String>,
    version: String,
    onBackClick: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onLanguageChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    // The screen's background shows through, as on the other screens
    val itemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LargeTopAppBar(
            title = { Text(stringResource(R.string.app_settings_title)) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                    )
                }
            },
            // Insets are handled by the parent Scaffold
            windowInsets = WindowInsets(0, 0, 0, 0),
            scrollBehavior = scrollBehavior,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(text = stringResource(R.string.app_settings_appearance))

            ListItem(
                headlineContent = { Text(stringResource(R.string.app_settings_theme)) },
                supportingContent = {
                    ThemeModeSelector(
                        selected = settings.themeMode,
                        onSelect = onThemeModeChange,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                },
                leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                colors = itemColors,
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.app_settings_dynamic_color)) },
                supportingContent = { Text(stringResource(R.string.app_settings_dynamic_color_supporting)) },
                leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                // The whole row toggles, so the switch itself takes no clicks
                trailingContent = { Switch(checked = settings.dynamicColor, onCheckedChange = null) },
                modifier = Modifier.toggleable(
                    value = settings.dynamicColor,
                    role = Role.Switch,
                    onValueChange = onDynamicColorChange,
                ),
                colors = itemColors,
            )

            SettingsSectionHeader(text = stringResource(R.string.app_settings_general))

            ListItem(
                headlineContent = { Text(stringResource(R.string.app_settings_language)) },
                supportingContent = {
                    Text(language?.let(::languageName) ?: stringResource(R.string.app_settings_language_system))
                },
                leadingContent = { Icon(Icons.Outlined.Language, contentDescription = null) },
                modifier = Modifier.clickable(role = Role.Button) { showLanguageDialog = true },
                colors = itemColors,
            )

            SettingsSectionHeader(text = stringResource(R.string.app_settings_about))

            ListItem(
                headlineContent = { Text(stringResource(R.string.app_settings_version)) },
                supportingContent = { Text(version) },
                leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                colors = itemColors,
            )
        }
    }

    if (showLanguageDialog) {
        LanguageDialog(
            languages = languages,
            selected = language,
            onSelect = { tag ->
                showLanguageDialog = false
                if (tag != language) onLanguageChange(tag)
            },
            onDismiss = { showLanguageDialog = false },
        )
    }
}

@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = ThemeMode.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                label = {
                    Text(
                        text = stringResource(mode.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@get:StringRes
private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.app_settings_theme_system
        ThemeMode.LIGHT -> R.string.app_settings_theme_light
        ThemeMode.DARK -> R.string.app_settings_theme_dark
    }

@Preview(showBackground = true)
@Composable
private fun AppSettingsScreenPreview() {
    MaterialTheme {
        AppSettingsScreen(
            settings = AppSettings(themeMode = ThemeMode.DARK, dynamicColor = true),
            language = null,
            languages = listOf("en", "pl"),
            version = "1.0 (1)",
            onBackClick = {},
            onThemeModeChange = {},
            onDynamicColorChange = {},
            onLanguageChange = {},
        )
    }
}
