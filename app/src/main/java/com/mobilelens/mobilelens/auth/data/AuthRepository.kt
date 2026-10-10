package com.mobilelens.mobilelens.auth.data

import com.mobilelens.mobilelens.auth.data.remote.AccountApi
import com.mobilelens.mobilelens.auth.data.remote.AuthApi
import com.mobilelens.mobilelens.auth.data.remote.dtos.ChangeEmailRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.ChangePasswordRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.LoginRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.RegisterRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.SessionResponse
import com.mobilelens.mobilelens.auth.data.remote.dtos.UpdateUserRequest
import com.mobilelens.mobilelens.auth.model.User
import com.mobilelens.mobilelens.core.data.remote.ApiClient
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/** Registration worked, but the backend wants the e-mail confirmed before it issues a session. */
class EmailVerificationRequiredException : Exception()

class AuthRepository(
    private val authApi: AuthApi = ApiClient.createService(),
    private val accountApi: AccountApi = ApiClient.createService()
) {
    suspend fun login(email: String, password: String): User {
        val response = authApi.signInWithEmail(LoginRequest(email = email, password = password))
        response.token?.let { ApiClient.authToken = it }
        val dto = response.user
        return User(
            id = dto?.id ?: "",
            username = dto?.username ?: dto?.name ?: email.substringBefore("@").ifBlank { "username" },
            email = dto?.email ?: email,
            role = dto?.role ?: "Reviewer"
        )
    }

    suspend fun register(username: String, email: String, password: String): User {
        val response = authApi.signUpWithEmail(
            RegisterRequest(
                email = email,
                password = password,
                name = username.ifBlank { "Username" },
                username = username.ifBlank { "username" }
            )
        )
        // No token means no session: the backend is waiting for the e-mail to be confirmed
        val token = response.token ?: throw EmailVerificationRequiredException()
        ApiClient.authToken = token
        val dto = response.user
        return User(
            id = dto?.id ?: "",
            username = dto?.username ?: dto?.name ?: username.ifBlank { "username" },
            email = dto?.email ?: email,
            role = dto?.role ?: "Reviewer"
        )
    }

    suspend fun getSession(): User? {
        if (ApiClient.authToken == null) return null
        val dto = parseSession(authApi.getSession().string())?.user
        if (dto == null) {
            // Token is present but the session is gone; drop the stored bearer
            ApiClient.authToken = null
            return null
        }
        return User(
            id = dto.id,
            username = dto.username ?: dto.name ?: "username",
            email = dto.email,
            role = dto.role ?: "Reviewer"
        )
    }

    // A token without a session (revoked, expired, or issued by another server) gets the literal `null`
    private fun parseSession(body: String): SessionResponse? {
        if (body.isBlank()) return null
        val element = ApiClient.json.parseToJsonElement(body)
        if (element !is JsonObject) return null
        return ApiClient.json.decodeFromJsonElement<SessionResponse>(element)
    }

    suspend fun updateUserProfile(username: String? = null) {
        authApi.updateUser(UpdateUserRequest(username = username, name = username))
    }

    suspend fun changeEmail(newEmail: String) {
        authApi.changeEmail(ChangeEmailRequest(newEmail = newEmail))
    }

    /** Signs out every other device. This one stays signed in on the new session the backend returns. */
    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val response = authApi.changePassword(
            ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        )
        response.token?.let { ApiClient.authToken = it }
    }

    suspend fun deleteAccount() {
        accountApi.deleteAccount()
        // The backend has already ended every session, so there is nothing to sign out of
        ApiClient.authToken = null
    }

    suspend fun logout() {
        val token = ApiClient.authToken ?: return
        ApiClient.authToken = null
        try {
            authApi.signOut(authHeader = "Bearer $token")
        } catch (_: Exception) {}
    }
}
