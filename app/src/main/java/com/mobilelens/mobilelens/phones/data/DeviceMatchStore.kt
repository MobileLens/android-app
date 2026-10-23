package com.mobilelens.mobilelens.phones.data

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "device_match"
private const val KEY_MODEL = "model"
private const val KEY_PHONE_ID = "phone_id"

/**
 * The catalogue phone the user picked for this device, for when Build.MODEL (an OEM code such as
 * "SM-S928B") doesn't match any catalogue model name. Kept with the model it was picked on, so a
 * backup restored onto another device doesn't carry it over.
 */
class DeviceMatchStore private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** The phone picked for [model], if there is one. */
    fun getPhoneId(model: String): String? =
        prefs.getString(KEY_PHONE_ID, null)
            ?.takeIf { prefs.getString(KEY_MODEL, null) == model }

    fun setPhoneId(model: String, phoneId: String) {
        prefs.edit {
            putString(KEY_MODEL, model)
            putString(KEY_PHONE_ID, phoneId)
        }
    }

    fun clear() {
        prefs.edit {
            remove(KEY_MODEL)
            remove(KEY_PHONE_ID)
        }
    }

    companion object {
        @Volatile
        private var instance: DeviceMatchStore? = null

        fun getInstance(context: Context): DeviceMatchStore {
            return instance ?: synchronized(this) {
                instance ?: DeviceMatchStore(context).also { instance = it }
            }
        }
    }
}
