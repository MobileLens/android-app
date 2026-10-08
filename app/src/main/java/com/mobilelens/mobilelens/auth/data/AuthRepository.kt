package com.mobilelens.mobilelens.auth.data

import com.mobilelens.mobilelens.auth.data.remote.AuthApi
import com.mobilelens.mobilelens.auth.data.remote.dtos.ChangeEmailRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.ChangePasswordRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.LoginRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.RegisterRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.UpdateUserRequest
import com.mobilelens.mobilelens.auth.model.User
import com.mobilelens.mobilelens.core.data.remote.ApiClient

class AuthRepository(
    private val authApi: AuthApi = ApiClient.createService()
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
        response.token?.let { ApiClient.authToken = it }
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
        val response = authApi.getSession()
        val dto = response.user
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

    suspend fun updateUserProfile(username: String? = null) {
        authApi.updateUser(UpdateUserRequest(username = username, name = username))
    }

    suspend fun changeEmail(newEmail: String) {
        authApi.changeEmail(ChangeEmailRequest(newEmail = newEmail))
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        authApi.changePassword(
            ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        )
    }

    suspend fun logout() {
        val token = ApiClient.authToken ?: return
        ApiClient.authToken = null
        try {
            authApi.signOut(authHeader = "Bearer $token")
        } catch (_: Exception) {}
    }
}
