package za.co.varsitycollege.st10204772.opsc7312_poe

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationClass : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body)
        }

        // Handle data payload if required
        remoteMessage.data.isNotEmpty().let {
            // Process data payload if needed
        }
    }

    fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "MessageNotifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.navbar_match) // Your notification icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =  getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel =
            NotificationChannelCompat.Builder(channelId, NotificationManager.IMPORTANCE_DEFAULT)
                .setName("Chat Notifications") // Updated to a more descriptive title
                .setDescription("Notifications for new chat messages.")
                .build()


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Register_Permissions().requestNotificationPermissions()
        }
        notificationManager.notify(0, notificationBuilder.build())
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun saveTokenToPreferences(token: String) {
        // Assuming you have a SharedPreferences instance set up
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        var userID = User().Email
        with(sharedPreferences.edit()) {
            putString("${userID}_fcm_token", token)
            apply()
        }
    }

    companion object {
        private const val TAG = "FirebaseNotification"
    }
}
