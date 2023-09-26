package com.hackathoners.opencvapp.Shared.Views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hackathoners.opencvapp.Shared.ui.theme.MyApplicationTheme
import com.hackathoners.opencvapp.Shared.ui.theme.Purple80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseView(
    title: String = "Open CV App",
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    MyApplicationTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = title) },
                        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple80),
                        navigationIcon = navigationIcon,
                        actions = actions
                    )
                },
                content = {
                    Box(
                        modifier = Modifier
                            .padding(it)
                    ) {
                        content()
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BaseViewPreview() {
    BaseView {
        Text(text = "Hello World")
    }
}
