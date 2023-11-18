package com.hackathoners.opencvapp.Pages.Draw

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.common.math.Quantiles.scale
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.hackathoners.opencvapp.R
import com.hackathoners.opencvapp.Shared.Helpers.GestureRecognizerHelper
import com.hackathoners.opencvapp.Shared.Utility.HTTP
import com.hackathoners.opencvapp.Shared.Utility.ImageAPI
import com.hackathoners.opencvapp.Shared.Utility.ToastHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.resize
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max

// create enum called mode with values of camera or video
enum class Mode {
    CAMERA,
    VIDEO
}

@OptIn(DelicateCoroutinesApi::class)
class DrawViewModel : ViewModel() {
    var rawSketchImage by mutableStateOf<Bitmap?>(null)
//    var originalImage by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
//    var thresholdImage by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    var handsImage by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
//    var outputImage by mutableStateOf<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

    var prompt by mutableStateOf("Sketch")

    var showSaveAlert by mutableStateOf(false)
    var showSavingAlert by mutableStateOf(false)
    var showErrorAlert by mutableStateOf(false)
    var showFinishedSavingAlert by mutableStateOf(false)

    var saving by mutableStateOf(false)

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

    // keeps track of previous point
    private var prevpos2 : Point? = null
    private var allDrawnPoints = mutableListOf<Point>()
    // mat of all lines drawn
    private var sketch2 : Mat? = null

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService
    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private var gestureRecognizerResultBundle: GestureRecognizerHelper.ResultBundle? = null

    class SketchRNNPoint(
        val x: Double,
        val y: Double,
        val p1: Int,
        val p2: Int,
        val p3: Int
    )
    private var sketchRNNPoints = mutableListOf<SketchRNNPoint>()

    // region Initialize
    @SuppressLint("StaticFieldLeak")
    private lateinit var activity: Activity
    fun initialize(activity: Activity) {
        this.activity = activity

//        val bitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.cat)
//        image = invertBitmapColors(bitmap)
    }
    // endregion

