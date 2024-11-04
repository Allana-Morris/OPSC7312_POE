package za.co.varsitycollege.st10204772.opsc7312_poe

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody

object FCMSender {

    private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
    private const val CONTENT_TYPE = "application/json"
    private const val serverKey = ClientID.FCM_KEY

    private val client = OkHttpClient()

    fun sendPushNotification(fcmToken: String, title: String, body: String): ResponseBody? {
        val json = """
            {
                "to": "$fcmToken",
                "notification": {
                    "title": "$title",
                    "body": "$body"
                }
            }
        """.trimIndent()

        val requestBody = RequestBody.create(CONTENT_TYPE.toMediaType(), json)

        val request = Request.Builder()
            .url(FCM_URL)
            .post(requestBody)
            .addHeader("Authorization", "key=$serverKey")
            .addHeader("Content-Type", CONTENT_TYPE)
            .build()

        return try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body
            } else {
                // Handle error response
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}