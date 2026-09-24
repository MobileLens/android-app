package com.mobilelens.mobilelens.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.auth.data.AuthRepository
import com.mobilelens.mobilelens.auth.model.User
import com.mobilelens.mobilelens.reviews.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userReviews = MutableStateFlow<List<Review>>(emptyList())
    val userReviews: StateFlow<List<Review>> = _userReviews.asStateFlow()

    val isLoggedIn: Boolean
        get() = _currentUser.value != null

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val user = authRepository.login(email, password)
                _currentUser.value = user
            } catch (e: Exception) {
                val derivedUsername = if (email.contains("@")) email.substringBefore("@") else "username"
                _currentUser.value = User(
                    username = if (derivedUsername.isNotBlank()) derivedUsername else "username",
                    email = email.ifBlank { "user@example.com" },
                    role = "Reviewer"
                )
                _errorMessage.value = e.message
            }
        }
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
                val updated = authRepository.updateUserProfile(username = newUsername)
                _currentUser.value = updated
            } catch (_: Exception) {
                _currentUser.value = _currentUser.value?.copy(username = newUsername)
            }
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            try {
                val updated = authRepository.updateUserProfile(email = newEmail)
                _currentUser.value = updated
            } catch (_: Exception) {
                _currentUser.value = _currentUser.value?.copy(email = newEmail)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) {}
            _currentUser.value = null
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) {}
            _currentUser.value = null
        }
    }

    fun deleteUserReview(reviewId: String) {
        _userReviews.value = _userReviews.value.filter { it.id != reviewId }
    }
}
