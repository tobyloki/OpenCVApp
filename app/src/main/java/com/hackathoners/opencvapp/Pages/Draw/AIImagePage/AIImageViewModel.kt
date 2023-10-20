package com.hackathoners.opencvapp.Pages.Draw.AIImagePage

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import com.google.auto.value.extension.AutoValueExtension.Context
import com.hackathoners.opencvapp.R
import com.hackathoners.opencvapp.Shared.Utility.ImageAPI
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AIImageViewModel : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private var activity: Activity? = null
    fun initialize(activity: Activity) {
        this.activity = activity
    }
    fun onCreate() {
        Timber.i("onCreate")
        val bitmap = BitmapFactory.decodeResource(activity?.resources, R.drawable.apple)

        // TODO: Change the bitmap to File format
//        val byteArray = convertBitmapToByteArray(bitmap, Bitmap.CompressFormat.PNG, 100)
//        ImageAPI.POST(byteArray,"apple", "cinematic")
    }

    fun onResume() {
        Timber.i("onResume")
    }

    fun onPause() {
        Timber.i("onPause")
    }


}