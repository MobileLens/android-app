package com.mobilelens.mobilelens.auth.data

// Same rule as PASSWORD_STRENGTH_REGEX in backend-api/api/src/lib/auth.ts, checked up front so a weak
// password gets a precise message instead of a round trip. The backend stays the authority:
// it answers 400 PASSWORD_TOO_WEAK if this ever drifts.
private val STRONG_PASSWORD =
    Regex("""^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\|,.<>/?]).{8,}$""")

/** At least 8 characters with a lowercase letter, an uppercase letter, a digit and a special character. */
fun isStrongPassword(password: String): Boolean = STRONG_PASSWORD.matches(password)
