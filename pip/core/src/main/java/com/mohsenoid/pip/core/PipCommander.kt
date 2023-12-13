package com.mohsenoid.pip.core

interface PipCommander {

    /**
     * Enter PiP mode.
     *
     * @param onSuccess will be called if PiP mode is entered successfully
     * @param onError will be called if PiP mode is not entered successfully
     */
    fun enterPip(
        onSuccess: ((alreadyInPipMode: Boolean) -> Unit)? = {},
        onError: ((PipEnterError) -> Unit)? = {},
    )

    /**
     * Close PiP mode.
     *
     * @param contentId optional contentId of the PiP window to close
     */
    fun closePip(contentId: String? = null)
}
