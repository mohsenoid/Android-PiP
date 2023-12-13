package com.mohsenoid.pip.core

import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mohsenoid.pip.core.internal.PipController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * This class is a helper for [AppCompatActivity] to support Picture-in-Picture mode.
 */
abstract class PipSupportAppCompatActivity : AppCompatActivity() {

    private val pipController: PipController = Pip.getPipController()
    private var pipActions: List<RemoteAction> = emptyList()
    private var rational: Rational? = null
    private var pipRect: Rect? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pipController.enterPipEventFlow.collect { enterPipMode() }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pipController.closePipEventFlow.collect { finish() }
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pipController.isPipAllowedStateFlow.collectLatest { setPipParams() }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        setPipParams()
        pipController.updateIsInPipMode(isInPictureInPictureMode)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        setPipParams()
    }

    /**
     * This method is used to set PiP mode [RemoteAction]s.
     *
     * @param pipActions The list of [RemoteAction]s that will be shown in PiP mode.
     */
    fun updatePipActions(pipActions: List<RemoteAction>) {
        this.pipActions = pipActions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        setPipParams()
    }

    /**
     * This method is used to set PiP mode ratio using a [Rational] object.
     *
     * @param rational The [Rational] object that represents the PiP mode ratio.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun updatePipRatio(rational: Rational) {
        this.rational = rational
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        setPipParams()
    }

    /**
     * This method is used to set PiP mode Rectangle for a smooth animation using a [Rect] object.
     *
     * @param pipRect The [Rect] object that represents the PiP mode Rectangle. You can use Player view's global visible rect.
     */
    fun updatePipRect(pipRect: Rect) {
        this.pipRect = pipRect
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        setPipParams()
    }

    /**
     * This method is used to update the player state, so that the PiP mode can be entered automatically when the player is playing.
     *
     * @param isPlaying The player state, true if the player is playing, false otherwise.
     */
    fun updatePlayerState(isPlaying: Boolean, contentId: String? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        pipController.updatePlayerStatus(isPlaying = isPlaying, contentId = contentId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setPipParams() {
        val params: PictureInPictureParams =
            pipController.getPipParams(pipActions, rational, pipRect)
        setPictureInPictureParams(params)
    }

    private fun enterPipMode(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            enterPictureInPictureMode()
            return true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params: PictureInPictureParams = pipController.getPipParams(
                pipActions,
                rational,
                pipRect,
            )
            return enterPictureInPictureMode(params)
        }

        return false
    }

    override fun onUserLeaveHint() {
        if (!pipController.isPipAllowedStateFlow.value) return

        // This checks if it is an old SDK and we should call enter PiP mode manually
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // There is NO PiP auto enter feature, and NO PiP Params, and we should do it manually
            @Suppress("DEPRECATION")
            enterPictureInPictureMode()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // There is NO PiP auto enter feature, and we should do it manually
            val params: PictureInPictureParams = pipController.getPipParams(
                pipActions,
                rational,
                pipRect,
            )
            enterPictureInPictureMode(params)
        }

        super.onUserLeaveHint()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        } else {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        }
        pipController.updateIsInPipMode(isInPictureInPictureMode)
    }

    override fun onStop() {
        super.onStop()
        pipController.resetPip()
    }
}
