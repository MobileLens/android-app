package com.mobilelens.mobilelens.core.ui

import androidx.annotation.StringRes

/**
 * A one-off message for a snackbar or toast, from a ViewModel whose messages report both
 * successes and failures. [isError] picks the haptic that comes with it.
 */
data class UserMessage(@StringRes val textRes: Int, val isError: Boolean)
