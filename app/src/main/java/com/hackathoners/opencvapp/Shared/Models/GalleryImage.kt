package com.hackathoners.opencvapp.Shared.Models

import android.graphics.Bitmap
import java.io.File
import java.io.Serializable
import java.util.Date

class GalleryImage(
    val bitmap: Bitmap,
    val file: File
) : Serializable