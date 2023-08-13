package com.hackathoners.opencvapp

import android.app.Application
import org.opencv.android.OpenCVLoader
import timber.log.Timber

class OpenCVApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Setup Timber for logging
        Timber.plant(Timber.DebugTree())

        // Setup OpenCV
        if (OpenCVLoader.initDebug()) {
            Timber.i("OpenCVLoader.initDebug() success")
        } else {
            Timber.i("OpenCVLoader.initDebug() fail")
        }
    }
}