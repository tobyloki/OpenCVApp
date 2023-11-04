package com.hackathoners.opencvapp.Pages.Home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathoners.opencvapp.Shared.Helpers.PerformOnLifecycle
import com.hackathoners.opencvapp.Shared.Views.BaseView
import com.hackathoners.opencvapp.Shared.Views.OverflowMenu

class HomeView : ComponentActivity() {
    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(this)
        setContent {
            HomeViewComposable()
        }
    }
}

@Composable
fun HomeViewComposable(
    activity: ComponentActivity = ComponentActivity(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: HomeViewModel = viewModel()
) {
    PerformOnLifecycle(
        lifecycleOwner = lifecycleOwner,
        onCreate = viewModel::onCreate,
        onResume = viewModel::onResume,
        onPause = viewModel::onPause
    )

    // https://fvilarino.medium.com/using-activity-result-contracts-in-jetpack-compose-14b179fb87de
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            viewModel.handleImagePickerResult(uri)
        }
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
        },
        actions = {
            OverflowMenu { dismissMenu ->

                DropdownMenuItem(
                    onClick = {
                        viewModel.handleBtnClick()
                        dismissMenu()
                    },
                    text = { Text(text = "Increment count") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        viewModel.goToCalibrationPage()
                        dismissMenu()
                    },
                    text = { Text(text = "Go to calibration page") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        viewModel.goToDrawPage()
                        dismissMenu()
                    },
                    text = { Text(text = "Go to draw page") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }

                )

                DropdownMenuItem(
                    onClick = {
                        viewModel.goToImagePage()
                        dismissMenu()
                    },
                    text = { Text(text = "Go to Image page") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }

                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(top = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            Text(
                text = "Count: ${viewModel.count}"
            )
            Button(
                onClick = viewModel::handleBtnClick
            ) {
                Text(text = "Click me")
            }
            Button(
                onClick = viewModel::goToDrawPage
            ) {
                Text(text = "Go to draw page")
            }
            Button (onClick = viewModel::goToImagePage) {
                Text(text = "Go to Image page")
            }
            Button(
                onClick = viewModel::goToCalibrationPage
            ) {
                Text(text = "Go to Calibration page")
            }
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(250.dp)
                    .border(
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = viewModel.image.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp)
                )
            }
            Button(
                onClick = {
                    imagePicker.launch("image/*")
                }
            ) {
                Text(text = "Select image")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    HomeViewComposable()
}