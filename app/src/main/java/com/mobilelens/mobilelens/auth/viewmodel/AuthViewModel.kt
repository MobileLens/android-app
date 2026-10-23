package com.mobilelens.mobilelens.auth.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.auth.data.AuthRepository
import com.mobilelens.mobilelens.auth.data.EmailVerificationRequiredException
import com.mobilelens.mobilelens.auth.data.isStrongPassword
import com.mobilelens.mobilelens.auth.model.User
import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.core.data.remote.apiErrorCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "AuthViewModel"

// Wait between attempts to restore the session, doubling after each failure up to the maximum
private const val RESTORE_RETRY_INITIAL_MS = 2_000L
private const val RESTORE_RETRY_MAX_MS = 60_000L

/** Progress of a change to the account that one of the account screen's dialogs is saving. */
sealed interface AccountEditState {
    object Idle : AccountEditState
    object Saving : AccountEditState
    data class Failed(@StringRes val messageRes: Int) : AccountEditState
    /** Done; [messageRes] confirms it once the dialog has closed. */
    data class Saved(@StringRes val messageRes: Int) : AccountEditState
}

/** Progress of a password reset requested from the login screen. */
sealed interface PasswordResetState {
    object Idle : PasswordResetState
    object Sending : PasswordResetState
    data class Failed(@StringRes val messageRes: Int) : PasswordResetState
    /** The backend took the request; a link is on its way if the address has an account. */
    object Sent : PasswordResetState
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _registerError = MutableStateFlow<Int?>(null)
    // String resource of the last failed registration, shown on the register screen
    val registerError: StateFlow<Int?> = _registerError.asStateFlow()

    private val _loginError = MutableStateFlow<Int?>(null)
    // String resource of the last failed login, shown on the login screen
    val loginError: StateFlow<Int?> = _loginError.asStateFlow()

    private val _accountEdit = MutableStateFlow<AccountEditState>(AccountEditState.Idle)
    val accountEdit: StateFlow<AccountEditState> = _accountEdit.asStateFlow()

    private val _passwordReset = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val passwordReset: StateFlow<PasswordResetState> = _passwordReset.asStateFlow()

    val isLoggedIn: Boolean
        get() = _currentUser.value != null

    // Cancelled when the user logs in meanwhile, so a session restored late can't replace the new one
    private var restoreJob: Job? = null

    init {
        restoreSession()
    }

