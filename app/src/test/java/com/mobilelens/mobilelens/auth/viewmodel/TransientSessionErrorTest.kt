package com.mobilelens.mobilelens.auth.viewmodel

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class TransientSessionErrorTest {
    private fun httpError(code: Int) = HttpException(Response.error<Unit>(code, "".toResponseBody()))

    @Test
    fun retriesWithoutConnection() {
        assertTrue(isTransientSessionError(IOException("offline")))
        assertTrue(isTransientSessionError(SocketTimeoutException()))
    }

    @Test
    fun retriesWhenServerFails() {
        assertTrue(isTransientSessionError(httpError(500)))
        assertTrue(isTransientSessionError(httpError(503)))
        assertTrue(isTransientSessionError(httpError(408)))
        assertTrue(isTransientSessionError(httpError(429)))
    }

    @Test
    fun givesUpOnDefiniteAnswers() {
        assertFalse(isTransientSessionError(httpError(401)))
        assertFalse(isTransientSessionError(httpError(403)))
        assertFalse(isTransientSessionError(httpError(404)))
        assertFalse(isTransientSessionError(IllegalStateException("not JSON")))
    }
}