    // region Lifecycle
    @SuppressLint("DiscouragedApi")
    fun onCreate() {
        Timber.i("onCreate")

//        val intent = activity?.intent?.extras
//        value = intent?.getString("value") ?: "no value"

        // load video
        val resId = activity.resources?.getIdentifier("handw", "raw", activity?.packageName)
        Timber.i("resId: $resId")
        val uri = Uri.parse("android.resource://${activity.packageName}/$resId")
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

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Create the GestureRecognizerHelper that will handle the inference
        backgroundExecutor.execute {
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = activity.applicationContext!!,
                runningMode = RunningMode.LIVE_STREAM,
                currentDelegate = GestureRecognizerHelper.DELEGATE_CPU,
                gestureRecognizerListener = object : GestureRecognizerHelper.GestureRecognizerListener {
                    override fun onError(error: String, errorCode: Int) {
                        Timber.e("GestureRecognizerHelper error: $error, errorCode: $errorCode")
                    }

                    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
//                        Timber.i("GestureRecognizerHelper results: $resultBundle")
                        gestureRecognizerResultBundle = resultBundle
                    }
                }
            )
        }

        // Start the GestureRecognizerHelper again when users come back
        // to the foreground.
        backgroundExecutor.execute {
            if (gestureRecognizerHelper.isClosed()) {
                gestureRecognizerHelper.setupGestureRecognizer()
            }
        }
    }

    fun onPause() {
        Timber.i("onPause")

        if(this::gestureRecognizerHelper.isInitialized) {
            // Close the GestureRecognizerHelper and release resources
            backgroundExecutor.execute { gestureRecognizerHelper.clearGestureRecognizer() }
        }

        // Shut down our background executor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }
    // endregion

    // region Business logic
    private fun loadCalibrationValues() {
        val sharedPref = activity.getSharedPreferences("calibration", Activity.MODE_PRIVATE) ?: return
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

    fun getSketchRNNPrediction() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // convert allDrawnPoints to SketchRNNPoint
                val points = mutableListOf<SketchRNNPoint>()
                for (point in allDrawnPoints) {
//                    Timber.i("point: $point")
                    points.add(SketchRNNPoint(point.x, point.y, 1, 0, 0))
                }
                // convert to json array of json arrays
                val jsonArray = JSONArray()
                for (point in points) {
                    val jsonPoint = JSONArray()
                    jsonPoint.put(point.x)
                    jsonPoint.put(point.y)
                    jsonPoint.put(point.p1)
                    jsonPoint.put(point.p2)
                    jsonPoint.put(point.p3)

                    jsonArray.put(jsonPoint)
                }

                // create json object with model string and strokes array of numbers
                val input: Map<String, Any> = mapOf(
                    "model" to "bird",
                    "strokes" to jsonArray
                )
                val data = async { HTTP.POST("/simple_predict_absolute", input) }.await()
                Timber.i("sketchRNN data: $data")
                if(!data.isNullOrBlank()) {
                    val json = JSONArray(data)
                    // convert to list of points
                    sketchRNNPoints = mutableListOf()
                    for (i in 0 until json.length()) {
                        val point = json.getJSONArray(i)
                        sketchRNNPoints.add(
                            SketchRNNPoint(
                                point.getDouble(0),
                                point.getDouble(1),
                                point.getInt(2),
                                point.getInt(3),
                                point.getInt(4)
                            )
                        )
                    }
                }
                // print count of points and last point
                Timber.i("points count: ${sketchRNNPoints.count()}")
                Timber.i("last point: ${sketchRNNPoints.last().x}, ${sketchRNNPoints.last().y}")
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
                handleFrame(bitmap)
            }
        }
    }

    private var liftedFinger = true
    fun handleFrame(bitmap: Bitmap) {
        // don't do anything if saving
        if (saving) {
            return
        }

        if(this::gestureRecognizerHelper.isInitialized) {
            gestureRecognizerHelper.recognizeLiveStream(
                bitmap = bitmap
            )
        }

        coroutineScope.launch {
            // hand tracking on bitmap
            val originalFrame = Mat()
            Utils.bitmapToMat(bitmap, originalFrame)

            // region Unused
//            // Apply skin color segmentation (you may need to adjust these values)
//            val hsvFrame = Mat()
//            Imgproc.cvtColor(originalFrame, hsvFrame, Imgproc.COLOR_BGR2HSV)
//
//            val lowerSkin = Scalar(lowerSkinH.toDouble(), lowerSkinS.toDouble(), lowerSkinV.toDouble())
//            val upperSkin = Scalar(upperSkinH.toDouble(), upperSkinS.toDouble(), upperSkinV.toDouble())
//            val mask = Mat()
//            Core.inRange(hsvFrame, lowerSkin, upperSkin, mask)
//
//            // blur the mask to reduce noise
//            val blur = Mat()
//            Imgproc.blur(mask, blur, Size(2.0, 2.0))
//
//            // get threshold image
//            val thresholdFrame = Mat()
//            Imgproc.threshold(mask, thresholdFrame, thresh.toDouble(), maxval.toDouble(), Imgproc.THRESH_BINARY)
//
//            // Find contours in the mask
//            val contours = ArrayList<MatOfPoint>()
//            val hierarchy = Mat()
//            Imgproc.findContours(
//                thresholdFrame,
//                contours,
//                hierarchy,
//                Imgproc.RETR_EXTERNAL,
//                Imgproc.CHAIN_APPROX_SIMPLE
//            )
//
//            // Find the largest contour (assuming it's the hand)
//            var minArea = 0.0
//            var maxContour: MatOfPoint? = null
//            for (contour in contours) {
//                val area = Imgproc.contourArea(contour)
//                if (area > minArea) {
//                    minArea = area
//                    maxContour = contour
//                }
//            }
//
//            // Draw a bounding box around the hand
//            val drawFrame = Mat()
//            originalFrame.copyTo(drawFrame)
//
//            if (maxContour != null) {
//                val area = maxContour.size().area()
//                if (area > 350) {   // min area
//                    val boundingRect = Imgproc.boundingRect(maxContour)
//                    Imgproc.rectangle(
//                        drawFrame,
//                        boundingRect.tl(),
//                        boundingRect.br(),
//                        Scalar(0.0, 255.0, 0.0),
//                        2
//                    )
//
//                    // Compute the centroid of the largest contour
//                    val moments = Imgproc.moments(maxContour)
//                    val cx = (moments.m10 / moments.m00).toInt()
//                    val cy = (moments.m01 / moments.m00).toInt()
//
//                    // Draw a circle with the center as the centroid and the radius based on the bounding rectangle's diagonal length
////            val radius = Math.sqrt((boundingRect.width * boundingRect.width + boundingRect.height * boundingRect.height) / 2.0).toInt()
//                    Imgproc.circle(
//                        drawFrame,
//                        Point(cx.toDouble(), cy.toDouble()),
//                        10,
//                        Scalar(0.0, 255.0, 0.0),
//                        2
//                    )
//
//                    // initialize sketch (if not already initialized)
//                    if (sketch == null) {
//                        sketch = Mat(originalFrame.size(), originalFrame.type(), Scalar(0.0, 0.0, 0.0, 0.0))
//                    }
//
//                    // initialize prevpos (if not already initialized)
//                    if (prevpos == null) {
//                        prevpos = Point(0.0, 0.0)
//                    }
//
//                    // Draw line from previous point to current point
//                    Imgproc.line(
//                        sketch,
//                        prevpos,
//                        Point(cx.toDouble(), cy.toDouble()),
//                        Scalar(0.0, 255.0, 0.0),
//                        2
//                    )
//                    prevpos = Point(cx.toDouble(), cy.toDouble())
//
//                    // draw text on screen
//                    Imgproc.putText(
//                        drawFrame,
//                        "x: $cx, y: $cy, area: $area",
//                        Point(10.0, 50.0),
//                        0,
//                        1.0,
//                        Scalar(255.0, 0.0, 0.0),
//                        2
//                    )
//                }
//            }

            // endregion Unused

            // hands start

            val handsFrame = Mat()
            originalFrame.copyTo(handsFrame)

            if (gestureRecognizerResultBundle != null) {
                val gestureRecognizerResults: GestureRecognizerResult = gestureRecognizerResultBundle!!.results.first()
                val gestureCategories: List<List<Category>> = gestureRecognizerResults.gestures()
                val imageHeight: Int = gestureRecognizerResultBundle!!.inputImageHeight
                val imageWidth: Int = gestureRecognizerResultBundle!!.inputImageWidth
                val scaleFactor: Double = 1.0

                // write text on screen that says gesture
                var category: Category? = null
                if (gestureCategories.isNotEmpty()) {
                    val categories = gestureCategories.first()
                    val sortedCategories = categories.sortedByDescending { it.score() }
                    val firstCategory = sortedCategories.first()
                    category = firstCategory

                    Imgproc.putText(
                        handsFrame,
                        "${firstCategory.categoryName()} - ${String.format("%.2f", firstCategory.score())}",
                        Point(10.0, 50.0),
                        0,
                        1.0,
                        Scalar(255.0, 0.0, 0.0),
                        2
                    )
                }

                gestureRecognizerResults.let { gestureRecognizerResult ->
                    val landmarks = gestureRecognizerResult.landmarks()
//                    for (landmark in landmarks) {
                    if (landmarks.isNotEmpty()) {
                        val landmark = gestureRecognizerResult.landmarks()[0]
//                        Timber.i("count: ${landmark.count()}")
                        for (i in 0 until landmark.count()) {
                            val normalizedLandmark = landmark[i]
                            val isIndexFinger = i == 8

                            if (isIndexFinger) {
                                if (category != null && category.categoryName() == "Pointing_Up" && category.score() > 0.5) {
                                    val x = normalizedLandmark.x() * imageWidth * scaleFactor
                                    val y = normalizedLandmark.y() * imageHeight * scaleFactor
                                    val point = Point(x, y)

                                    // initialize sketch2 (if not already initialized)
                                    if (sketch2 == null) {
                                        sketch2 = Mat(originalFrame.size(), originalFrame.type(), Scalar(0.0, 0.0, 0.0, 0.0))
                                    }

                                    if (prevpos2 == null || liftedFinger) {
                                        prevpos2 = point
                                    }
                                    liftedFinger = false

                                    // Draw line from previous point to current point
                                    Imgproc.line(
                                        sketch2,
                                        prevpos2,
                                        point,
                                        Scalar(0.0, 255.0, 0.0),
                                        2
                                    )
                                    prevpos2 = point
                                    allDrawnPoints.add(point)
                                } else {
                                    liftedFinger = true
                                }
                            }

                            // draw points on screen
                            Imgproc.circle(
                                handsFrame,
                                Point(
                                    normalizedLandmark.x() * imageWidth * scaleFactor,
                                    normalizedLandmark.y() * imageHeight * scaleFactor
                                ),
                                4,
                                if (isIndexFinger) Scalar(255.0, 0.0, 0.0) else Scalar(0.0, 0.0, 255.0),
                                2
                            )
                            // add text to points of index
//                            Imgproc.putText(
//                                handsFrame,
//                                "$i",
//                                Point(
//                                    normalizedLandmark.x() * imageWidth * scaleFactor,
//                                    normalizedLandmark.y() * imageHeight * scaleFactor
//                                ),
//                                0,
//                                1.0,
//                                Scalar(0.0, 0.0, 255.0),
//                                2
//                            )

                            // draw lines on screen
                            HandLandmarker.HAND_CONNECTIONS.forEach {
                                Imgproc.line(
                                    handsFrame,
                                    Point(
                                        gestureRecognizerResult.landmarks()[0][it.start()].x() * imageWidth * scaleFactor,
                                        gestureRecognizerResult.landmarks()[0][it.start()].y() * imageHeight * scaleFactor
                                    ),
                                    Point(
                                        gestureRecognizerResult.landmarks()[0][it.end()].x() * imageWidth * scaleFactor,
                                        gestureRecognizerResult.landmarks()[0][it.end()].y() * imageHeight * scaleFactor
                                    ),
                                    Scalar(0.0, 0.0, 255.0),
                                    2
                                )
                            }
                        }
                    }
                }
            }

            // hands end

            // Merge the sketch with the frame
            //   (!) Adjust alpha (0.7 in this case) as needed
//            if (sketch != null) {
//                Core.addWeighted(drawFrame, 1.0, sketch, 0.7, 0.0, drawFrame)
//            }
            if (sketch2 != null) {
                try {
                    // add to handsFrame
                    Core.addWeighted(handsFrame, 1.0, sketch2, 0.7, 0.0, handsFrame)

                    // make copy of handsFrame, but set the original handsFrame alpha to 0.01 (needed otherwise image won't get correctly resized when saving)
                    val sketchOnlyFrame = Mat()
                    handsFrame.copyTo(sketchOnlyFrame)

                    // add sketch2 to sketchOnlyFrame w/alpha of 0.01
                    Core.addWeighted(sketchOnlyFrame, 0.01, sketch2, 0.7, 0.0, sketchOnlyFrame)

                    // copy sketchOnlyFrame to rawSketchImage
                    val rawSketchBitmap: Bitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(sketchOnlyFrame, rawSketchBitmap)

                    rawSketchImage = rawSketchBitmap
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // draw sketch rnn points on screen
            var lastPoint: SketchRNNPoint? = null
            for (point in sketchRNNPoints) {
                if (lastPoint == null) {
                    lastPoint = point
                    continue
                }
                lastPoint.let { lastPoint ->
                    Imgproc.line(
                        handsFrame,
                        Point(lastPoint.x, lastPoint.y),
                        Point(point.x, point.y),
                        Scalar(0.0, 0.0, 255.0),
                        2
                    )
                }
                lastPoint = point
            }

//            val thresholdBitmap: Bitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//            Utils.matToBitmap(thresholdFrame, thresholdBitmap)
            val handsBitmap: Bitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(handsFrame, handsBitmap)
//            val drawBitmap: Bitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//            Utils.matToBitmap(drawFrame, drawBitmap)

            // run on UI thread
            activity.runOnUiThread {
                //                originalImage = bitmap
                //                thresholdImage = thresholdBitmap
                handsImage = handsBitmap
                //                outputImage = drawBitmap
            }
        }
    }

    fun clearSketch() {
        prevpos2 = null
        allDrawnPoints = mutableListOf()
        sketch2 = null
        rawSketchImage = null
        sketchRNNPoints = mutableListOf()
    }
    // endregion

    // region Menu actions
    fun saveImage() {
        // if rawSketchImage is not already initialized, show toast
        if (rawSketchImage == null) {
            ToastHelper.showToast(activity, "No sketch to save")
        } else {
            saving = true

            // wait for 100ms to let handleFrame() finish up
            GlobalScope.launch(Dispatchers.IO) {
                delay(100)
                activity.runOnUiThread {
                    val bwSketchImage = Mat()
                    Utils.bitmapToMat(rawSketchImage, bwSketchImage)

                    // convert to grayscale
                    Imgproc.cvtColor(bwSketchImage, bwSketchImage, Imgproc.COLOR_BGR2GRAY)

                    // make anything b/t 10-255 white, and everything else black
                    Imgproc.threshold(
                        bwSketchImage,
                        bwSketchImage,
                        10.0,
                        255.0,
                        Imgproc.THRESH_BINARY
                    )

                    // flip colors
                    Core.bitwise_not(bwSketchImage, bwSketchImage)

                    // save back to rawSketchImage
                    Utils.matToBitmap(bwSketchImage, rawSketchImage)

                    showSaveAlert = true
                }
            }
        }
    }

    fun confirmSaveImage() {
        showSavingAlert = true

        // get path to Pictures folder
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        val path = "$root/OpenCVApp"
        // make directory if not exists
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        // create file
        val file = File(path, "temp.jpg")

        // resize handsImage to fit into 1024x1024 without losing aspect ratio
        // makes image smaller to fit into the resize
        // black bars will show up for images that are not square
        fun resizeBitmapToSquare(bitmap: Bitmap, newSize: Int): Bitmap {
            val width = bitmap.width
            val height = bitmap.height

            val scale = newSize.toFloat() / max(width, height)

            val scaledWidth = scale * width
            val scaledHeight = scale * height

            val left = (newSize - scaledWidth) / 2
            val top = (newSize - scaledHeight) / 2

            val newBitmap = Bitmap.createBitmap(newSize, newSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(newBitmap)

            val paint = Paint()
            canvas.drawBitmap(bitmap, null, RectF(left, top, left + scaledWidth, top + scaledHeight), paint)

            Timber.i("Original Dimensions: ${bitmap.width} x ${bitmap.height}")
            Timber.i("Resized Dimensions: ${newBitmap.width} x ${newBitmap.height}")

            return newBitmap
        }

        val resizedBitmap: Bitmap = resizeBitmapToSquare(rawSketchImage!!, 1024)

        // write the bytes in file
        val fos = FileOutputStream(file)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()

        prompt = prompt.trim()
        prompt = prompt.ifEmpty {
            "Sketch"
        }

//        saving = false
//        showSavingAlert = false

        ImageAPI.POST(file, prompt, "cinematic") { error, response ->
            activity.runOnUiThread {
                saving = false
                showSavingAlert = false

                // delete temp file
//                file.delete()

                if (error != null) {
                    Timber.e("error: $error")
                    showErrorAlert = true
                } else if (response != null) {
                    response.body?.bytes()?.let {
                        ImageAPI.saveImage(path, it)
                    }
                    showFinishedSavingAlert = true
                }
            }
        }
    }

    fun goToIndividualGalleryPage() {
        // TODO: implement
    }
    // endregion
}
