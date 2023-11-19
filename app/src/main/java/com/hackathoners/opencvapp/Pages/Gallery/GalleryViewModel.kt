package com.hackathoners.opencvapp.Pages.Gallery

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.hackathoners.opencvapp.Shared.Models.GalleryImage
import com.hackathoners.opencvapp.Shared.Utility.ToastHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.File
import java.util.Date


@SuppressLint("MutableCollectionMutableState")
class GalleryViewModel : ViewModel() {
    private val viewModelJob = Job()
    private var coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    var recentPictures: MutableList<GalleryImage> by mutableStateOf(mutableListOf())
    var allPictures: MutableList<GalleryImage> by mutableStateOf(mutableListOf())

    init {
        // create items
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for (i in 1..5) {
            val galleryImage = GalleryImage(bitmap, File("file$i"))
            recentPictures += galleryImage
            allPictures += galleryImage
        }
    }

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
    }

    fun onResume() {
        Timber.i("onResume")

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Timber.i("READ_EXTERNAL_STORAGE not granted")
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        } else {
            // Permission is already granted
            // Do something
            Timber.i("READ_EXTERNAL_STORAGE permission already granted")
            getPictures()
        }
    }

    fun onPause() {
        Timber.i("onPause")
    }
    // endregion

    // region Business logic
    private fun getPictures() {
        // get path to Pictures folder
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        val path = "$root/OpenCVApp"
        // check if path exists
        val dir = File(path)
        if (!dir.exists()) {
            return
        }

        // get all files in path
        val files = dir.listFiles() ?: return
        // get all files that are images
        val imageFiles = files.filter { it.extension == "jpg" || it.extension == "jpeg" || it.extension == "png" }
        // convert to bitmaps
        val galleryImages: MutableList<GalleryImage> = mutableListOf()
        for (imageFile in imageFiles) {
            val filePath = imageFile.path;
            val bitmap = BitmapFactory.decodeFile(filePath)
            val galleryImage = GalleryImage(bitmap, imageFile)
            galleryImages += galleryImage
        }

        // sort by date (newest first)
        galleryImages.sortByDescending { it.file.lastModified() }

        // update all pictures
        allPictures = galleryImages

        // get list of recent pictures (pictures taken in the last 24 hours)
        val currentTime = Date().time
        val recents: MutableList<GalleryImage> = galleryImages.filter { currentTime - it.file.lastModified() < 24 * 60 * 60 * 1000 }.toMutableList()

        // update recent pictures
        recentPictures = recents
    }
    // endregion

    // region Button actions
    fun goToIndividualPage(galleryImage: GalleryImage) {
        // TODO: implement
        ToastHelper.showToast(activity, "Go to individual page")
    }
    // endregion
}