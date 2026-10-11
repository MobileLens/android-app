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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "CompareViewModel"

// This device isn't a catalogue entry, so it has no real id
private const val THIS_DEVICE_PHONE_ID = "this-device"

/** One side of a comparison. [isThisDevice] marks this device's own Camera2 lenses. */
data class ComparedPhone(
    val phone: Phone,
    val isThisDevice: Boolean = false,
)

sealed interface CompareUiState {
    object Loading : CompareUiState
    data class Success(val first: ComparedPhone, val second: ComparedPhone) : CompareUiState
    data class Error(@StringRes val messageRes: Int) : CompareUiState
}

// Scoped to a single ComparePhones back stack entry
class CompareViewModel : ViewModel() {
    private val repository = PhoneCatalogueRepository()

    private val _uiState = MutableStateFlow<CompareUiState>(CompareUiState.Loading)
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    // The last request, so returning to the screen doesn't reload and the error screen can retry
    private var request: CompareRequest? = null
    private var loadJob: Job? = null

    /**
     * Loads two catalogue phones. A null [firstPhoneId] stands for this device, whose lenses come
     * from [thisDevice]; without them there is nothing to compare.
     */
    fun load(firstPhoneId: String?, secondPhoneId: String, thisDevice: CameraUiState.Success?) {
        val request = CompareRequest(
            firstPhoneId = firstPhoneId,
            secondPhoneId = secondPhoneId,
            // Only matters when it is compared, so it can't trigger a reload otherwise
            thisDevice = thisDevice.takeIf { firstPhoneId == null },
        )
        if (request == this.request) return
        this.request = request
        fetch(request)
    }

    fun retry() {
        request?.let(::fetch)
    }

    private fun fetch(request: CompareRequest) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = CompareUiState.Loading
            try {
                _uiState.value = if (request.firstPhoneId != null) {
                    val (first, second) = repository.comparePhones(
                        listOf(request.firstPhoneId, request.secondPhoneId)
                    )
                    CompareUiState.Success(ComparedPhone(first), ComparedPhone(second))
                } else if (request.thisDevice != null) {
                    val thisDevice = Phone(
                        id = THIS_DEVICE_PHONE_ID,
                        deviceInfo = request.thisDevice.deviceInfo,
                        lenses = request.thisDevice.lenses,
                    )
                    CompareUiState.Success(
                        first = ComparedPhone(thisDevice, isThisDevice = true),
                        second = ComparedPhone(repository.getPhoneById(request.secondPhoneId)),
                    )
                } else {
                    CompareUiState.Error(R.string.compare_error_this_device)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val first = request.firstPhoneId ?: "this device"
                Log.w(TAG, "Failed to load phones to compare: $first, ${request.secondPhoneId}", e)
                _uiState.value = CompareUiState.Error(R.string.error_compare_phones)
            }
        }
    }

    private data class CompareRequest(
        val firstPhoneId: String?,
        val secondPhoneId: String,
        val thisDevice: CameraUiState.Success?,
    )
}
