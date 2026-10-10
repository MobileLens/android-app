package com.mobilelens.mobilelens.core.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiErrorTest {
    @Test
    fun readsCodeFromApiError() {
        val body = """{"error":"Forbidden","code":"FORBIDDEN"}"""
        assertEquals("FORBIDDEN", parseApiErrorCode(body))
    }

    @Test
    fun readsCodeFromBetterAuthError() {
        val body = """{"message":"Your account has been banned","code":"USER_BANNED"}"""
        assertEquals("USER_BANNED", parseApiErrorCode(body))
    }

    @Test
    fun ignoresExtraFields() {
        val body = """{"error":"Bad field","code":"INVALID_FIELD","field":"aperture"}"""
        assertEquals("INVALID_FIELD", parseApiErrorCode(body))
    }

    @Test
    fun returnsNullWithoutCodeOrJson() {
        assertNull(parseApiErrorCode(null))
        assertNull(parseApiErrorCode(""))
        assertNull(parseApiErrorCode("<html>502 Bad Gateway</html>"))
        assertNull(parseApiErrorCode("""{"error":"no code"}"""))
    }
}
