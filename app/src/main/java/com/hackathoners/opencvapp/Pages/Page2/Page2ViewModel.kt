package com.hackathoners.opencvapp.Pages.Page2

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hackathoners.opencvapp.R
import com.hackathoners.opencvapp.rotateBitmap
import com.hackathoners.opencvapp.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import java.io.ByteArrayOutputStream


class Page2ViewModel : ViewModel() {
    var value by mutableStateOf("")
    var count by mutableStateOf(0)
    var image by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

    private val viewModelJob = Job()
    private var coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // region Initialize
    @SuppressLint("StaticFieldLeak")
    private var activity: Activity? = null
    fun initialize(activity: Activity) {
        this.activity = activity

        val bitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.cat)
        image = invertBitmapColors(bitmap)
    }
    // endregion

    // region Lifecycle
    fun onCreate() {
        Timber.i("onCreate")

        val intent = activity?.intent?.extras
        value = intent?.getString("value") ?: "no value"
    }

    fun onResume() {
        Timber.i("onResume")
    }

    fun onPause() {
        Timber.i("onPause")
    }
    // endregion

    // region Business logic
    private fun invertBitmapColors(bitmap: Bitmap): Bitmap {
        // https://www.youtube.com/watch?v=wJHv83HsPjA&list=PLTuKYqpidPXZjtOEjOgeKxNAe4NzSgsg8&index=5
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }
    // endregion

    // region Button actions
    fun handleBtnClick() {
        Timber.i("buttonAction1")
        count++
    }

    fun handleImage(bitmap: Bitmap) {
        this.image = invertBitmapColors(bitmap)

//        Timber.i("hello world")
    }
    // endregion
}
