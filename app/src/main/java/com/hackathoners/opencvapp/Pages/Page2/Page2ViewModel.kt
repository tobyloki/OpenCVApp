package com.hackathoners.opencvapp.Pages.Page2

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.opencv.android.OpenCVLoader
import timber.log.Timber

class Page2ViewModel : ViewModel() {
    var value by mutableStateOf("")
    var count by mutableStateOf(0)

    private val viewModelJob = Job()
    private var coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // region Initialize
    @SuppressLint("StaticFieldLeak")
    private lateinit var activity: Activity
    fun initialize(activity: Activity) {
        this.activity = activity
    }
    // endregion

    // region Lifecycle
    fun onCreate() {
        Timber.i("onCreate")

        val intent = activity.intent.extras
        value = intent?.getString("value") ?: "no value"

        if (OpenCVLoader.initDebug()) {
            Timber.i("OpenCVLoader.initDebug() success")
        } else {
            Timber.i("OpenCVLoader.initDebug() fail")
        }
    }

    fun onResume() {
        Timber.i("onResume")
    }

    fun onPause() {
        Timber.i("onPause")
    }
    // endregion

    // region Button actions
    fun handleBtnClick() {
        Timber.i("buttonAction1")
        count++
    }
    // endregion
}