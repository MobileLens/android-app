package com.mobilelens.mobilelens.core.ui

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

// The app's haptic vocabulary, so the same kind of interaction feels the same on every screen.
// Material 3 components don't give feedback of their own, so each call site picks one of these.
// They go through the view, which drops them when touch feedback is off in the system settings
// and falls back to the closest older effect where a newer one isn't available.

/**
 * Wraps [delegate] so every call site respects the Settings switch without checking it itself.
 * [enabled] is read on each call, so a change applies to the very next effect.
 */
class SwitchableHapticFeedback(
    private val delegate: HapticFeedback,
    private val enabled: () -> Boolean,
) : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        if (enabled()) delegate.performHapticFeedback(hapticFeedbackType)
    }
}

/** A two-state control flipped: a favorite, a like, a switch, a toggle chip, the FAB menu. */
fun HapticFeedback.performToggle(on: Boolean) {
    performHapticFeedback(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
}

/** One option picked from several: a tab, a segment, a navigation item, a menu or radio entry. */
fun HapticFeedback.performSelect() {
    performHapticFeedback(HapticFeedbackType.SegmentTick)
}

/** Something the user waited on went through, e.g. a review published or a sign-in. */
fun HapticFeedback.performConfirm() {
    performHapticFeedback(HapticFeedbackType.Confirm)
}

/** Something the user asked for failed or was refused. */
fun HapticFeedback.performReject() {
    performHapticFeedback(HapticFeedbackType.Reject)
}

/** A drag went far enough that letting go acts on it, e.g. pulling to refresh. */
fun HapticFeedback.performThresholdReached() {
    performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
}

/** Confirms or rejects, by how [message] reports the action it follows. */
fun HapticFeedback.performOutcome(message: UserMessage) {
    if (message.isError) performReject() else performConfirm()
}
