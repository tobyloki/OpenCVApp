package com.hackathoners.opencvapp.Pages.Draw

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.hackathoners.opencvapp.Shared.Utility.HTTP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
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

@OptIn(DelicateCoroutinesApi::class)
class DrawViewModel : ViewModel() {
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

    private var prevpos : Point? = null
    private var sketch : Mat? = null

    // MediaPipe
    // MODEL INSTALL PAGE: (https://developers.google.com/mediapipe/solutions/vision/hand_landmarker)
    // How-to: (https://developers.google.com/mediapipe/solutions/vision/hand_landmarker/android#live-stream)
    val HAND_LANDMARKER_MODEL = "" //TODO: Install model
    private lateinit var handLandmarker: HandLandmarker

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

        // MediaPipe
        val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath(HAND_LANDMARKER_MODEL)
        val baseOptions = baseOptionsBuilder.build()

        val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            //.setMinHandDetectionConfidence( ) //TODO: Set these parameters
            //.setMinTrackingConfidence( )
            //.setMinHandPresenceConfidence( )
            //.setNumHands( )
            //.setResultListener( )
            //.setErrorListener( )
            .setRunningMode(RunningMode.VIDEO)

        val options = optionsBuilder.build()

        // Tutorial used `context` but I'm not sure what that is
        //handLandmarker = HandLandmarker.createFromOptions(context, options)
        // This works in DrawView.kt but not here:
        //handLandmarker = HandLandmarker.createFromOptions(this, options)
    }

    fun onResume() {
        Timber.i("onResume")

        loadCalibrationValues()

        getSketchRNNPrediction()
    }

    fun onPause() {
        Timber.i("onPause")
    }
    // endregion

    // region Business logic
    private fun loadCalibrationValues() {
        val sharedPref = activity?.getSharedPreferences("calibration", Activity.MODE_PRIVATE) ?: return
        lowerSkinH = sharedPref.getFloat("lower-skin-h", 0f)
        lowerSkinS = sharedPref.getFloat("lower-skin-s", 50f)
        lowerSkinV = sharedPref.getFloat("lower-skin-v", 50f)
        upperSkinH = sharedPref.getFloat("upper-skin-h", 200f)
        upperSkinS = sharedPref.getFloat("upper-skin-s", 240f)
        upperSkinV = sharedPref.getFloat("upper-skin-v", 240f)
        thresh = sharedPref.getFloat("skin-thresh", 200f)
        maxval = sharedPref.getFloat("skin-maxval", 255f)

        Timber.i("lowerSkinH: $lowerSkinH")
        Timber.i("lowerSkinS: $lowerSkinS")
        Timber.i("lowerSkinV: $lowerSkinV")
        Timber.i("upperSkinH: $upperSkinH")
        Timber.i("upperSkinS: $upperSkinS")
        Timber.i("upperSkinV: $upperSkinV")
        Timber.i("thresh: $thresh")
        Timber.i("maxval: $maxval")
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

    class SketchRNNPoint(
        val x: Double,
        val y: Double,
        val p1: Int,
        val p2: Int,
        val p3: Int
    )
    private var points = mutableListOf<SketchRNNPoint>()

    private fun getSketchRNNPrediction() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // create json object with model string and strokes array of numbers
                val input = JSONObject( mapOf(
                    "model" to "bird",
//                    "strokes" to JSONArray(listOf(1, 2, 3, 4, 5))
                ) )
                val data = async { HTTP.POST("/simple_predict_absolute", input.toString()) }.await()
                Timber.i("sketchRNN data: $data")
                if(!data.isNullOrBlank()) {
                    val json = JSONArray(data)
                    // convert to list of points
                    points = mutableListOf<SketchRNNPoint>()
                    for (i in 0 until json.length()) {
                        val point = json.getJSONArray(i)
                        points.add(
                            SketchRNNPoint(
                                point.getDouble(0) / 8,
                                point.getDouble(1) / 8,
                                point.getInt(2),
                                point.getInt(3),
                                point.getInt(4)
                            )
                        )
                    }
                }
                // print count of points and last point
                Timber.i("points count: ${points.count()}")
                Timber.i("last point: ${points.last().x}, ${points.last().y}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
            var minArea = 0.0
            var maxContour: MatOfPoint? = null
            for (contour in contours) {
                val area = Imgproc.contourArea(contour)
                if (area > minArea) {
                    minArea = area
                    maxContour = contour
                }
            }

            // initialize sketch (if not already initialized)
            if (sketch == null) {
                sketch = Mat(frame.size(), frame.type(), Scalar(0.0, 0.0, 0.0, 0.0))
            }

            // Draw a bounding box around the hand
            if (maxContour != null) {
                val area = maxContour.size().area()
                if (area > 350) {   // min area
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

                    // initialize prevpos (if not already initialized)
                    if (prevpos == null) {
                        prevpos = Point(0.0, 0.0)
                    }

                    // Draw line from previous point to current point
                    Imgproc.line(
                        sketch,
                        prevpos,
                        Point(cx.toDouble(), cy.toDouble()),
                        Scalar(0.0, 255.0, 0.0),
                        2
                    )
                    prevpos = Point(cx.toDouble(), cy.toDouble())

                    // draw text on screen
                    Imgproc.putText(
                        frame,
                        "x: $cx, y: $cy, area: $area",
                        Point(10.0, 50.0),
                        0,
                        1.0,
                        Scalar(255.0, 0.0, 0.0),
                        2
                    )
                }
            }

            // TODO: draw points on screen
            for (point in points) {
                try {
                    Imgproc.circle(
                        frame,
                        Point(point.x, point.y),
                        1,
                        Scalar(0.0, 0.0, 255.0),
                        2
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // END

            // Merge the sketch with the frame
            //   (!) Adjust alpha (0.7 in this case) as needed
            Core.addWeighted(frame, 1.0, sketch, 0.7, 0.0, frame)

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
