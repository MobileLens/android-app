package com.mobilelens.mobilelens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.data.BuildInfoRepository
import com.mobilelens.mobilelens.data.CameraHardwareRepository
import com.mobilelens.mobilelens.model.DeviceInfo
import com.mobilelens.mobilelens.model.Lens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CameraUiState {
    object Checking : CameraUiState

    data class Success(
        val lenses: List<Lens>,
        val deviceInfo: DeviceInfo
    ) : CameraUiState
    data class Fallback(val message: String) : CameraUiState
}

class CameraViewModel(
    private val cameraRepository: CameraHardwareRepository,
    private val buildInfoRepository: BuildInfoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Checking)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        loadAllCameras()
    }

    fun loadAllCameras() {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = CameraUiState.Checking

            val cameraList = cameraRepository.getAllCamerasSpecs()
            val deviceInfo = buildInfoRepository.getDeviceInfo()

            if (cameraList.isNotEmpty()) {
                _uiState.value = CameraUiState.Success(cameraList, deviceInfo)
            } else {
                _uiState.value = CameraUiState.Fallback("No cameras :(")
            }
        }
    }
}