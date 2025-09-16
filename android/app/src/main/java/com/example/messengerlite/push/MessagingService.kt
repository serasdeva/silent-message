package com.example.messengerlite.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.messengerlite.MainActivity
import com.example.messengerlite.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // TODO: send to backend device endpoint
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"]
        if (type == "message") {
            val chatId = message.data["chatId"] ?: return
            val preview = message.data["preview"] ?: "New message"
            showMessageNotification(chatId, preview)
        }
    }

    private fun showMessageNotification(chatId: String, preview: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "messages"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(channelId, "Messages", NotificationManager.IMPORTANCE_HIGH))
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("mlite://chat?chatId=$chatId")
            setClass(this@MessagingService, MainActivity::class.java)
        }
        val pi = PendingIntent.getActivity(this, chatId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Messenger Lite")
            .setContentText(preview)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        nm.notify(chatId.hashCode(), notif)
    }
}

