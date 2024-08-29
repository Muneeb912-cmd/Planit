package com.example.eventmanagement.utils


import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

object FcmUtils {

    private const val FCM_SERVER_KEY = "711227771945-sdbc4soiebp6m7au89evbf2knbu64m04.apps.googleusercontent.com"
    private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"

    private val client = OkHttpClient()

    fun sendNotification(to: String, title: String, message: String) {
        val json = JSONObject()
        json.put("to", to)
        val notification = JSONObject()
        notification.put("title", title)
        notification.put("body", message)
        json.put("notification", notification)

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(FCM_URL)
            .post(body)
            .addHeader("Authorization", "key=$FCM_SERVER_KEY")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                println(response.body?.string())
            }
        })
    }
}
