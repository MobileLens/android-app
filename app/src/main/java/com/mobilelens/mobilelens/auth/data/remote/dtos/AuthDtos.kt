package com.mobilelens.mobilelens.auth.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val username: String? = null
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val username: String? = null,
    val email: String? = null
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val revokeOtherSessions: Boolean = true
)

@Serializable
data class UserDto(
    val id: String = "",
    val email: String = "",
    val name: String? = null,
    val username: String? = null,
    val role: String? = "user"
)

@Serializable
data class SessionDto(
    val id: String = "",
    val userId: String = "",
    val token: String? = null
)

@Serializable
data class AuthResponse(
    val user: UserDto? = null,
    val token: String? = null,
    val session: SessionDto? = null
)

@Serializable
data class SessionResponse(
    val user: UserDto? = null,
    val session: SessionDto? = null
)
