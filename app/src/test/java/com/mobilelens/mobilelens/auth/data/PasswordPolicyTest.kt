package com.mobilelens.mobilelens.auth.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mirrors PASSWORD_STRENGTH_REGEX in backend-api/api/src/lib/auth.ts. */
class PasswordPolicyTest {
    @Test
    fun acceptsPasswordWithEveryCharacterClass() {
        assertTrue(isStrongPassword("Abcdef1!"))
        assertTrue(isStrongPassword("correct-Horse-battery-9"))
    }

    @Test
    fun rejectsPasswordMissingAClass() {
        assertFalse(isStrongPassword("abcdef1!"))   // no uppercase
        assertFalse(isStrongPassword("ABCDEF1!"))   // no lowercase
        assertFalse(isStrongPassword("Abcdefg!"))   // no digit
        assertFalse(isStrongPassword("Abcdefg1"))   // no special character
    }

    @Test
    fun rejectsShortPassword() {
        assertFalse(isStrongPassword("Ab1!"))
        assertFalse(isStrongPassword("Abcd1!x"))
    }

    @Test
    fun acceptsEightCharactersExactly() {
        assertTrue(isStrongPassword("Abcde1!x"))
    }
}
