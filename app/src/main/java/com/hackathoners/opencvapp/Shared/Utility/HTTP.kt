package com.hackathoners.opencvapp.Shared.Utility

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class HTTP {
    companion object {
        private const val endpoint = "https://7d08-155-133-15-37.ngrok-free.app"

        fun GET(path: String): String? {
            try {
                Timber.i("Sending GET request to: $endpoint$path")
                val client: OkHttpClient = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder()
                    .url(endpoint + path)
                    .header("ngrok-skip-browser-warning", "anyvalue")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    // Get response body
                    return response.body?.string()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        fun POST(path: String, data: String): String? {
            try {
                Timber.i("Sending POST request to: $endpoint$path")
                val client: OkHttpClient = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
                val requestBody: RequestBody =
                    data.toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(endpoint + path)
                    .header("ngrok-skip-browser-warning", "anyvalue")
                    .post(requestBody)
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    // Get response body
                    return response.body?.string()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
}