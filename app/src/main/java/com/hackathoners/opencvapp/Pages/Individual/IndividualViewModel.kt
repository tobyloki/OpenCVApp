package com.hackathoners.opencvapp.Pages.Individual

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Intent.getIntent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hackathoners.opencvapp.Pages.Gallery.GalleryView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.File

class IndividualViewModel : ViewModel() {
    var image by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    var file: File? = null
    private val viewModelJob = Job()
    private var coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // region Initialize
    @SuppressLint("StaticFieldLeak")
    private lateinit var activity: Activity
    fun initialize(activity: Activity) {
        this.activity = activity
    }

    // region Lifecycle
    fun onCreate() {
        Timber.i("onCreate")

    }

    fun onResume() {
        Timber.i("onResume")
    }

    fun onPause() {
        Timber.i("onPause")
    }
    // endregion

    fun setGalleryImage(image: Bitmap) {
        this.image = image
    }

    fun setGalleryFile(file: File) {
        this.file = file
    }

    fun goToGalleryPage() {
        val intent = Intent(activity, GalleryView::class.java)
        activity.startActivity(intent)
    }
}