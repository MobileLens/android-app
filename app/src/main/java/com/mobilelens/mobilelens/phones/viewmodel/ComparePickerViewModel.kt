package com.mobilelens.mobilelens.phones.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.PhoneCatalogueRepository
import com.mobilelens.mobilelens.phones.model.Phone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ComparePickerViewModel"
private const val SEARCH_DEBOUNCE_MS = 300L

sealed interface ComparePickerUiState {
    object Loading : ComparePickerUiState
    // isRefreshing: results for a newer query are loading while these stay on screen
    data class Success(
        val phones: List<Phone>,
        val isRefreshing: Boolean = false,
    ) : ComparePickerUiState
    data class Error(@StringRes val messageRes: Int) : ComparePickerUiState
}

// Scoped to a single ComparePicker or ReviewPhonePicker back stack entry
class ComparePickerViewModel : ViewModel() {
    private val repository = PhoneCatalogueRepository()

    private val _uiState = MutableStateFlow<ComparePickerUiState>(ComparePickerUiState.Loading)
    val uiState: StateFlow<ComparePickerUiState> = _uiState.asStateFlow()

    // Null until the first search, which lists every phone straight away
    private var query: String? = null
    private var searchJob: Job? = null

    fun search(query: String) {
        val trimmed = query.trim()
        // Returning to the screen searches for the same text again
        if (trimmed == this.query) return
        val debounce = this.query != null
        this.query = trimmed
        load(debounce)
    }

    /** Searches again for the current text, for the error screen. */
    fun retry() {
        load(debounce = false)
    }

    private fun load(debounce: Boolean) {
        // Only the newest search may update the state
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Wait for typing to pause; the next keystroke cancels this job during the delay
            if (debounce) delay(SEARCH_DEBOUNCE_MS)
            val kept = _uiState.value as? ComparePickerUiState.Success
            _uiState.value = kept?.copy(isRefreshing = true) ?: ComparePickerUiState.Loading
            try {
                val phones = repository.searchPhones(query?.ifBlank { null })
                _uiState.value = ComparePickerUiState.Success(phones)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to search phones", e)
                _uiState.value = ComparePickerUiState.Error(R.string.error_load_phones)
            }
        }
    }
}
