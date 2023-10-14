package com.hackathoners.opencvapp.Pages.Home

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
import com.hackathoners.opencvapp.Pages.Draw.CalibrationPage.CalibrationPageView
import com.hackathoners.opencvapp.Pages.Draw.DrawView
import com.hackathoners.opencvapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import timber.log.Timber

class HomeViewModel : ViewModel() {
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

        // get from shared preferences
        val sharedPref = activity?.getPreferences(Activity.MODE_PRIVATE) ?: return
        count = sharedPref.getInt("count", 0)
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

    fun handleImagePickerResult(uri: Uri?) {
        Timber.i("handleImagePickerResult: $uri")
        if (uri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
            image = invertBitmapColors(bitmap)
        }
    }

    // region Button actions
    fun handleBtnClick() {
        Timber.i("increment count")
        count++

        // save to shared preferences
        val sharedPref = activity?.getPreferences(Activity.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt("count", count)
            apply()
        }
    }

    fun goToCalibrationPage() {
        val intent = Intent(activity, CalibrationPageView::class.java)
        activity?.startActivity(intent)
    }

    fun goToDrawPage() {
        val intent = Intent(activity, DrawView::class.java)
        activity?.startActivity(intent)
    }

//    fun selectImage() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "image/*"
//        activity.startActivityForResult(intent, 1)
//    }
    // endregion
}