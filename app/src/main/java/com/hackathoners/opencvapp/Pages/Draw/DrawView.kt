package com.hackathoners.opencvapp.Pages.Draw

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.hackathoners.opencvapp.Extensions.flipBitmap
import com.hackathoners.opencvapp.Shared.Helpers.PerformOnLifecycle
import com.hackathoners.opencvapp.Shared.Models.Gesture
import com.hackathoners.opencvapp.Shared.Views.BaseView
import com.hackathoners.opencvapp.Shared.ui.theme.Background
import com.hackathoners.opencvapp.Shared.ui.theme.LightGreen
import com.hackathoners.opencvapp.rotateBitmap


class DrawView : ComponentActivity() {
    private val viewModel by viewModels<DrawViewModel>()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(this)
        setContent {
            DrawViewComposable(mode = Mode.CAMERA)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DrawViewComposable(
    previewMode: Boolean = LocalInspectionMode.current,
    activity: Activity = (LocalContext.current as? Activity) ?: Activity(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: DrawViewModel = viewModel(),
    mode: Mode = Mode.CAMERA
    ) {
    if (!previewMode) {
        PerformOnLifecycle(
            lifecycleOwner = lifecycleOwner,
            onCreate = viewModel::onCreate,
            onResume = viewModel::onResume,
            onPause = viewModel::onPause
        )
    }

    if (viewModel.showSaveAlert)
    {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icons.Filled.Send
            },
            title = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Save Artwork")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                border = BorderStroke(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                            .background(color = Background),
                        contentAlignment = Alignment.Center
                    ) {
                        viewModel.rawSketchImage?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Let AI auto generate a picture for you based on your sketch. Enter a prompt that describes your sketch.")

                    val maxChar = 5000
                    Column {
                        OutlinedTextField(
                            value = viewModel.prompt,
                            onValueChange = {
                                viewModel.prompt = it.take(maxChar)
                            },
                            label = { Text("Prompt") },
                            singleLine = true
                        )
                        Text(
                            text = "${viewModel.prompt.length} / $maxChar",
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.showSaveAlert = false
                        viewModel.saving = false
                    }) {
                    Text(text = "Dismiss")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.showSaveAlert = false
                        viewModel.confirmSaveImage()
                    }) {
                    Text(text = "Confirm")
                }
            }
        )
    }

    if (viewModel.showSavingAlert)
    {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icons.Filled.Send
            },
            title = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Generating...")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                border = BorderStroke(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                            .background(color = Background),
                        contentAlignment = Alignment.Center
                    ) {
                        viewModel.rawSketchImage?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            text = { Text(text = "The artwork is being generated. Please wait a moment.") },
            confirmButton = {}
        )
    }

    if (viewModel.showErrorAlert)
    {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icons.Filled.Send
            },
            title = { Text(text = "Error") },
            text = { Text(text = "An unexpected error occurred. Please try again later.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.showErrorAlert = false
                    }) {
                    Text(text = "Close")
                }
            }
        )
    }

    if (viewModel.showFinishedSavingAlert)
    {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icons.Filled.Send
            },
            title = { Text(text = "Artwork Created") },
            text = { Text(text = "The artwork has finished generating and is now saved in your gallery.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.showFinishedSavingAlert = false
                        viewModel.goToIndividualGalleryPage()
                    }) {
                    Text(text = "Finish")
                }
            }
        )
    }


    if (viewModel.showGestureConfirmationAlert)
    {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icons.Filled.Send
            },
            title = {
                Column {
//                    Text(text = "Gesture: ${viewModel.currentGesture}")
                    if (viewModel.currentGesture == Gesture.Thumb_Up) {
                        Text("Save artwork")
                    } else if (viewModel.currentGesture == Gesture.Closed_Fist) {
                        Text("Autocomplete Sketch")
                    } else if (viewModel.currentGesture == Gesture.Thumb_Down) {
                        Text("Clear Sketch")
                    }
                }
            },
            text = { Text(text = "Hold for ${viewModel.gestureConfirmationCounter} more seconds to confirm.") },
            confirmButton = {}
        )
    }

    BaseView(
        title = "Draw",
        navigationIcon = {
            run {
                IconButton(onClick = {
                    activity.finish()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            run {
                TextButton(
                    onClick = viewModel::saveImage,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Filled.Send, contentDescription = null, tint = LightGreen)
                    Spacer(modifier = Modifier.width(10.dp)) // Add space between icon and text
                    Text(text = "Save")
                }
            }
        }
    ) {
//        var permissionGranted by remember { mutableStateOf(false) }

        if (mode == Mode.CAMERA) {
//            if (permissionGranted) {
            if (!previewMode) {
                // https://www.youtube.com/watch?v=pPVZambOuG8&t=625s
                // https://github.com/YanneckReiss/JetpackComposeCameraXShowcase/blob/master/app/src/main/kotlin/de/yanneckreiss/cameraxtutorial/ui/features/camera/photo_capture/CameraScreen.kt
                val cameraController: LifecycleCameraController =
                    remember { LifecycleCameraController(activity) }

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth(),
                    factory = { context ->
                        PreviewView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.BLACK)
                            implementationMode =
                                PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_START
                            cameraController.cameraSelector =
                                CameraSelector.DEFAULT_FRONT_CAMERA
                        }.also { previewView ->
                            cameraController.setImageAnalysisAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                GetFrameBitmap(onFrame = { bitmap ->
                                    viewModel.handleFrame(bitmap)
                                })
                            )

                            previewView.controller = cameraController
                            cameraController.bindToLifecycle(lifecycleOwner)
                        }
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
//                    .verticalScroll(rememberScrollState()),
//                contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                        .defaultMinSize(minHeight = 300.dp) // Set the minimum height here
                    .border(
                        border = BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = viewModel.handsImage.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column {
                Column(
                    // set spacing between items
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                    ) {

                        //TODO: Dropdown menu
                        // w/ all the different sketchRNN models
                        var srnnDdmExpanded by remember { mutableStateOf(false) }
                        val srnnModels = listOf( // SketchRNN model names
                            "truck", "angel", "windmill", "bicycle", "pig", "flamingo",
                            "the_mona_lisa", "mosquito", "garden", "octopus", "cat", "face",
                            "cruise_ship", "spider", "butterfly", "crab", "bird", "pineapple",
                            "yoga", "lion", "castle", "swing_set", "sea_turtle", "catbus",
                            "lighthouse", "power_outlet", "catpig", "hand", "radio", "chair",
                            "rain", "ant", "bridge", "calendar", "basket", "parrot", "helicopter",
                            "toothpaste", "postcard", "toothbrush", "trombone", "tiger",
                            "speedboat", "bee", "passport", "firetruck", "paintbrush", "bus",
                            "barn", "couch", "swan", "snail", "stove", "sheep", "scorpion",
                            "owl", "cactus", "ambulance", "flower", "pool", "elephantpig",
                            "snowflake", "diving_board", "floweryoga", "peas", "backpack",
                            "duck", "antyoga", "strawberry", "palm_tree", "hedgehog", "skull",
                            "beeflower", "monkey", "roller_coaster", "mermaid", "dolphin",
                            "crabrabbitfacepig", "lantern", "key", "penguin", "dogbunny",
                            "brain", "squirrel", "steak", "rabbitturtle", "yogabicycle",
                            "map", "tractor", "radioface", "rabbit", "fire_hydrant", "frogsofa",
                            "crabchair", "eye", "sandwich", "frog", "kangaroo", "hedgeberry",
                            "alarm_clock", "bear", "bulldozer", "rifle", "lionsheep", "dog",
                            "book", "whale", "lobster", "monapassport", "pigsheep", "rhinoceros",
                            "elephant", "fan", "everything"
                        )
                        var srnnDdmSelectedIndex by remember { mutableIntStateOf(0) }
                        Column {

                            Box(
                                modifier = Modifier
                                    .background(color = Color.White)
                                    .padding(10.dp)
                            ) {

                                ClickableText(
                                    text = AnnotatedString("Model: ${srnnModels[srnnDdmSelectedIndex]}"),
                                    onClick = { srnnDdmExpanded = true },
                                    style = TextStyle(color = Color.Black)
                                )

                                DropdownMenu(
                                    expanded = srnnDdmExpanded,
                                    onDismissRequest = { srnnDdmExpanded = false }
                                ) {
                                    srnnModels.forEachIndexed { index, model ->
                                        DropdownMenuItem(
                                            text = { Text(model) },
                                            onClick = {
                                                srnnDdmSelectedIndex = index
                                                srnnDdmExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = viewModel::clearSketch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = "Clear Sketch")
                        }

                        Button(
                            onClick = viewModel::getSketchRNNPrediction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = "Test")
                        }
                    }

                    // region Unused
//                        if (mode == Mode.VIDEO) {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(300.dp)
//                                    .background(color = Color.Black),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                val context = LocalContext.current
//                                val resources: Resources = context.resources
//                                val exoPlayer =
//                                    ExoPlayer.Builder(context).build()
//
//                                val resId = resources.getIdentifier(
//                                    "handw",
//                                    "raw",
//                                    context.packageName
//                                )
//                                val uri =
//                                    Uri.parse("android.resource://${context.packageName}/$resId")
//                                val mediaItem = MediaItem.fromUri(uri)
//                                exoPlayer.setMediaItem(mediaItem)
//
//                                val playerView = StyledPlayerView(context)
//                                playerView.player = exoPlayer
//
//                                AndroidView(factory = { playerView }) {
//                                    exoPlayer.prepare()
//                                    exoPlayer.playWhenReady = true
//                                    exoPlayer.repeatMode =
//                                        ExoPlayer.REPEAT_MODE_ALL
//                                    // hide controls
//                                    playerView.useController = true
//
//                                    // get frame callback
//                                    exoPlayer.setVideoFrameMetadataListener { presentationTimeUs, releaseTimeNs, format, mediaFormat ->
//                                        viewModel.handleVideoFrame(
//                                            presentationTimeUs
//                                        )
//                                        // run in background
////                                                    CoroutineScope(Dispatchers.IO).launch {
////                                                    GlobalScope.launch {
////                                                        val currentFrame = getVideoFrame(
////                                                            context,
////                                                            uri,
////                                                            presentationTimeUs
////                                                        )
//
////                                                        if (currentFrame != null) {
////                                                            viewModel.handleImage(currentFrame)
////                                                        }
////                                                    }
//                                    }
//                                }
//                            }
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(300.dp)
//                                .border(
//                                    border = BorderStroke(
//                                        2.dp,
//                                        MaterialTheme.colorScheme.primary
//                                    ),
//                                    shape = MaterialTheme.shapes.medium
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Image(
//                                bitmap = viewModel.originalImage.asImageBitmap(),
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxSize()
//                            )
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(300.dp)
//                                .border(
//                                    border = BorderStroke(
//                                        2.dp,
//                                        MaterialTheme.colorScheme.primary
//                                    ),
//                                    shape = MaterialTheme.shapes.medium
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Image(
//                                bitmap = viewModel.thresholdImage.asImageBitmap(),
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxSize()
//                            )
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(300.dp)
//                                .border(
//                                    border = BorderStroke(
//                                        2.dp,
//                                        MaterialTheme.colorScheme.primary
//                                    ),
//                                    shape = MaterialTheme.shapes.medium
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Image(
//                                bitmap = viewModel.handsImage.asImageBitmap(),
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxSize()
//                            )
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(300.dp)
//                                .border(
//                                    border = BorderStroke(
//                                        2.dp,
//                                        MaterialTheme.colorScheme.primary
//                                    ),
//                                    shape = MaterialTheme.shapes.medium
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Image(
//                                bitmap = viewModel.outputImage.asImageBitmap(),
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxSize()
//                            )
//                        }
                    // endregion
                }
            }
        }

//        if (mode == Mode.CAMERA) {
//            if (!previewMode) {
//                val cameraPermissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)
//                permissionGranted = cameraPermissionState.status.isGranted
//
//                if (!permissionGranted) {
//                    NoCameraPermissionScreen(
//                        onRequestPermission = cameraPermissionState::launchPermissionRequest
//                    )
//                }
//            }
//        }
    }
}

// https://www.youtube.com/watch?v=wCADCaeS8-A
// https://github.com/YanneckReiss/JetpackComposeMLKitTutorial/blob/main/app/src/main/java/de/yanneckreiss/mlkittutorial/ui/camera/TextRecognitionAnalyzer.kt
class GetFrameBitmap(
    private val onFrame: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        val correctedBitmap: Bitmap = image
            .toBitmap()
//            .rotateBitmap(image.imageInfo.rotationDegrees)
            .rotateBitmap(-90)
            .flipBitmap(xFlip = true, yFlip = false)
        onFrame(correctedBitmap)
        image.close()
    }
}

@Preview(showBackground = true)
@Composable
fun DrawViewPreview() {
    DrawViewComposable()
}
