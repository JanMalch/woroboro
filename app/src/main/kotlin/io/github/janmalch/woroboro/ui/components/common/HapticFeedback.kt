package io.github.janmalch.woroboro.ui.components.common

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Immutable
interface HapticFeedback {
    fun dragStart()
    fun gestureEnd()
    fun segmentFrequentTick()

    companion object {
        operator fun invoke(view: View): HapticFeedback =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                HapticFeedbackImpl(view)
            } else {
                HapticFeedbackNoop
            }
    }
}

@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) { HapticFeedback(view) }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
private class HapticFeedbackImpl(private val view: View) : HapticFeedback {
    override fun dragStart() {
        view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
    }

    override fun gestureEnd() {
        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
    }

    override fun segmentFrequentTick() {
        view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
    }
}

private object HapticFeedbackNoop : HapticFeedback {
    override fun dragStart() {
        /* no-op */
    }

    override fun gestureEnd() {
        /* no-op */
    }

    override fun segmentFrequentTick() {
        /* no-op */
    }
}
