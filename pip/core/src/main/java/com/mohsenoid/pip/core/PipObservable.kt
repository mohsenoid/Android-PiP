package com.mohsenoid.pip.core

import kotlinx.coroutines.flow.StateFlow

interface PipObservable {

    /**
     * Based on player state and device, indicates if switching to PiP mode is allowed or not.
     * Can be used for enabling/disabling PiP mode button.
     *
     * @return a Boolean StateFlow which is true if PiP switching is allowed
     */
    val isPipAllowedStateFlow: StateFlow<Boolean>

    /**
     * Indicates if the Activity is in PiP mode or not.
     *
     * @return a Boolean StateFlow which is true if the Activity is in PiP mode
     */
    val isInPipModeStateFlow: StateFlow<Boolean>

    /**
     * Indicates if the Activity is in PiP mode or not.
     *
     * @param contentId optional contentId of the PiP window to check
     * @return true if the Activity with the specified contentId is in PiP mode
     */
    fun isInPipMode(contentId: String? = null): Boolean
}
