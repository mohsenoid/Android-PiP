package com.mohsenoid.pip.sample

import androidx.multidex.MultiDexApplication
import com.mohsenoid.pip.core.Pip

class SampleApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the PiP library
        Pip.init(this)
    }
}