    /**
     * Reloads the user from a persisted bearer token, or clears it when the session is dead.
     * Offline the token may still be good, so it keeps trying until the backend answers.
     */
    private fun restoreSession() {
        if (ApiClient.authToken == null) return
        restoreJob = viewModelScope.launch {
            var retryDelayMs = RESTORE_RETRY_INITIAL_MS
            while (true) {
                try {
                    _currentUser.value = authRepository.getSession()
                    return@launch
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to restore session", e)
                    if (e is HttpException && e.code() == 401) {
                        ApiClient.authToken = null
                        _currentUser.value = null
                    }
                    if (!isTransientSessionError(e)) return@launch
                }
                delay(retryDelayMs)
                retryDelayMs = (retryDelayMs * 2).coerceAtMost(RESTORE_RETRY_MAX_MS)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            try {
                val user = authRepository.login(email, password)
                restoreJob?.cancel()
                _currentUser.value = user
            } catch (e: Exception) {
                Log.w(TAG, "Login failed", e)
                _loginError.value = loginErrorFor(e)
            }
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun clearRegisterError() {
        _registerError.value = null
    }

    fun register(username: String, email: String, password: String) {
        if (!isStrongPassword(password)) {
            _registerError.value = R.string.auth_error_password_weak
            return
        }
        viewModelScope.launch {
            _registerError.value = null
            try {
                val user = authRepository.register(username, email, password)
                restoreJob?.cancel()
                _currentUser.value = user
            } catch (e: Exception) {
                Log.w(TAG, "Registration failed", e)
                _registerError.value = registerErrorFor(e)
            }
        }
    }

    fun requestPasswordReset(email: String) {
        if (_passwordReset.value == PasswordResetState.Sending) return
        _passwordReset.value = PasswordResetState.Sending
        viewModelScope.launch {
            _passwordReset.value = try {
                authRepository.requestPasswordReset(email)
                PasswordResetState.Sent
            } catch (e: Exception) {
                Log.w(TAG, "Password reset request failed", e)
                PasswordResetState.Failed(passwordResetErrorFor(e))
            }
        }
    }

    /** Forgets the last reset request, once its dialog has closed or a field was edited. */
    fun clearPasswordReset() {
        _passwordReset.value = PasswordResetState.Idle
    }

    fun updateUsername(newUsername: String) =
        saveAccountEdit("Username change failed", ::usernameErrorFor) {
            authRepository.updateUserProfile(username = newUsername)
            _currentUser.value = _currentUser.value?.copy(username = newUsername)
            R.string.settings_username_changed
        }

    fun updateEmail(newEmail: String) =
        saveAccountEdit("E-mail change failed", ::emailErrorFor) {
            authRepository.changeEmail(newEmail)
            // The backend switches the address only once the link it e-mails is opened, and quietly
            // does nothing when another account has it, so check whether it changed right away
            val reloaded = runCatching { authRepository.getSession() }.getOrNull()
            if (reloaded != null) {
                _currentUser.value = reloaded
            }
            if (reloaded?.email.equals(newEmail, ignoreCase = true)) {
                R.string.settings_email_changed
            } else {
                R.string.settings_email_confirmation_sent
            }
        }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (!isStrongPassword(newPassword)) {
            _accountEdit.value = AccountEditState.Failed(R.string.auth_error_password_weak)
            return
        }
        saveAccountEdit("Password change failed", ::passwordChangeErrorFor) {
            authRepository.changePassword(currentPassword, newPassword)
            R.string.settings_password_changed
        }
    }

    fun deleteAccount() =
        saveAccountEdit("Account deletion failed", ::deleteAccountErrorFor) {
            authRepository.deleteAccount()
            _currentUser.value = null
            R.string.settings_account_deleted
        }

    /** Forgets the last result, once it's been shown or its dialog has closed. */
    fun clearAccountEdit() {
        _accountEdit.value = AccountEditState.Idle
    }

    // Runs one account change at a time; [save] returns the message confirming it
    private fun saveAccountEdit(
        failureLog: String,
        errorFor: (Exception) -> Int,
        save: suspend () -> Int,
    ) {
        if (_accountEdit.value == AccountEditState.Saving) return
        _accountEdit.value = AccountEditState.Saving
        viewModelScope.launch {
            _accountEdit.value = try {
                AccountEditState.Saved(save())
            } catch (e: Exception) {
                Log.w(TAG, failureLog, e)
                AccountEditState.Failed(errorFor(e))
            }
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
}

/**
 * Whether restoring the session failed for a reason that may pass (no connection, a timeout, the
 * server failing), so the stored token may still be good and the attempt is worth repeating.
 */
internal fun isTransientSessionError(e: Exception): Boolean = when (e) {
    is IOException -> true
    is HttpException -> e.code() >= 500 || e.code() == 408 || e.code() == 429
    else -> false
}

// The backend answers 401 for a wrong e-mail or password, 400 for a malformed e-mail and 403 for a
// banned account (USER_BANNED) or an e-mail that still has to be confirmed (EMAIL_NOT_VERIFIED)
@StringRes
private fun loginErrorFor(e: Exception): Int = when {
    e is HttpException && e.apiErrorCode == "USER_BANNED" -> R.string.auth_error_banned
    e is HttpException && e.apiErrorCode == "EMAIL_NOT_VERIFIED" -> R.string.auth_error_email_not_verified
    e is HttpException && (e.code() == 400 || e.code() == 401) -> R.string.auth_error_invalid_credentials
    e is IOException -> R.string.auth_error_no_connection
    else -> R.string.auth_error_login
}

@StringRes
private fun registerErrorFor(e: Exception): Int = when {
    e is EmailVerificationRequiredException -> R.string.auth_error_register_verify_email
    e is HttpException -> when (e.apiErrorCode) {
        "PASSWORD_TOO_WEAK", "PASSWORD_TOO_SHORT", "PASSWORD_TOO_LONG", "PASSWORD_REQUIRED" ->
            R.string.auth_error_password_weak
        "USER_ALREADY_EXISTS", "USER_ALREADY_EXISTS_USE_ANOTHER_EMAIL" -> R.string.auth_error_register_exists
        "INVALID_EMAIL" -> R.string.auth_error_register_invalid_email
        else -> R.string.auth_error_register
    }
    e is IOException -> R.string.auth_error_no_connection
    else -> R.string.auth_error_register
}

@StringRes
private fun passwordResetErrorFor(e: Exception): Int = when {
    e is HttpException && e.code() == 400 -> R.string.auth_error_register_invalid_email
    e is HttpException && e.code() == 429 -> R.string.auth_error_reset_too_many
    e is IOException -> R.string.auth_error_no_connection
    else -> R.string.auth_error_reset
}

@StringRes
private fun usernameErrorFor(e: Exception): Int = when (e) {
    is IOException -> R.string.auth_error_no_connection
    // Usernames are unique, and a taken one fails as a server error
    else -> R.string.settings_error_change_username
}

@StringRes
private fun emailErrorFor(e: Exception): Int = when {
    // The dialog doesn't send the current address, so a 400 means a malformed one
    e is HttpException && e.code() == 400 -> R.string.auth_error_register_invalid_email
    e is IOException -> R.string.auth_error_no_connection
    else -> R.string.settings_error_change_email
}

@StringRes
private fun passwordChangeErrorFor(e: Exception): Int = when {
    e is HttpException -> when (e.apiErrorCode) {
        "INVALID_PASSWORD" -> R.string.settings_error_wrong_password
        "PASSWORD_TOO_WEAK", "PASSWORD_TOO_SHORT", "PASSWORD_TOO_LONG", "PASSWORD_REQUIRED" ->
            R.string.auth_error_password_weak
        else -> R.string.settings_error_change_password
    }
    e is IOException -> R.string.auth_error_no_connection
    else -> R.string.settings_error_change_password
}

@StringRes
private fun deleteAccountErrorFor(e: Exception): Int = when (e) {
    is IOException -> R.string.auth_error_no_connection
    else -> R.string.settings_error_delete_account
}
