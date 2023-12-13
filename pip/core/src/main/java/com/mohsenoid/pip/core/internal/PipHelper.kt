package com.mohsenoid.pip.core.internal

import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import androidx.annotation.RequiresApi
import com.mohsenoid.pip.core.PipCommander
import com.mohsenoid.pip.core.PipEnterError
import com.mohsenoid.pip.core.PipObservable
import com.mohsenoid.pip.core.isPipAllowedInSettings
import com.mohsenoid.pip.core.isPipSupported
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

internal class PipHelper(
    private val context: Context,
) : PipController, PipCommander, PipObservable {

    // region private properties
    private val isPipSupported: Boolean
        get() = context.isPipSupported()

    private val isPipAllowedInSettings: Boolean
        get() = context.isPipAllowedInSettings()

    private var isPlayerPlayingStateFlow = MutableStateFlow(false)

    private var contentId: String? = null
    // endregion

    // region PipObserver
    override val isPipAllowedStateFlow: StateFlow<Boolean> =
        isPlayerPlayingStateFlow.map { isPlayerPlaying ->
            isPlayerPlaying && isPipSupported && isPipAllowedInSettings
        }.stateIn(
            scope = MainScope() + CoroutineName("PipHelper"),
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    private val _isInPipModeStateFlow = MutableStateFlow(false)
    override val isInPipModeStateFlow: StateFlow<Boolean> by ::_isInPipModeStateFlow

    override fun isInPipMode(contentId: String?): Boolean {
        if (contentId != null && contentId != this.contentId) return false
        return isInPipModeStateFlow.value
    }
    // endregion

    // region PipActor
    private val _enterPipEventFlow = MutableSharedFlow<Boolean>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    )
    override val enterPipEventFlow: SharedFlow<Boolean> by ::_enterPipEventFlow

    private val _closePipEventFlow = MutableSharedFlow<Boolean>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    )
    override val closePipEventFlow: SharedFlow<Boolean> by ::_closePipEventFlow

    override fun updatePlayerStatus(isPlaying: Boolean, contentId: String?) {
        this.contentId = contentId
        this.isPlayerPlayingStateFlow.value = isPlaying
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getPipParams(
        actions: List<RemoteAction>,
        rational: Rational?,
        pipRect: Rect?,
    ): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(rational)
            .setSourceRectHint(pipRect)
            .setActions(actions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(isPipAllowedStateFlow.value)
        }

        return builder.build()
    }

    override fun updateIsInPipMode(isInPipMode: Boolean) {
        _isInPipModeStateFlow.value = isInPipMode
    }

    override fun resetPip() {
        isPlayerPlayingStateFlow.value = false
        _isInPipModeStateFlow.value = false
    }
    // endregion

    // region PipController
    override fun enterPip(
        onSuccess: ((alreadyInPipMode: Boolean) -> Unit)?,
        onError: ((PipEnterError) -> Unit)?,
    ) {
        when {
            !isPipSupported -> {
                onError?.invoke(PipEnterError.NotSupported)
            }

            !isPipAllowedInSettings -> {
                onError?.invoke(PipEnterError.DisabledByUser)
            }

            isInPipModeStateFlow.value -> {
                onSuccess?.invoke(true)
            }

            else -> {
                _enterPipEventFlow.tryEmit(true)
                onSuccess?.invoke(false)
            }
        }
    }

    override fun closePip(contentId: String?) {
        if (contentId == null || isInPipMode(contentId)) {
            _closePipEventFlow.tryEmit(true)
        }
    }
    // endregion
}
