package com.hackathoners.opencvapp.Pages.Page2

import android.annotation.SuppressLint
import android.media.ImageReader
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.hackathoners.opencvapp.Pages.Page2.NoPermissionScreen.NoPermissionScreen
import com.hackathoners.opencvapp.Shared.PerformOnLifecycle
import com.hackathoners.opencvapp.ui.theme.LightBlue
import com.hackathoners.opencvapp.ui.theme.MyApplicationTheme
import com.hackathoners.opencvapp.ui.theme.Purple80

class Page2 : ComponentActivity() {
    private val viewModel by viewModels<Page2ViewModel>()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(this)
        setContent {
            Greeting2(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun Greeting2(
    activity: ComponentActivity = ComponentActivity(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: Page2ViewModel = viewModel()
) /*: ImageReader.OnImageAvailableListener*/ {
    /*override fun onImageAvailable(reader: ImageReader) {
        viewModel.handleFrame(reader.someFrame)
        reader.acquireLatestImage().close()
    }*/

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
                    Column(
                        modifier = Modifier
                            .padding(it)
                            .background(LightBlue)
                            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

                            Column {
                                Text(
                                    text = "AI Gesture Art",
                                    fontSize = 30.sp
                                )

                                if (!cameraPermissionState.status.isGranted) {
                                    NoPermissionScreen(cameraPermissionState::launchPermissionRequest)
                                } else {
//                                    Text("has permission: ${cameraPermissionState.status.isGranted}")
                                    // create box of 500 x 500 with background black
                                    Box(
                                        modifier = Modifier
                                            .width(500.dp)
                                            .height(500.dp)
                                            .background(color = Color.Black)
                                    ) {
                                        val cameraController: LifecycleCameraController = remember { LifecycleCameraController(activity) }

                                        AndroidView(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            factory = { context ->
                                                PreviewView(context).apply {
                                                    layoutParams = LinearLayout.LayoutParams(
                                                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                                                    )
                                                    setBackgroundColor(android.graphics.Color.BLACK)
                                                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                                    scaleType = PreviewView.ScaleType.FILL_START
                                                    cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                                                }.also { previewView ->
                                                    previewView.controller = cameraController
                                                    cameraController.bindToLifecycle(lifecycleOwner)
                                                }
                                            }
                                        )
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    Greeting2()
}