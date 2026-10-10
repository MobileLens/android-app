package com.mobilelens.mobilelens.settings.data

import android.content.Context
import androidx.core.content.edit
import com.mobilelens.mobilelens.settings.model.AppSettings
import com.mobilelens.mobilelens.settings.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val PREFS_NAME = "app_settings"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_DYNAMIC_COLOR = "dynamic_color"
private const val KEY_LANGUAGE = "language"

/**
 * App preferences in SharedPreferences. [settings] is shared by every reader, so MainActivity's
 * theme follows a change made on the Settings screen straight away.
 */
class AppSettingsStore private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    /** Language tag chosen in the app before Android 13, which has no per-app language of its own. */
    var languageTag: String?
        get() = prefs.getString(KEY_LANGUAGE, null)
        set(value) = prefs.edit {
            if (value == null) remove(KEY_LANGUAGE) else putString(KEY_LANGUAGE, value)
        }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        _settings.update { it.copy(themeMode = mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, enabled) }
        _settings.update { it.copy(dynamicColor = enabled) }
    }

    private fun read(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            themeMode = prefs.getString(KEY_THEME_MODE, null)
                ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                ?: defaults.themeMode,
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, defaults.dynamicColor),
        )
    }

    companion object {
        @Volatile
        private var instance: AppSettingsStore? = null

        fun getInstance(context: Context): AppSettingsStore {
            return instance ?: synchronized(this) {
                instance ?: AppSettingsStore(context).also { instance = it }
            }
        }
    }
}
