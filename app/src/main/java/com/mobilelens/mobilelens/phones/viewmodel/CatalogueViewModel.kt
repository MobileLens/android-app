package com.mobilelens.mobilelens.phones.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.PhoneCatalogueRepository
import com.mobilelens.mobilelens.phones.model.Brand
import com.mobilelens.mobilelens.phones.model.CatalogueFilters
import com.mobilelens.mobilelens.phones.model.CatalogueSort
import com.mobilelens.mobilelens.phones.model.Phone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "CatalogueViewModel"
private const val SEARCH_DEBOUNCE_MS = 300L

sealed interface CatalogueUiState {
    object Loading : CatalogueUiState
    // isRefreshing: a newer search is loading while these (older) results stay on screen.
    // isPullRefreshing: the same for a pull-to-refresh, which has its own indicator.
    data class Success(
        val phones: List<Phone>,
        val isRefreshing: Boolean = false,
        val isPullRefreshing: Boolean = false,
    ) : CatalogueUiState
    data class Error(@StringRes val messageRes: Int) : CatalogueUiState
}

class CatalogueViewModel : ViewModel() {
    private val repository = PhoneCatalogueRepository()

    private val _catalogueState = MutableStateFlow<CatalogueUiState>(CatalogueUiState.Loading)
    val catalogueState: StateFlow<CatalogueUiState> = _catalogueState.asStateFlow()

    private val _sort = MutableStateFlow(CatalogueSort.ALL)
    val sort: StateFlow<CatalogueSort> = _sort.asStateFlow()

    // Filters are only offered (and applied) on the All tab
    private val _filters = MutableStateFlow(CatalogueFilters())
    val filters: StateFlow<CatalogueFilters> = _filters.asStateFlow()

    // Empty until loaded; the brand filter is left out meanwhile
    private val _brands = MutableStateFlow<List<Brand>>(emptyList())
    val brands: StateFlow<List<Brand>> = _brands.asStateFlow()

    // A failed refresh keeps the results on screen, so it is reported once instead (a toast);
    // cleared with errorMessageShown()
    private val _errorMessageRes = MutableStateFlow<Int?>(null)
    val errorMessageRes: StateFlow<Int?> = _errorMessageRes.asStateFlow()

    // Refreshing, switching sort and filtering reload with the query typed so far
    private var query = ""
    private var loadJob: Job? = null

    init {
        loadBrands()
    }

    fun searchPhones(query: String) {
        this.query = query
        load(debounce = true)
    }

    fun selectSort(sort: CatalogueSort) {
        if (sort == _sort.value) return
        _sort.value = sort
        // The old results belong to another ordering, so don't leave them on screen
        load(replaceResults = true)
    }

    fun setFilters(filters: CatalogueFilters) {
        if (filters == _filters.value) return
        _filters.value = filters
        // Same ordering as before, so like a search the old results stay up until the new ones land
        if (_sort.value == CatalogueSort.ALL) load()
    }

    /** Reloads the catalogue for the current search and sort. Also serves as the error screen's retry. */
    fun refresh() {
        if (_brands.value.isEmpty()) loadBrands()
        load(userRefresh = true)
    }

    fun errorMessageShown() {
        _errorMessageRes.value = null
    }

    private fun loadBrands() {
        viewModelScope.launch {
            try {
                _brands.value = repository.getBrands().sortedBy { it.name.lowercase() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load brands", e)
            }
        }
    }

    private fun load(
        debounce: Boolean = false,
        userRefresh: Boolean = false,
        replaceResults: Boolean = false,
    ) {
        // Only the newest request may update the state, so drop any load still in flight
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // Wait for typing to pause; the next keystroke cancels this job during the delay
            if (debounce) delay(SEARCH_DEBOUNCE_MS)
            // Keep the current results visible instead of swapping them for a spinner
            val current = _catalogueState.value
            val keptResults = current as? CatalogueUiState.Success
            _catalogueState.value = if (keptResults != null && !replaceResults) {
                keptResults.copy(isRefreshing = !userRefresh, isPullRefreshing = userRefresh)
            } else {
                CatalogueUiState.Loading
            }
            try {
                // if query is empty we should probably pass null to fetch all
                val apiQuery = if (query.isBlank()) null else query
                val sort = _sort.value
                // Filters belong to the All tab, so New and Trending always show everything
                val filters = if (sort == CatalogueSort.ALL) _filters.value else CatalogueFilters()
                repository.loadPhones(apiQuery, sort, filters)
                _catalogueState.value = CatalogueUiState.Success(repository.phones.value)
            } catch (e: CancellationException) {
                // Superseded by a newer load, don't report it as an error
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load phones", e)
                if (userRefresh && keptResults != null) {
                    _catalogueState.value = keptResults.copy(isRefreshing = false, isPullRefreshing = false)
                    _errorMessageRes.value = R.string.error_load_phones
                } else {
                    _catalogueState.value = CatalogueUiState.Error(R.string.error_load_phones)
                }
            }
        }
    }
}
