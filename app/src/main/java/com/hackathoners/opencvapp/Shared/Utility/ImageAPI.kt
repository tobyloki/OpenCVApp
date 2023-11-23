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
import com.hackathoners.opencvapp.BuildConfig
import com.hackathoners.opencvapp.R


class ImageAPI {
    companion object {
        private const val endpoint = "https://clipdrop-api.co/sketch-to-image/v1/sketch-to-image"
        private const val APIKey = BuildConfig.CLIPDROP_API_KEY

        // Make a POST request to the endpoint to get an AI image
        fun POST(file: File, name: String, style: String, completionHandler: (error: String?, data: Response?) -> Unit) {
            try {
                Timber.i("Sending POST request for generating AI Image to: $endpoint")

                val prompt = "$name"
                val fileBody = file.asRequestBody("image/*".toMediaType())

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("prompt", prompt)
                    .addFormDataPart(
                        "sketch_file",
                        file.name,
                        fileBody
                    )
                    .build()

                val client: OkHttpClient = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()


                val request = Request.Builder()
                    .url(endpoint)
                    .header("x-api-key", APIKey)
                    .header("Content-Type", "multipart/form-data")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        Timber.e("onFailure: ${e.message}")
                        e.printStackTrace()
                        completionHandler.invoke(e.message ?: "Unknown error", null)
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        Timber.i("onResponse: $response")

                        if (!response.isSuccessful) {
                            val body = response.body?.string()
                            completionHandler.invoke("Unsuccessful response: $body", null)
                        } else {
//                            val body = response.body?.string()
//                            Timber.i("response: ${response.body?.string()}")

                            completionHandler.invoke(null, response)
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                completionHandler.invoke(e.message ?: "Unknown error", null)
            }
        }

        // save the Ai generated image to the directory
        fun saveImage(path: String, imageBytes: ByteArray): String? {
            try {
                // Store the file in the internal storage
                val fileName = "generated_image-${System.currentTimeMillis()}.jpg"
                val file = File(path, fileName)
                val outputStream = FileOutputStream(file)
                outputStream.write(imageBytes)
                outputStream.close()
                return file.path
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }
    }
}