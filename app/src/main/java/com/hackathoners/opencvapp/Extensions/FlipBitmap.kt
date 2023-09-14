package com.hackathoners.opencvapp.Extensions

import android.graphics.Bitmap
import android.graphics.Matrix


fun Bitmap.flipBitmap(xFlip: Boolean, yFlip: Boolean) : Bitmap {
    val matrix = Matrix()
    matrix.postScale(
        (if (xFlip) -1 else 1).toFloat(),
        (if (yFlip) -1 else 1).toFloat(),
        this.width / 2f,
        this.height / 2f
    )
    return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
}