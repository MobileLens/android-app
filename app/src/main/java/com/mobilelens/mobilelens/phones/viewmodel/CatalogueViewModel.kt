package com.mobilelens.mobilelens.phones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.phones.data.PhoneCatalogueRepository
import com.mobilelens.mobilelens.phones.model.Phone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 300L

sealed interface CatalogueUiState {
    object Loading : CatalogueUiState
    // isRefreshing: a newer search is loading while these (older) results stay on screen
    data class Success(val phones: List<Phone>, val isRefreshing: Boolean = false) : CatalogueUiState
    data class Error(val message: String) : CatalogueUiState
}

class CatalogueViewModel : ViewModel() {
    private val repository = PhoneCatalogueRepository()

    private val _catalogueState = MutableStateFlow<CatalogueUiState>(CatalogueUiState.Loading)
    val catalogueState: StateFlow<CatalogueUiState> = _catalogueState.asStateFlow()

    private val _favoritePhones = MutableStateFlow<List<Phone>>(emptyList())
    val favoritePhones: StateFlow<List<Phone>> = _favoritePhones.asStateFlow()

    private var searchJob: Job? = null

    fun searchPhones(query: String) {
        // Only the newest query may update the state, so drop any search still in flight
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Wait for typing to pause; the next keystroke cancels this job during the delay
            delay(SEARCH_DEBOUNCE_MS)
            // Keep the current results visible instead of swapping them for a spinner
            val current = _catalogueState.value
            _catalogueState.value = if (current is CatalogueUiState.Success) {
                current.copy(isRefreshing = true)
            } else {
                CatalogueUiState.Loading
            }
            try {
                // if query is empty we should probably pass null to fetch all
                val apiQuery = if (query.isBlank()) null else query
                repository.loadPhones(apiQuery)
                _catalogueState.value = CatalogueUiState.Success(repository.phones.value)
            } catch (e: CancellationException) {
                // Superseded by a newer search, don't report it as an error
                throw e
            } catch (e: Exception) {
                _catalogueState.value = CatalogueUiState.Error(e.message ?: "Failed to load phones")
            }
        }
    }

    fun loadFavorites(favoriteIds: List<String>) {
        viewModelScope.launch {
            try {
                val favs = favoriteIds.mapNotNull {
                    try { repository.getPhoneById(it) } catch(e: Exception) { null }
                }
                _favoritePhones.value = favs
            } catch (_: Exception) {
                _favoritePhones.value = emptyList()
            }
        }
    }
}
