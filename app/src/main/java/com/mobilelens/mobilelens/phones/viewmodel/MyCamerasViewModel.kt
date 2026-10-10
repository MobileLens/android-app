package com.mobilelens.mobilelens.phones.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.MyCamerasRepository
import com.mobilelens.mobilelens.phones.model.SubmittedCamera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "MyCamerasViewModel"

sealed interface MyCamerasUiState {
    object Loading : MyCamerasUiState
    data class Success(val cameras: List<SubmittedCamera>) : MyCamerasUiState
    data class Error(@StringRes val messageRes: Int) : MyCamerasUiState
}

/** The signed-in user's submitted cameras, whatever their status, as listed on the account screen. */
class MyCamerasViewModel(
    private val repository: MyCamerasRepository = MyCamerasRepository()
) : ViewModel() {
    private val _cameras = MutableStateFlow<MyCamerasUiState>(MyCamerasUiState.Loading)
    val cameras: StateFlow<MyCamerasUiState> = _cameras.asStateFlow()

    /** Loads the list, or refreshes it in place when it's already showing. */
    fun loadCameras() {
        viewModelScope.launch {
            if (_cameras.value !is MyCamerasUiState.Success) {
                _cameras.value = MyCamerasUiState.Loading
            }
            try {
                _cameras.value = MyCamerasUiState.Success(repository.getMyCameras())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load the user's cameras", e)
                // A refresh that fails keeps the list it already has
                if (_cameras.value !is MyCamerasUiState.Success) {
                    _cameras.value = MyCamerasUiState.Error(R.string.error_load_my_cameras)
                }
            }
        }
    }
}
