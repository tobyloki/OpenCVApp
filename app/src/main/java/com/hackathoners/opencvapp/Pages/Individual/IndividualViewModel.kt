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
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.hackathoners.opencvapp.Pages.Gallery.GalleryView
import com.hackathoners.opencvapp.Shared.Utility.ToastHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.File

class IndividualViewModel : ViewModel() {
    var image by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    var file: File? = null
        private set
    var filePath: String? = null
        private set
    private val viewModelJob = Job()
    private var coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    var showDeleteAlert by mutableStateOf(false)
    var showErrorDeletionAlert by mutableStateOf(false)

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

    fun setFilePath(filePath: String?) {
        this.filePath = filePath
    }

    fun shareImage(filePath: String?) {
        val file = File(filePath)

        if (file.exists()) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val fileUri: Uri? = FileProvider.getUriForFile(activity, "com.hackathoners.opencvapp.fileprovider", file)
            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            activity.startActivity(Intent.createChooser(intent, "Share Image"))
        } else {
            ToastHelper.showToast(activity, "Error: Cannot share the image.")
        }
    }

    fun deleteImage(filePath: String?) {
        val file = File(filePath)

        if (file.delete()) {
            ToastHelper.showToast(activity, "Image deleted successfully.")
            activity.onBackPressed()
        } else {
            ToastHelper.showToast(activity, "Error: Cannot delete image.")
            this.showErrorDeletionAlert = true
        }
    }
}