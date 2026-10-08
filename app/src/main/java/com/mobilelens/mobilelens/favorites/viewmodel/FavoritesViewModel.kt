package com.mobilelens.mobilelens.favorites.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.favorites.data.FavoriteRepository
import com.mobilelens.mobilelens.favorites.data.LocalFavoritesStore
import com.mobilelens.mobilelens.phones.model.Phone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "FavoritesViewModel"

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FavoriteRepository()
    private val localStore = LocalFavoritesStore.getInstance(application)

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _favoritePhones = MutableStateFlow<List<Phone>>(emptyList())
    val favoritePhones: StateFlow<List<Phone>> = _favoritePhones.asStateFlow()

    private val _uiError = MutableStateFlow<Int?>(null)
    // String resource of the last load/toggle failure
    val uiError: StateFlow<Int?> = _uiError.asStateFlow()

    @Volatile
    private var signedIn: Boolean = false

    init {
        applyLocal()
    }

    /** Call when auth session appears: merge guest favorites to the account, then load from API. */
    fun onSignedIn() {
        signedIn = true
        viewModelScope.launch {
            mergeLocalIntoAccount()
            refreshFromServer()
        }
    }

    /** Call when signed out: show locally stored favorites (not the previous account's list). */
    fun onSignedOut() {
        signedIn = false
        applyLocal()
        _uiError.value = null
    }

    fun toggleFavorite(phone: Phone) {
        val currentlyFavorited = phone.id in _favoriteIds.value
        val previousIds = _favoriteIds.value
        val previousPhones = _favoritePhones.value

        if (currentlyFavorited) {
            _favoriteIds.value = previousIds - phone.id
            _favoritePhones.value = previousPhones.filterNot { it.id == phone.id }
        } else {
            _favoriteIds.value = previousIds + phone.id
            _favoritePhones.value = listOf(phone) + previousPhones.filterNot { it.id == phone.id }
        }

        viewModelScope.launch {
            try {
                if (signedIn) {
                    if (currentlyFavorited) {
                        repository.removeFavorite(phone.id)
                    } else {
                        repository.addFavorite(phone.id)
                    }
                } else {
                    if (currentlyFavorited) {
                        localStore.remove(phone.id)
                    } else {
                        localStore.add(phone)
                    }
                }
                _uiError.value = null
            } catch (e: Exception) {
                Log.w(TAG, "Failed to toggle favorite ${phone.id}", e)
                _favoriteIds.value = previousIds
                _favoritePhones.value = previousPhones
                _uiError.value = R.string.error_toggle_favorite
            }
        }
    }

    fun clearError() {
        _uiError.value = null
    }

    private fun applyLocal() {
        val phones = localStore.getAll()
        _favoritePhones.value = phones
        _favoriteIds.value = phones.map { it.id }.toSet()
    }

    private suspend fun refreshFromServer() {
        try {
            val phones = repository.getFavorites()
            _favoritePhones.value = phones
            _favoriteIds.value = phones.map { it.id }.toSet()
            _uiError.value = null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load favorites", e)
            _uiError.value = R.string.error_load_favorites
        }
    }

    private suspend fun mergeLocalIntoAccount() {
        val local = localStore.getAll()
        if (local.isEmpty()) return
        var failed = false
        for (phone in local) {
            try {
                repository.addFavorite(phone.id)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to merge favorite ${phone.id}", e)
                failed = true
            }
        }
        if (!failed) {
            localStore.clear()
        }
    }
}
