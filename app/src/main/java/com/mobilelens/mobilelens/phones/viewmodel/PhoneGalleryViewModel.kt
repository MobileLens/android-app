package com.mobilelens.mobilelens.phones.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.PhoneGalleryRepository
import com.mobilelens.mobilelens.phones.model.GalleryPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "PhoneGalleryViewModel"

sealed interface PhoneGalleryUiState {
    object Loading : PhoneGalleryUiState
    data class Success(val photos: List<GalleryPhoto>) : PhoneGalleryUiState
    data class Error(@StringRes val messageRes: Int) : PhoneGalleryUiState
}

// Owned by the phone details back stack entry. The full gallery reuses this instance.
class PhoneGalleryViewModel(
    private val repository: PhoneGalleryRepository = PhoneGalleryRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<PhoneGalleryUiState>(PhoneGalleryUiState.Loading)
    val uiState: StateFlow<PhoneGalleryUiState> = _uiState.asStateFlow()

    private var loadedPhoneId: String? = null
    private var loadingPhoneId: String? = null

    fun loadPhotos(phoneId: String) {
        // Skip when this phone is already shown or a load for it is in flight.
        // The full gallery calls this again on the same ViewModel.
        if (phoneId == loadedPhoneId || phoneId == loadingPhoneId) return

        loadingPhoneId = phoneId
        viewModelScope.launch {
            _uiState.value = PhoneGalleryUiState.Loading
            try {
                _uiState.value = PhoneGalleryUiState.Success(repository.getPhotos(phoneId))
                loadedPhoneId = phoneId
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load photos of phone $phoneId", e)
                _uiState.value = PhoneGalleryUiState.Error(R.string.error_load_photos)
            } finally {
                if (loadingPhoneId == phoneId) loadingPhoneId = null
            }
        }
    }
}
