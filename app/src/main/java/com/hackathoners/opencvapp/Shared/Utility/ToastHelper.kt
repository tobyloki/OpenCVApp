package com.hackathoners.opencvapp.Shared.Utility

import android.content.Context
import android.widget.Toast

class ToastHelper {
    companion object {
        private var toast: Toast? = null

        fun showToast(context: Context, message: String) {
            toast?.cancel()
            toast = Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            )
            toast?.show()
        }
    }
}