package com.battledawn.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.battledawn.R
import com.battledawn.presentation.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

/**
 * Firebase Cloud Messaging service for push notifications
 */
class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token: $token")
        // TODO: Send token to backend server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("Message received from: ${message.from}")

        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "Battle Dawn",
                body = notification.body ?: "",
                type = message.data["type"] ?: "general"
            )
        }
    }

    private fun showNotification(title: String, body: String, type: String) {
        val channelId = when (type) {
            "attack" -> CHANNEL_ATTACK
            "construction" -> CHANNEL_CONSTRUCTION
            "research" -> CHANNEL_RESEARCH
            "alliance" -> CHANNEL_ALLIANCE
            else -> CHANNEL_GENERAL
        }

        createNotificationChannel(channelId)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(getNotificationPriority(type))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = when (channelId) {
                CHANNEL_ATTACK -> "Attacks & Battles"
                CHANNEL_CONSTRUCTION -> "Construction"
                CHANNEL_RESEARCH -> "Research"
                CHANNEL_ALLIANCE -> "Alliance"
                else -> "General"
            }

            val importance = when (channelId) {
                CHANNEL_ATTACK -> NotificationManager.IMPORTANCE_HIGH
                else -> NotificationManager.IMPORTANCE_DEFAULT
            }

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = "Battle Dawn notifications for $name"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getNotificationPriority(type: String): Int {
        return when (type) {
            "attack" -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    companion object {
        const val CHANNEL_GENERAL = "general"
        const val CHANNEL_ATTACK = "attack"
        const val CHANNEL_CONSTRUCTION = "construction"
        const val CHANNEL_RESEARCH = "research"
        const val CHANNEL_ALLIANCE = "alliance"
    }
}
