package com.mobilelens.mobilelens.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mobilelens.mobilelens.settings.data.AppLanguages
import com.mobilelens.mobilelens.settings.data.AppSettingsStore
import com.mobilelens.mobilelens.settings.model.AppSettings
import com.mobilelens.mobilelens.settings.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val store = AppSettingsStore.getInstance(application)

    val settings: StateFlow<AppSettings> = store.settings

    private val _language = MutableStateFlow(AppLanguages.current(application))
    // Chosen language tag; null follows the system
    val language: StateFlow<String?> = _language.asStateFlow()

    fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)

    fun setDynamicColor(enabled: Boolean) = store.setDynamicColor(enabled)

    fun setLanguage(tag: String?) {
        AppLanguages.set(getApplication(), tag)
        _language.value = tag
    }

    /** Reads the language again, since on Android 13+ it can also be changed from the system's settings. */
    fun loadLanguage() {
        _language.value = AppLanguages.current(getApplication())
    }
}
