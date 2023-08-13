package com.hackathoners.opencvapp.Shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

// https://www.youtube.com/watch?v=SR2vGdLgmRE&list=PLLgF5xrxeQQ27eka94cugwjAXbD17O2iz&index=5&pp=iAQB
// https://github.com/the-android-factory/OneQuote/blob/episode_5/app/src/main/java/com/androidfactory/onequote/LifecycleHelper.kt
@Composable
fun PerformOnLifecycle(
    lifecycleOwner: LifecycleOwner,
    onCreate: () -> Unit = { },
    onResume: () -> Unit = { },
    onPause: () -> Unit = { }
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onCreate()
                Lifecycle.Event.ON_RESUME -> onResume()
                Lifecycle.Event.ON_PAUSE -> onPause()
                else -> {
//                    Timber.i("Lifecycle: " + event.name)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        return@DisposableEffect onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}