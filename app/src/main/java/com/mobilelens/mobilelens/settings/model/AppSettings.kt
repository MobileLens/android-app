package com.mobilelens.mobilelens.settings.model

/** Whether the app is light or dark, or follows the system. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Preferences from the Settings screen. The language is kept by `AppLanguages`, not here. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Material You: colors taken from the wallpaper instead of the app's own
    val dynamicColor: Boolean = true,
)

/** Whether the app should be dark, given whether the system is. */
fun ThemeMode.isDark(systemIsDark: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemIsDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
