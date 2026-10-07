package com.mobilelens.mobilelens.phones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.phones.data.PhoneCatalogueRepository
import com.mobilelens.mobilelens.phones.model.Phone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PhoneDetailsUiState {
    object Loading : PhoneDetailsUiState
    data class Success(val phone: Phone) : PhoneDetailsUiState
    data class Error(val message: String) : PhoneDetailsUiState
}

// Scoped to a single PhoneDetails back stack entry, so each opened phone keeps its own state
class PhoneDetailsViewModel : ViewModel() {
    private val repository = PhoneCatalogueRepository()

    private val _uiState = MutableStateFlow<PhoneDetailsUiState>(PhoneDetailsUiState.Loading)
    val uiState: StateFlow<PhoneDetailsUiState> = _uiState.asStateFlow()

    fun loadPhone(id: String) {
        // Skip reloading when returning to this screen from further down the back stack
        val current = _uiState.value
        if (current is PhoneDetailsUiState.Success && current.phone.id == id) return

        viewModelScope.launch {
            _uiState.value = PhoneDetailsUiState.Loading
            try {
                _uiState.value = PhoneDetailsUiState.Success(repository.getPhoneById(id))
            } catch (e: Exception) {
                _uiState.value = PhoneDetailsUiState.Error(e.message ?: "Failed to load phone.")
            }
        }
    }
}
