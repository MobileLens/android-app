package com.mobilelens.mobilelens.auth.data.remote

import com.mobilelens.mobilelens.core.data.remote.dtos.OkResponse
import retrofit2.http.DELETE

/** The signed-in user's own account, outside better-auth (`api/account`). */
interface AccountApi {
    // Anonymises the profile and ends every session, so the bearer token stops working afterwards
    @DELETE("api/account")
    suspend fun deleteAccount(): OkResponse
}
