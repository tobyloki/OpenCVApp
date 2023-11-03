package com.hackathoners.opencvapp.Shared.Utility

import android.media.Image
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import okio.buffer
import okio.sink
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.content.Context


class ImageAPI {
    companion object {

        private const val endpoint = "https://clipdrop-api.co/sketch-to-image/v1/sketch-to-image"
        private const val APIKey = "APIKEY"
        private const val contentType = "multipart/form-data"
        private lateinit var context: Context

        // save the Ai generated image to the directory
        fun saveImage(response: Response?): Boolean {
            if (response == null) return false

            val responseBody = response.body ?: return false
            val imageBytes = responseBody?.bytes()
            try {

                if (imageBytes != null) {
                    // Store the file in the internal storage
                    val fileName = "generated_image.png"
                    val file = File(context.filesDir, fileName) // Path
                    val outputStream = FileOutputStream(file)
                    outputStream.write(imageBytes)
                    outputStream.close()
                    return true
                } else {
                    return false
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }

        }

        // Make a POST request to the endpoint to get an AI image
        //
        fun POST(file: File, name: String, style: String): Response? {
            try {
                Timber.i("Sending POST request for generating AI Image to: $endpoint")

                val prompt = "$name, $style"
                val fileBody = file.asRequestBody("image/*".toMediaTypeOrNull())

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("prompt", prompt)
                    .addFormDataPart("sketch_file", "filename",fileBody)
                    .build()

                val client: OkHttpClient = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()


                val request = Request.Builder()
                    .url(endpoint)
                    .header("x-api-key", APIKey)
                    .header("Content-Type", contentType)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        saveImage(response) // The file should be saved in the internal storage I think
                    }
                    throw IOException("Unexpected code $response")
                    // Get response
                    return response
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
}