package com.mobilelens.mobilelens.core.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/**
 * Error body shape: the API answers `{ "error": "...", "code": "SOME_CODE", ...extra }`, and
 * better-auth (everything under `api/auth`) answers `{ "message": "...", "code": "SOME_CODE" }`.
 * `code` is stable, the text may be localised, so branch on `code`.
 */
@Serializable
private data class ApiErrorBody(val code: String? = null)

private val errorJson = Json { ignoreUnknownKeys = true }

/** The `code` of an error response body, or null when the body is missing or isn't JSON. */
internal fun parseApiErrorCode(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return try {
        errorJson.decodeFromString<ApiErrorBody>(body).code
    } catch (_: Exception) {
        null
    }
}

/** The backend's error `code` for this failed call, if it sent one. */
internal val HttpException.apiErrorCode: String?
    get() = parseApiErrorCode(
        // peek() leaves the buffered body readable for anyone else
        response()?.errorBody()?.source()?.peek()?.readUtf8()
    )
