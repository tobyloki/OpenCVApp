package com.hackathoners.opencvapp.Shared.Utility

import com.hackathoners.opencvapp.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class HTTP {
    companion object {
        private const val endpoint = BuildConfig.SKETCHRNN_ENDPOINT

        fun POST(path: String, input: Map<String, Any>): String? {
            val data = JSONObject(input).toString()
            Timber.i("data: $data")

            try {
                Timber.i("Sending POST request to: $endpoint$path")
                val client: OkHttpClient = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
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