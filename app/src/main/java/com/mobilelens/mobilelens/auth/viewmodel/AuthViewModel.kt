package com.mobilelens.mobilelens.auth.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.auth.data.AuthRepository
import com.mobilelens.mobilelens.auth.model.User
import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.reviews.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "AuthViewModel"

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loginError = MutableStateFlow<Int?>(null)
    // String resource of the last failed login, shown on the login screen
    val loginError: StateFlow<Int?> = _loginError.asStateFlow()

    private val _profileError = MutableStateFlow<Int?>(null)
    // String resource of the last failed settings action, shown on the settings screen
    val profileError: StateFlow<Int?> = _profileError.asStateFlow()

    private val _userReviews = MutableStateFlow<List<Review>>(emptyList())
    val userReviews: StateFlow<List<Review>> = _userReviews.asStateFlow()

    val isLoggedIn: Boolean
        get() = _currentUser.value != null

    init {
        restoreSession()
    }

    /** Reloads the user from a persisted bearer token, or clears it when the session is dead. */
    private fun restoreSession() {
        if (ApiClient.authToken == null) return
        viewModelScope.launch {
            try {
                _currentUser.value = authRepository.getSession()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to restore session", e)
                if (e is HttpException && e.code() == 401) {
                    ApiClient.authToken = null
                    _currentUser.value = null
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            try {
                _currentUser.value = authRepository.login(email, password)
            } catch (e: Exception) {
                Log.w(TAG, "Login failed", e)
                _loginError.value = loginErrorFor(e)
            }
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val user = authRepository.register(username, email, password)
                _currentUser.value = user
            } catch (e: Exception) {
                _currentUser.value = User(
                    username = username.ifBlank { "username" },
                    email = email.ifBlank { "user@example.com" },
                    role = "Reviewer"
                )
                _errorMessage.value = e.message
            }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            try {
                authRepository.updateUserProfile(username = newUsername)
                _currentUser.value = _currentUser.value?.copy(username = newUsername)
            } catch (_: Exception) {
                _currentUser.value = _currentUser.value?.copy(username = newUsername)
            }
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            _profileError.value = null
            try {
                authRepository.changeEmail(newEmail)
                // The server answers success even if the address belongs to another account,
                // so reload the user and check that the e-mail actually changed.
                val reloaded = authRepository.getSession()
                if (reloaded != null) {
                    _currentUser.value = reloaded
                }
                if (!reloaded?.email.equals(newEmail, ignoreCase = true)) {
                    _profileError.value = R.string.settings_error_change_email
                }
            } catch (_: Exception) {
                _profileError.value = R.string.settings_error_change_email
            }
        }
    }

    fun clearProfileError() {
        _profileError.value = null
    }

    fun deleteAccount() {
        _currentUser.value = null
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) {}
        }
    }

    fun logout() {
        _currentUser.value = null
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) {}
        }
    }

    fun deleteUserReview(reviewId: String) {
        _userReviews.value = _userReviews.value.filter { it.id != reviewId }
    }
}

// The backend answers 401 for a wrong e-mail or password and 400 for a malformed e-mail
@StringRes
private fun loginErrorFor(e: Exception): Int = when {
    e is HttpException && (e.code() == 400 || e.code() == 401) -> R.string.auth_error_invalid_credentials
    e is IOException -> R.string.auth_error_no_connection
    else -> R.string.auth_error_login
}
