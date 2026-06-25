package com.mobilelens.mobilelens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.data.CameraHardwareRepository
import com.mobilelens.mobilelens.data.Lens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CameraUiState {
    object Checking : CameraUiState

    data class Success(val lenses: List<Lens>) : CameraUiState
    data class Fallback(val message: String) : CameraUiState
}

class CameraViewModel(private val repository: CameraHardwareRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Checking)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        loadAllCameras()
    }

    fun loadAllCameras() {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = CameraUiState.Checking

            val cameraList = repository.getAllCamerasSpecs()

            if (cameraList.isNotEmpty()) {
                _uiState.value = CameraUiState.Success(cameraList)
            } else {
                _uiState.value = CameraUiState.Fallback("No cameras :(")
            }
        }
    }
}