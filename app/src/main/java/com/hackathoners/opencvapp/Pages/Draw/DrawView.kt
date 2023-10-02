package com.hackathoners.opencvapp.Pages.Draw

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.hackathoners.opencvapp.Extensions.flipBitmap
import com.hackathoners.opencvapp.Pages.Draw.NoCameraPermissionScreen.NoCameraPermissionScreen
import com.hackathoners.opencvapp.Shared.Helpers.PerformOnLifecycle
import com.hackathoners.opencvapp.Shared.Views.BaseView
import com.hackathoners.opencvapp.Shared.ui.theme.LightBlue
import com.hackathoners.opencvapp.rotateBitmap
import timber.log.Timber


class DrawView : ComponentActivity() {
    private val viewModel by viewModels<DrawViewModel>()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(this)
        setContent {
            DrawViewComposable(this, mode = Mode.CAMERA)
        }

        Timber.i("on create")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DrawViewComposable(
    activity: ComponentActivity = ComponentActivity(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: DrawViewModel = viewModel(),
    mode: Mode = Mode.VIDEO
    ) {
    PerformOnLifecycle(
        lifecycleOwner = lifecycleOwner,
        onCreate = viewModel::onCreate,
        onResume = viewModel::onResume,
        onPause = viewModel::onPause
    )

    BaseView(
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
        }
    ) {
        val cameraPermissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)

        if (mode == Mode.CAMERA && !cameraPermissionState.status.isGranted) {
            NoCameraPermissionScreen(cameraPermissionState::launchPermissionRequest)
        } else {
            if (mode == Mode.CAMERA) {
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
                                    viewModel.handleImage(bitmap)
                                })
                            )

                            previewView.controller = cameraController
                            cameraController.bindToLifecycle(lifecycleOwner)
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBlue)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column {
                    Column(
                        // set spacing between items
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 20.dp)
                    ) {
                        Text(
                            text = "Draw",
                            fontSize = 30.sp
                        )

                        if (mode == Mode.VIDEO) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .background(color = Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                val context = LocalContext.current
                                val resources: Resources = context.resources
                                val exoPlayer =
                                    ExoPlayer.Builder(context).build()

                                val resId = resources.getIdentifier(
                                    "handw",
                                    "raw",
                                    context.packageName
                                )
                                val uri =
                                    Uri.parse("android.resource://${context.packageName}/$resId")
                                val mediaItem = MediaItem.fromUri(uri)
                                exoPlayer.setMediaItem(mediaItem)

                                val playerView = StyledPlayerView(context)
                                playerView.player = exoPlayer

                                AndroidView(factory = { playerView }) {
                                    exoPlayer.prepare()
                                    exoPlayer.playWhenReady = true
                                    exoPlayer.repeatMode =
                                        ExoPlayer.REPEAT_MODE_ALL
                                    // hide controls
                                    playerView.useController = true

                                    // get frame callback
                                    exoPlayer.setVideoFrameMetadataListener { presentationTimeUs, releaseTimeNs, format, mediaFormat ->
                                        viewModel.handleVideoFrame(
                                            presentationTimeUs
                                        )
                                        // run in background
//                                                    CoroutineScope(Dispatchers.IO).launch {
//                                                    GlobalScope.launch {
//                                                        val currentFrame = getVideoFrame(
//                                                            context,
//                                                            uri,
//                                                            presentationTimeUs
//                                                        )

//                                                        if (currentFrame != null) {
//                                                            viewModel.handleImage(currentFrame)
//                                                        }
//                                                    }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .border(
                                    border = BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                    ),
                                    shape = MaterialTheme.shapes.medium
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = viewModel.originalImage.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .border(
                                    border = BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                    ),
                                    shape = MaterialTheme.shapes.medium
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = viewModel.thresholdImage.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .border(
                                    border = BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                    ),
                                    shape = MaterialTheme.shapes.medium
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = viewModel.outputImage.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
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