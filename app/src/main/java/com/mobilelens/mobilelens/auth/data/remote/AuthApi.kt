package com.mobilelens.mobilelens.auth.data.remote

import com.mobilelens.mobilelens.auth.data.remote.dtos.AuthResponse
import com.mobilelens.mobilelens.auth.data.remote.dtos.ChangePasswordRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.LoginRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.RegisterRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.SessionResponse
import com.mobilelens.mobilelens.auth.data.remote.dtos.UpdateUserRequest
import com.mobilelens.mobilelens.auth.data.remote.dtos.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/sign-in/email")
    suspend fun signInWithEmail(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/auth/sign-up/email")
    suspend fun signUpWithEmail(
        @Body request: RegisterRequest
    ): AuthResponse

    @GET("api/auth/get-session")
    suspend fun getSession(
        @Header("Authorization") authHeader: String? = null
    ): SessionResponse

    @POST("api/auth/sign-out")
    suspend fun signOut(
        @Header("Authorization") authHeader: String? = null
    )

    @POST("api/auth/update-user")
    suspend fun updateUser(
        @Body request: UpdateUserRequest,
        @Header("Authorization") authHeader: String? = null
    ): UserDto

    @POST("api/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest,
        @Header("Authorization") authHeader: String? = null
    )
}
