package com.hackathoners.opencvapp.Pages.Draw.AIImagePage

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathoners.opencvapp.Shared.Helpers.PerformOnLifecycle

class AIImageView : ComponentActivity() {
    private val viewModel by viewModels<AIImageViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstantState: Bundle?) {
        super.onCreate(savedInstantState)
        viewModel.initialize(this)
        setContent { ImageViewPreview()  }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AIImageViewComposable(
    activity: ComponentActivity = ComponentActivity(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: AIImageViewModel = viewModel()
) {
    PerformOnLifecycle(
        lifecycleOwner = lifecycleOwner,
        onCreate = viewModel::onCreate,
        onResume = viewModel::onResume,
        onPause = viewModel::onPause)
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ImageViewPreview() {
    AIImageViewComposable()
}