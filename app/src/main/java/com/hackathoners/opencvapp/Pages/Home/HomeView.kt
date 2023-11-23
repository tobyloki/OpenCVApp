package com.hackathoners.opencvapp.Pages.Home

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathoners.opencvapp.R
import com.hackathoners.opencvapp.Shared.Helpers.PerformOnLifecycle
import com.hackathoners.opencvapp.Shared.Views.BaseView
import com.hackathoners.opencvapp.Shared.Views.OverflowMenu
import kotlinx.coroutines.delay

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
    previewMode: Boolean = LocalInspectionMode.current,
    activity: Activity = (LocalContext.current as? Activity) ?: Activity(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: HomeViewModel = viewModel()
) {
    if (!previewMode) {
        PerformOnLifecycle(
            lifecycleOwner = lifecycleOwner,
            onCreate = viewModel::onCreate,
            onResume = viewModel::onResume,
            onPause = viewModel::onPause
        )
    }

    // https://fvilarino.medium.com/using-activity-result-contracts-in-jetpack-compose-14b179fb87de
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            viewModel.handleImagePickerResult(uri)
        }
    )

    var startBtnEnabled by remember { mutableStateOf(true) }
    var galleryBtnEnabled by remember { mutableStateOf(true) }
    Column(
        verticalArrangement = Arrangement.spacedBy(80.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color = Color(0xFF262626))
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Image(
            painter = painterResource(id = R.drawable.cover_image),
            contentDescription = "Cover image",
            contentScale = ContentScale.FillBounds
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LaunchedEffect(galleryBtnEnabled, startBtnEnabled) {
                    if (galleryBtnEnabled || startBtnEnabled) return@LaunchedEffect
                    else delay(1000L)
                    startBtnEnabled = true
                    galleryBtnEnabled = true
                }

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0, 178, 146), disabledContainerColor = Color(0x80AEFFC5)),
                    onClick = {
                        startBtnEnabled = false
                        galleryBtnEnabled = false
                        viewModel.goToDrawPage() },
                    enabled = startBtnEnabled,
                    modifier = Modifier
                        .shadow(
                            elevation = 20.dp,
                            spotColor = Color(0x80AEFFC5),
                            ambientColor = Color(0x80AEFFC5)
                        )
                        .width(150.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.palette),
                            contentDescription = "Cover image",
                            contentScale = ContentScale.FillBounds
                        )
                        Text(text = "Start")
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, disabledContainerColor = Color.Gray),
                    onClick = {
                        startBtnEnabled = false
                        galleryBtnEnabled = false
                        viewModel.goToGalleryPage() },
                    enabled = galleryBtnEnabled,
                    modifier = Modifier
                        .shadow(elevation = 20.dp, spotColor = Color(0x80D3D3D3), ambientColor = Color(0x80D3D3D3))
                        .width(150.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.picture),
                            contentDescription = "Cover image",
                            contentScale = ContentScale.FillBounds
                        )
                        Text(text = "Gallery", color = Color.Black)
                    }
                }
            }
        }
//        Button(
//            onClick = viewModel::goToCalibrationPage
//        ) {
//            Text(text = "Go to Calibration page")
//        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    HomeViewComposable()
}