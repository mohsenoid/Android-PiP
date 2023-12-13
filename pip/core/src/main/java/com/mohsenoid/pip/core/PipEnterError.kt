package com.mohsenoid.pip.core

sealed interface PipEnterError {

    /**
     * PiP feature is not supported by Device and switching to PiP mode is not possible
     */
    data object NotSupported : PipEnterError

    /**
     * PiP feature is disabled by User and switching to PiP mode is not possible
     */
    data object DisabledByUser : PipEnterError
}
