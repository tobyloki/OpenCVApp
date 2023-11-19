package com.hackathoners.opencvapp.Pages.Gallery

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathoners.opencvapp.Shared.Helpers.PerformOnLifecycle
import com.hackathoners.opencvapp.Shared.Models.GalleryImage
import com.hackathoners.opencvapp.Shared.Views.BaseView
import timber.log.Timber

class GalleryView : ComponentActivity() {
    private val viewModel by viewModels<GalleryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(this)
        setContent {
            GalleryViewComposable()
        }
    }
}

@Composable
fun GalleryViewComposable(
    previewMode: Boolean = LocalInspectionMode.current,
    activity: Activity = (LocalContext.current as? Activity) ?: Activity(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: GalleryViewModel = viewModel()
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
        title = "Gallery",
        navigationIcon = {
            run {
                IconButton(onClick = {
                    // TODO: make button button go back to home page regardless of origin
                    Timber.i("Back button clicked")
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .padding(top = 20.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            if (viewModel.recentPictures.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)
                ) {
                    Text(
                        text = "Recent",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFFFFFF)
                        )
                    )

                    LazyVerticalGrid(
                        // 3 columns
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.Start),
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                    ) {
                        items(viewModel.recentPictures) {
                            GalleryImageComposable(viewModel, it)
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)
            ) {
                Text(
                    text = "All",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFFFF)
                    )
                )

                if (viewModel.allPictures.isNotEmpty()) {
                    LazyVerticalGrid(
                        // 3 columns
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.Start),
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                    ) {
                        items(viewModel.allPictures) {
                            GalleryImageComposable(viewModel, it)
                        }
                    }
                } else {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = "No images found",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFFFFFF)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GalleryImageComposable(
    viewModel: GalleryViewModel = viewModel(),
    galleryImage: GalleryImage
) {
    Button(
        onClick = {
            viewModel.goToIndividualPage(galleryImage)
        },
        // set contentPadding to 0.dp to remove the default padding
        contentPadding = PaddingValues(0.dp),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Color.White,
//            contentColor = Color.Black
//        ),
        shape = MaterialTheme.shapes.medium.copy(
//            topStart = ZeroCornerSize,
//            topEnd = ZeroCornerSize,
//            bottomStart = ZeroCornerSize,
//            bottomEnd = ZeroCornerSize
        ),
        modifier = Modifier
            .requiredWidth(100.dp)
            .requiredHeight(100.dp)
            .shadow(
                elevation = 20.dp,
                spotColor = Color(0x80FFFFFF),
                ambientColor = Color(0x80FFFFFF)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // clip to round the corners
//                .clip(MaterialTheme.shapes.medium)
//                .background(Color.Red)
        ) {
            Image(
                bitmap = galleryImage.bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GalleryViewPreview() {
    GalleryViewComposable()
}