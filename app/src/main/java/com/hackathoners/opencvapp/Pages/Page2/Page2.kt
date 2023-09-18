package com.hackathoners.opencvapp.Pages.Page2

import android.annotation.SuppressLint
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
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
import com.hackathoners.opencvapp.Extensions.flipBitmap
import com.hackathoners.opencvapp.Pages.Page2.NoPermissionScreen.NoPermissionScreen
import com.hackathoners.opencvapp.Shared.PerformOnLifecycle
import com.hackathoners.opencvapp.rotateBitmap
import com.hackathoners.opencvapp.ui.theme.LightBlue
import com.hackathoners.opencvapp.ui.theme.MyApplicationTheme
import com.hackathoners.opencvapp.ui.theme.Purple80
import timber.log.Timber

class Page2 : ComponentActivity() {
    private val viewModel by viewModels<Page2ViewModel>()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(this)
        setContent {
            Greeting2(this)
        }

        Timber.i("on create")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun Greeting2(
    activity: ComponentActivity = ComponentActivity(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: Page2ViewModel = viewModel()
) {

    PerformOnLifecycle(
        lifecycleOwner = lifecycleOwner,
        onCreate = viewModel::onCreate,
        onResume = viewModel::onResume,
        onPause = viewModel::onPause
    )

    MyApplicationTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = "Page 2") },
                        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple80),
                        navigationIcon = {
                            run {
                                IconButton(onClick = {
                                    activity.finish()
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        }
                    )
                },
                content = {
                    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

                    if (!cameraPermissionState.status.isGranted) {
                        NoPermissionScreen(cameraPermissionState::launchPermissionRequest)
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(it)
                        ) {
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
                                            TextRecognitionAnalyzer(onFrame = { bitmap ->
                                                viewModel.handleImage(bitmap)
                                            })
                                        )

                                        previewView.controller = cameraController
                                        cameraController.bindToLifecycle(lifecycleOwner)
                                    }
                                }
                            )

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
                                            text = "AI Gesture Art",
                                            fontSize = 30.sp
                                        )

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
                                                contentDescription = "",
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
                                                contentDescription = "",
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
                                                contentDescription = "",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

// https://www.youtube.com/watch?v=wCADCaeS8-A
// https://github.com/YanneckReiss/JetpackComposeMLKitTutorial/blob/main/app/src/main/java/de/yanneckreiss/mlkittutorial/ui/camera/TextRecognitionAnalyzer.kt
class TextRecognitionAnalyzer(
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
fun GreetingPreview2() {
    Greeting2()
}