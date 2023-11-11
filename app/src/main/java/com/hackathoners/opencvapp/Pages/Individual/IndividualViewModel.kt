package com.hackathoners.opencvapp.Pages.Individual

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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

class IndividualViewModel : ViewModel() {
    var image by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

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
        // get from shared preferences
        val sharedPref = activity.getPreferences(Activity.MODE_PRIVATE) ?: return

    }

    fun onResume() {
        Timber.i("onResume")
    }

    fun onPause() {
        Timber.i("onPause")
    }
    // endregion

    fun goToGalleryPage() {
        val intent = Intent(activity, GalleryView::class.java)
        activity.startActivity(intent)
    }
}