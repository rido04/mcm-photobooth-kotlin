package com.example.photoprintapp.services

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PrintApiService {

    companion object {
        // Ganti dengan IP server kamu
        private const val BASE_URL = "http://YOUR_SERVER_IP:8000"

        private val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun printPhoto(base64Image: String, copies: Int = 1, callback: (Boolean, String) -> Unit) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("image", base64Image)
                    put("copies", copies)
                }

                val body = json.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = okhttp3.Request.Builder()
                    .url("$BASE_URL/api/print")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    callback(true, "Print berhasil! $responseBody")
                } else {
                    callback(false, "Print gagal: ${response.code} - $responseBody")
                }
            } catch (e: Exception) {
                callback(false, "Error: ${e.message}")
            }
        }.start()
    }

    fun checkStatus(callback: (Boolean, String) -> Unit) {
        Thread {
            try {
                val request = okhttp3.Request.Builder()
                    .url("$BASE_URL/api/status")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    callback(true, responseBody)
                } else {
                    callback(false, "Status check failed: ${response.code}")
                }
            } catch (e: Exception) {
                callback(false, "Error: ${e.message}")
            }
        }.start()
    }
}