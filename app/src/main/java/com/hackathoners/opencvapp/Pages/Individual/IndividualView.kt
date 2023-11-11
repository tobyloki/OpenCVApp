package com.hackathoners.opencvapp.Pages.Individual

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import timber.log.Timber

class IndividualView : ComponentActivity() {
    private val viewModel by viewModels<IndividualViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(this)
        setContent {
            IndividualViewComposable()
        }
    }
}

@Composable
fun IndividualViewComposable(
    previewMode: Boolean = LocalInspectionMode.current,
    activity: Activity = (LocalContext.current as? Activity) ?: Activity(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: IndividualViewModel = viewModel()
) {
    if (!previewMode) {
        PerformOnLifecycle(
            lifecycleOwner = lifecycleOwner,
            onCreate = viewModel::onCreate,
            onResume = viewModel::onResume,
            onPause = viewModel::onPause
        )
    }

    BaseView(
        title = "Gallery Piece",
        navigationIcon = {
            run {
                IconButton(onClick = viewModel::goToGalleryPage) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color(0xFF262626))
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 22.dp))
                    .width(335.dp)
                    .height(370.dp)
                    .shadow(elevation = 10.dp, spotColor = Color(0x40FFFFFF), ambientColor = Color(0x40FFFFFF)),
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top,
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(177, 6, 205)),
                    onClick = viewModel::goToGalleryPage,
                    modifier = Modifier
                        .shadow(
                            elevation = 20.dp,
                            spotColor = Color(0x80AEFFC5),
                            ambientColor = Color(0x80AEFFC5)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(text = "Share", color = Color.White)
                }
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(255, 64, 64)),
                    onClick = viewModel::goToGalleryPage,
                    modifier = Modifier
                        .shadow(
                            elevation = 20.dp,
                            spotColor = Color(0x80AEFFC5),
                            ambientColor = Color(0x80AEFFC5)
                        )
                ) {
                    Text(text = "Delete", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IndividualViewPreview() {
    IndividualViewComposable()
}