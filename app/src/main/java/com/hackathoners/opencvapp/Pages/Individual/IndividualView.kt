package com.hackathoners.opencvapp.Pages.Individual

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathoners.opencvapp.Shared.Helpers.PerformOnLifecycle
import com.hackathoners.opencvapp.Shared.Views.BaseView
import com.hackathoners.opencvapp.Shared.ui.theme.Background
import timber.log.Timber
import java.io.File


class IndividualView : ComponentActivity() {
    private val viewModel by viewModels<IndividualViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(this)
        val filePath = intent.getStringExtra("FILE_PATH")
        if (filePath != null) {
            Timber.i("FILE PATH IS VALID")
            viewModel.setGalleryImage(BitmapFactory.decodeFile(filePath))
            viewModel.setFilePath(filePath)
        }

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

    if (viewModel.showDeleteAlert)
    {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icons.Filled.Delete
            },
            title = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Delete")

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
                    )
                }
            },
            text = { Text(text = "Are you sure to delete this image?") },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.showDeleteAlert = false
                    }) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteImage(viewModel.filePath)
                        viewModel.showDeleteAlert = false
                    }) {
                    Text(text = "Yes")
                }
            }
        )
    }

    if (viewModel.showErrorDeletionAlert)
    {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icons.Filled.Delete
            },
            title = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Error")

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
                    )
                }
            },
            text = { Text(text = "Something went wrong.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.showDeleteAlert = false
                        viewModel.showErrorDeletionAlert = false
                    }) {
                    Text(text = "Close")
                }
            }
        )
    }

    BaseView(
        title = "Gallery Piece",
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
//                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 22.dp))
                    .width(370.dp)
                    .height(370.dp)
                    .shadow(
                        elevation = 10.dp,
                        spotColor = Color(0x40FFFFFF),
                        ambientColor = Color(0x40FFFFFF)
                    )
                    .clip(RoundedCornerShape(size = 22.dp))

            ) {
                Image(
                    bitmap = viewModel.image.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top,
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(177, 6, 205)),
                    onClick = { viewModel.shareImage(viewModel.filePath) },
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
                    onClick = { viewModel.showDeleteAlert = true },
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

