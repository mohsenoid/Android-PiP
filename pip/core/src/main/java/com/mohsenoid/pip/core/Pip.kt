package com.mohsenoid.pip.core

import android.annotation.SuppressLint
import android.content.Context
import com.mohsenoid.pip.core.internal.PipController
import com.mohsenoid.pip.core.internal.PipHelper

object Pip {

    /**
     * Initialize PiP library.
     *
     * @param context The application context.
     */
    fun init(context: Context) {
        getPipHelper(context)
    }

    @SuppressLint("StaticFieldLeak")
    @Volatile
    private var pipHelper: PipHelper? = null

    private fun getPipHelper(context: Context): PipHelper {
        val checkInstance = pipHelper
        if (checkInstance != null) {
            return checkInstance
        }

        return synchronized(this) {
            val checkInstanceAgain = pipHelper
            if (checkInstanceAgain != null) {
                checkInstanceAgain
            } else {
                val created = PipHelper(context)
                pipHelper = created
                created
            }
        }
    }

    internal fun getPipController(): PipController {
        return requireNotNull(pipHelper) { "PiP is not initialized!" }
    }

    /**
     * Get the [PipObservable] instance.
     *
     * @return The [PipObservable] instance.
     */
    fun getPipObservable(): PipObservable {
        return requireNotNull(pipHelper) { "PiP is not initialized!" }
    }

    /**
     * Get the [PipCommander] instance.
     *
     * @return The [PipCommander] instance.
     */
    fun getPipCommander(): PipCommander {
        return requireNotNull(pipHelper) { "PiP is not initialized!" }
    }
}
