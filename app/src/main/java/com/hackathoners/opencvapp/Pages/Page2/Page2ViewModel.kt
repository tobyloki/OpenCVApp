package com.hackathoners.opencvapp.Pages.Page2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import timber.log.Timber

// create enum called mode with values of camera or video
enum class Mode {
    CAMERA,
    VIDEO
}

class Page2ViewModel : ViewModel() {
    var originalImage by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    var thresholdImage by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    var outputImage by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

    private val viewModelJob = Job()
    private var coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Default)

    private lateinit var retriever: MediaMetadataRetriever

    private var lowerSkinH = 0f
    private var lowerSkinS = 0f
    private var lowerSkinV = 0f
    private var upperSkinH = 0f
    private var upperSkinS = 0f
    private var upperSkinV = 0f
    private var thresh = 0f
    private var maxval = 0f

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

        val resId = activity?.resources?.getIdentifier("handw", "raw", activity?.packageName)
        Timber.i("resId: $resId")
        val uri = Uri.parse("android.resource://${activity?.packageName}/$resId")
        Timber.i("uri: $uri")
        retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(activity, uri)
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun onResume() {
        Timber.i("onResume")

        loadCalibrationValues()
    }

    fun onPause() {
        Timber.i("onPause")
    }
    // endregion

    // region Business logic
    private fun loadCalibrationValues() {
        val sharedPref = activity?.getPreferences(Activity.MODE_PRIVATE) ?: return
        lowerSkinH = sharedPref.getFloat("lowerSkinH", 0f)
        lowerSkinS = sharedPref.getFloat("lowerSkinS", 50f)
        lowerSkinV = sharedPref.getFloat("lowerSkinV", 50f)
        upperSkinH = sharedPref.getFloat("upperSkinH", 200f)
        upperSkinS = sharedPref.getFloat("upperSkinS", 240f)
        upperSkinV = sharedPref.getFloat("upperSkinV", 240f)
        thresh = sharedPref.getFloat("thresh", 200f)
        maxval = sharedPref.getFloat("maxval", 255f)
    }

    private fun getVideoFrame(time: Long): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            bitmap = retriever.getFrameAtTime(time)
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
        }
        return bitmap
    }
    // endregion

    // region UI actions
    fun handleVideoFrame(time: Long) {
        coroutineScope.launch {
//            Timber.i("handleVideoFrame: $time")
            val bitmap = getVideoFrame(time)
            if (bitmap != null) {
                handleImage(bitmap)
            }
        }
    }

    fun handleImage(bitmap: Bitmap) {
        coroutineScope.launch {
            // hand tracking on bitmap
            val frame = Mat()
            Utils.bitmapToMat(bitmap, frame)

            // START

            // Apply skin color segmentation (you may need to adjust these values)
            val hsvFrame = Mat()
            Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV)

            val lowerSkin = Scalar(lowerSkinH.toDouble(), lowerSkinS.toDouble(), lowerSkinV.toDouble())
            val upperSkin = Scalar(upperSkinH.toDouble(), upperSkinS.toDouble(), upperSkinV.toDouble())
            val mask = Mat()
            Core.inRange(hsvFrame, lowerSkin, upperSkin, mask)

            // blur the mask to reduce noise
            val blur = Mat()
            Imgproc.blur(mask, blur, Size(2.0, 2.0))

            // get threshold image
            val threshold = Mat()
            Imgproc.threshold(mask, threshold, thresh.toDouble(), maxval.toDouble(), Imgproc.THRESH_BINARY)

            // Find contours in the mask
            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(
                threshold,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
            )

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
                Imgproc.rectangle(
                    frame,
                    boundingRect.tl(),
                    boundingRect.br(),
                    Scalar(0.0, 255.0, 0.0),
                    2
                )

                // Compute the centroid of the largest contour
                val moments = Imgproc.moments(maxContour)
                val cx = (moments.m10 / moments.m00).toInt()
                val cy = (moments.m01 / moments.m00).toInt()

                // Draw a circle with the center as the centroid and the radius based on the bounding rectangle's diagonal length
//            val radius = Math.sqrt((boundingRect.width * boundingRect.width + boundingRect.height * boundingRect.height) / 2.0).toInt()
                Imgproc.circle(
                    frame,
                    Point(cx.toDouble(), cy.toDouble()),
                    10,
                    Scalar(0.0, 255.0, 0.0),
                    2
                )
            }

            // END

            val thresholdBitmap: Bitmap =
                Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(threshold, thresholdBitmap)
            val newBitmap: Bitmap =
                Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(frame, newBitmap)

            // run on UI thread
            activity?.runOnUiThread {
                originalImage = bitmap
                thresholdImage = thresholdBitmap
                outputImage = newBitmap
            }
        }
    }
    // endregion
}
