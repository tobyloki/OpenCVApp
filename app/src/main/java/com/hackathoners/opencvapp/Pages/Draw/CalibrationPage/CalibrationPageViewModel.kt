package com.hackathoners.opencvapp.Pages.Draw.CalibrationPage

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
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
    var thresh by mutableDoubleStateOf(0.0)
    var maxval by mutableDoubleStateOf(255.0)

    // Default skin-tone threshold bounds
    val defaultLowerSkin = Scalar(0.0, 50.0, 50.0)
    val defaultUpperSkin = Scalar(200.0, 240.0, 240.0)
    val defaultThresh = 200.0
    val defaultMaxval = 255.0
    // For the HSV lower values
    val defaultSlider1Values = mutableListOf(0f, 50f, 50f)
    // For the HSV upper values
    val defaultSlider2Values = mutableListOf(200f, 240f, 240f)
    // For defaultThresh, defaultMaxval
    val defaultSlider3Values = mutableListOf(200f, 255f)

    // For the HSV lower values
    var slider1Value by mutableFloatStateOf(defaultSlider1Values[0])
    var slider2Value by mutableFloatStateOf(defaultSlider1Values[1])
    var slider3Value by mutableFloatStateOf(defaultSlider1Values[2])

    // For the HSV upper values
    var slider4Value by mutableFloatStateOf(defaultSlider2Values[0])
    var slider5Value by mutableFloatStateOf(defaultSlider2Values[1])
    var slider6Value by mutableFloatStateOf(defaultSlider2Values[2])

    // For Thresh and Maxval
    var slider7Value by mutableFloatStateOf(defaultSlider3Values[0])
    var slider8Value by mutableFloatStateOf(defaultSlider3Values[1])

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

        loadSkinValues()
    }

    fun onResume() {
        Timber.i("onResume")
    }

    fun onPause() {
        Timber.i("onPause")
    }
    // endregion

    // region Business logic
    fun setLowerSkinValues() {
        lowerSkin = Scalar(slider1Value.toDouble(), slider2Value.toDouble(), slider3Value.toDouble())
    }

    fun setUpperSkinValues() {
        upperSkin = Scalar(slider4Value.toDouble(), slider5Value.toDouble(), slider6Value.toDouble())
    }

    fun setSkinValues() {
        setLowerSkinValues()
        setUpperSkinValues()
        thresh = slider7Value.toDouble()
        maxval = slider8Value.toDouble()
    }

    fun loadSkinValues() {
        // get from shared preferences
        val sharedPref = activity?.getSharedPreferences("calibration", Activity.MODE_PRIVATE) ?: return

        // load lower hsv values
        val lowhue = sharedPref.getFloat("lower-skin-h", 0f)
        val lowsat = sharedPref.getFloat("lower-skin-s", 50f)
        val lowvalue = sharedPref.getFloat("lower-skin-v", 50f)
        lowerSkin = Scalar(lowhue.toDouble(), lowsat.toDouble(), lowvalue.toDouble())

        // load upper hsv values
        val highhue = sharedPref.getFloat("upper-skin-h", 200f)
        val highsat = sharedPref.getFloat("upper-skin-s", 240f)
        val highvalue = sharedPref.getFloat("upper-skin-v", 240f)
        upperSkin = Scalar(highhue.toDouble(), lowsat.toDouble(), lowvalue.toDouble())

        // load thresh & maxval
        thresh = sharedPref.getFloat("skin-thresh", 200f).toDouble()
        maxval = sharedPref.getFloat("skin-maxval", 255f).toDouble()

        // Set slider positions to loaded values
        slider1Value = lowhue
        slider2Value = lowsat
        slider3Value = lowvalue
        slider4Value = highhue
        slider5Value = highsat
        slider6Value = highvalue
        slider7Value = thresh.toFloat()
        slider8Value = maxval.toFloat()

        setSkinValues()

        Timber.i("lowerSkinH: $lowhue")
        Timber.i("lowerSkinS: $lowsat")
        Timber.i("lowerSkinV: $lowvalue")
        Timber.i("upperSkinH: $highhue")
        Timber.i("upperSkinS: $highsat")
        Timber.i("upperSkinV: $highvalue")
        Timber.i("thresh: $thresh")
        Timber.i("maxval: $maxval")
    }

    fun saveSkinValues() {
        // Save lower skin values to shared preferences memory
        // save to shared preferences
        val sharedPref = activity?.getSharedPreferences("calibration", Activity.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putFloat("lower-skin-h", slider1Value)
            putFloat("lower-skin-s", slider2Value)
            putFloat("lower-skin-v", slider3Value)
            putFloat("upper-skin-h", slider4Value)
            putFloat("upper-skin-s", slider5Value)
            putFloat("upper-skin-v", slider6Value)
            putFloat("skin-thresh", slider7Value)
            putFloat("skin-maxval", slider8Value)
            apply()
        }
    }



    fun resetToDefault() {
        lowerSkin = defaultLowerSkin
        upperSkin = defaultUpperSkin
        thresh = defaultThresh
        maxval = defaultMaxval

        slider1Value = defaultSlider1Values[0]
        slider2Value = defaultSlider1Values[1]
        slider3Value = defaultSlider1Values[2]

        slider4Value = defaultSlider2Values[0]
        slider5Value = defaultSlider2Values[1]
        slider6Value = defaultSlider2Values[2]

        slider7Value = defaultSlider3Values[0]
        slider8Value = defaultSlider3Values[1]
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
        Imgproc.threshold(mask, threshold, thresh, maxval, Imgproc.THRESH_BINARY)

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
