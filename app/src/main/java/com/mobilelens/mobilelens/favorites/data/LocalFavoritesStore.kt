package com.mobilelens.mobilelens.favorites.data

import android.content.Context
import androidx.core.content.edit
import com.mobilelens.mobilelens.phones.model.DeviceInfo
import com.mobilelens.mobilelens.phones.model.Phone
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val PREFS_NAME = "local_favorites"
private const val KEY_ENTRIES = "entries"

/**
 * Guest favorites: phone snapshots in SharedPreferences so the Favorites tab works offline
 * without N× detail fetches. Cleared after a successful merge into the signed-in account.
 */
class LocalFavoritesStore private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun getAll(): List<Phone> {
        val raw = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<LocalFavoriteEntry>>(raw).map { it.toPhone() }
        }.getOrDefault(emptyList())
    }

    fun add(phone: Phone) {
        val next = listOf(LocalFavoriteEntry.from(phone)) +
            getAll().filterNot { it.id == phone.id }.map(LocalFavoriteEntry::from)
        persist(next)
    }

    fun remove(smartphoneId: String) {
        persist(getAll().filterNot { it.id == smartphoneId }.map(LocalFavoriteEntry::from))
    }

    fun clear() {
        prefs.edit { remove(KEY_ENTRIES) }
    }

    private fun persist(entries: List<LocalFavoriteEntry>) {
        prefs.edit {
            if (entries.isEmpty()) remove(KEY_ENTRIES)
            else putString(KEY_ENTRIES, json.encodeToString(entries))
        }
    }

    companion object {
        @Volatile
        private var instance: LocalFavoritesStore? = null

        fun getInstance(context: Context): LocalFavoritesStore {
            return instance ?: synchronized(this) {
                instance ?: LocalFavoritesStore(context).also { instance = it }
            }
        }
    }
}

@Serializable
private data class LocalFavoriteEntry(
    val id: String,
    val brand: String = "",
    val model: String = "",
    val imageUrl: String? = null,
) {
    fun toPhone(): Phone = Phone(
        id = id,
        deviceInfo = DeviceInfo(
            brand = brand,
            model = model,
            imageURL = imageUrl,
            releaseDate = null,
        ),
        lenses = emptyList(),
    )

    companion object {
        fun from(phone: Phone) = LocalFavoriteEntry(
            id = phone.id,
            brand = phone.deviceInfo.brand,
            model = phone.deviceInfo.model,
            imageUrl = phone.deviceInfo.imageURL,
        )
    }
}
