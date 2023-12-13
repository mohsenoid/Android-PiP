package com.mohsenoid.pip.sample

import android.app.Application
import com.mohsenoid.pip.core.Pip

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the PiP library
        Pip.init(this)
    }
}