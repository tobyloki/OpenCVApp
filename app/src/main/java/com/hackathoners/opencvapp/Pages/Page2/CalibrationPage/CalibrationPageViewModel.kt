package com.hackathoners.opencvapp.Pages.Page2.CalibrationPage

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import timber.log.Timber


class CalibrationPageViewModel : ViewModel() {
    var originalImage by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    var thresholdImage by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    var outputImage by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    var lowerSkin by mutableStateOf<Scalar>(Scalar(0.0, 50.0, 50.0))
    var upperSkin by mutableStateOf<Scalar>(Scalar(200.0, 240.0, 240.0))

    // Default skin-tone threshold bounds
    val defaultLowerSkin = Scalar(0.0, 50.0, 50.0)
    val defaultUpperSkin = Scalar(200.0, 240.0, 240.0)
    val defaultThresh = 200.0
    val defaultMaxval = 255.0

    // For the HSV lower values
    var slider1Value by mutableStateOf(0f)
    var slider2Value by mutableStateOf(50f)
    var slider3Value by mutableStateOf(50f)

    // For the HSV upper values
    var slider4Value by mutableStateOf(200f)
    var slider5Value by mutableStateOf(240f)
    var slider6Value by mutableStateOf(240f)

    // For thresh and maxval
    var slider7Value by mutableStateOf(200f)
    var slider8Value by mutableStateOf(255f)


//    private val viewModelJob = Job()
//    private var coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // region Initialize
    @SuppressLint("StaticFieldLeak")
    private var activity: Activity? = null
    fun initialize(activity: Activity) {
        this.activity = activity

//        val bitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.cat)
//        image = invertBitmapColors(bitmap)
    }
    // endregion

    // region Lifecycle
    fun onCreate() {
        Timber.i("onCreate")

//        val intent = activity?.intent?.extras
//        value = intent?.getString("value") ?: "no value"
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

    fun setLowerSkinValues() {
        lowerSkin = Scalar(slider1Value.toDouble(), slider2Value.toDouble(), slider3Value.toDouble())
    }

    fun setUpperSkinValues() {
        upperSkin = Scalar(slider4Value.toDouble(), slider5Value.toDouble(), slider6Value.toDouble())
    }

    fun resetToDefault() {
        lowerSkin = defaultLowerSkin
        upperSkin = defaultUpperSkin
        //TODO: Reset the slider values as well
    }

    // endregion

    // region Button actions
    fun handleImage(bitmap: Bitmap) {

        val frame = Mat()
        Utils.bitmapToMat(bitmap, frame)

        // START

        // Apply skin color segmentation (you may need to adjust these values)
        val hsvFrame = Mat()
        Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV)

        //val lowerSkin = Scalar(0.0, 50.0, 50.0)
        //val upperSkin = Scalar(200.0, 240.0, 240.0)
        val mask = Mat()
        Core.inRange(hsvFrame, lowerSkin, upperSkin, mask)

        // blur the mask to reduce noise
        val blur = Mat()
        Imgproc.blur(mask, blur, Size(2.0, 2.0))

        // get threshold image
        val threshold = Mat()
        Imgproc.threshold(mask, threshold, 200.0, 255.0, Imgproc.THRESH_BINARY)

        // Find contours in the mask
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(threshold, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        // Find the largest contour (assuming it's the hand)
        var maxArea = 0.0
        var maxContour: MatOfPoint? = null
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > maxArea) {
                maxArea = area
                maxContour = contour
            }
        }

        // Draw a bounding box around the hand
        if (maxContour != null) {
            val boundingRect = Imgproc.boundingRect(maxContour)
            Imgproc.rectangle(frame, boundingRect.tl(), boundingRect.br(), Scalar(0.0, 255.0, 0.0), 2)

            // Compute the centroid of the largest contour
            val moments = Imgproc.moments(maxContour)
            val cx = (moments.m10 / moments.m00).toInt()
            val cy = (moments.m01 / moments.m00).toInt()

            // Draw a circle with the center as the centroid and the radius based on the bounding rectangle's diagonal length
//            val radius = Math.sqrt((boundingRect.width * boundingRect.width + boundingRect.height * boundingRect.height) / 2.0).toInt()
            Imgproc.circle(frame, Point(cx.toDouble(), cy.toDouble()), 10, Scalar(0.0, 255.0, 0.0), 2)
        }

        // END

        this.originalImage = bitmap

        val thresholdBitmap: Bitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(threshold, thresholdBitmap)

        this.thresholdImage = thresholdBitmap

        val newBitmap: Bitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(frame, newBitmap)

        this.outputImage = newBitmap

    }
    // endregion
}
