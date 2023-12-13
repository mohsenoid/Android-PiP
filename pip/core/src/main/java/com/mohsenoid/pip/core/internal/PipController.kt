package com.mohsenoid.pip.core.internal

import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

internal interface PipController {

    val isPipAllowedStateFlow: StateFlow<Boolean>

    val enterPipEventFlow: SharedFlow<Boolean>

    val closePipEventFlow: SharedFlow<Boolean>

    fun updatePlayerStatus(isPlaying: Boolean, contentId: String?)

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPipParams(
        actions: List<RemoteAction>,
        rational: Rational?,
        pipRect: Rect?,
    ): PictureInPictureParams

    fun updateIsInPipMode(isInPipMode: Boolean)

    fun resetPip()
}
