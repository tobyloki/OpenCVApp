package com.hackathoners.opencvapp.Pages.Draw.AIImagePage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathoners.opencvapp.Shared.Helpers.PerformOnLifecycle

class AIImageView : ComponentActivity() {
    private val viewModel by viewModels<AIImageViewModel>()

    override fun onCreate(savedInstantState: Bundle?) {
        super.onCreate(savedInstantState)
        viewModel.initialize(this)
        setContent {  }

    }
}

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