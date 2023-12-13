package com.mohsenoid.pip.ui

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mohsenoid.pip.core.Pip
import com.mohsenoid.pip.core.PipObservable
import kotlinx.coroutines.launch

/**
 * A [ConstraintLayout] that is aware of PiP mode.
 */
class PipAwareConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {

    /**
     * Initialize this view.
     * This method must be called after the view is created.
     */
    fun init() {
        val lifecycleOwner =
            requireNotNull(findViewTreeLifecycleOwner()) { "Cannot find LifecycleOwner" }

        val pipObservable: PipObservable = Pip.getPipObservable()

        lifecycleOwner.lifecycleScope.launch {
            pipObservable.isInPipModeStateFlow.collect { isInPictureInPictureMode ->
                onPipModeChanged(isInPictureInPictureMode = isInPictureInPictureMode)
            }
        }
    }

    private fun onPipModeChanged(isInPictureInPictureMode: Boolean) {
        isVisible = !isInPictureInPictureMode
    }
}
