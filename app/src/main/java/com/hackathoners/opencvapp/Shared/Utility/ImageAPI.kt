package com.hackathoners.opencvapp.Shared.Utility

import android.media.Image
import okhttp3.MediaType
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


class ImageAPI {
    companion object {

        private const val endpoint = "https://clipdrop-api.co/sketch-to-image/v1/sketch-to-image"
        private const val APIKey = "69892020177e3d5c9bc27f74916085ff8572b55c7083cf4531f2a3a3c4becccc464fa7136257005833e801228bfc9a55"
        private const val contentType = "multipart/form-data"

        // save the Ai generated image to the directory
        // TODO: change the savePath
        fun saveImage(response: Response?): Boolean {
            if (response == null) return false

            val responseBody = response.body ?: return false
            val savePath = "/raw"
            try {
                val file = File(savePath)
                val inputStream = responseBody.byteStream()
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4096)
                var bytesRead: Int

                while (inputStream.read(buffer).also {bytesRead = it} != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                outputStream.close()
                inputStream.close()

                return true
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

                val prompt: String = "$name, $style"

                val fileBody = file.asRequestBody("image/*".toMediaTypeOrNull())

                    // data.toRequestBody("application/json".toMediaTypeOrNull())

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("prompt", prompt)
                    .addFormDataPart("sketch_file", file.name, fileBody)
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
                        saveImage(response)
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