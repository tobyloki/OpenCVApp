package com.hackathoners.opencvapp.Shared.Views

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// https://stackoverflow.com/a/68354402
@Composable
fun OverflowMenu(
    content: @Composable (dismissMenu: () -> Unit) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dismissMenu: () -> Unit = {
        showMenu = false
    }

    IconButton(onClick = {
        showMenu = !showMenu
    }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "More",
        )
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        Column {
            content(dismissMenu)
        }
    }
}