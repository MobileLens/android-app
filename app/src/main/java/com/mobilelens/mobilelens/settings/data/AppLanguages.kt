package com.mobilelens.mobilelens.settings.data

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList

/**
 * The app's own language, independent of the system's.
 *
 * Android 13+ keeps the choice itself (LocaleManager), so it can also be changed from the system's
 * app settings, and recreates the activities when it changes. Before that the choice is stored in
 * [AppSettingsStore] and MainActivity applies it through [configurationOverride] when it's created.
 */
object AppLanguages {
    /**
     * Language tags the app is translated into, English (the unqualified resources) first.
     * Keep in sync with `res/xml/locales_config.xml`.
     */
    val supported: List<String> = listOf("en", "pl")

    /** Whether the system applies a change to the running activities, or the caller has to recreate them. */
    val isAppliedBySystem: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /** The chosen language tag, or null when the app follows the system language. */
    fun current(context: Context): String? =
        if (isAppliedBySystem) {
            context.getSystemService(LocaleManager::class.java).applicationLocales
                .takeUnless { it.isEmpty }
                ?.get(0)
                ?.toLanguageTag()
        } else {
            AppSettingsStore.getInstance(context).languageTag
        }

    /** Switches the app to [tag], or back to the system language when null. */
    fun set(context: Context, tag: String?) {
        if (isAppliedBySystem) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                if (tag == null) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
        } else {
            AppSettingsStore.getInstance(context).languageTag = tag
        }
    }

    /**
     * Configuration that puts an activity in the chosen language before Android 13, for
     * `applyOverrideConfiguration`. Null when the system takes care of it.
     */
    fun configurationOverride(context: Context): Configuration? {
        if (isAppliedBySystem) return null
        val tag = AppSettingsStore.getInstance(context).languageTag ?: return null
        // A blank Configuration only overrides the fields that are set on it
        return Configuration().apply { setLocales(LocaleList.forLanguageTags(tag)) }
    }
}
