package com.battledawn.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.battledawn.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Notification Manager for push notifications
 * Handles local and Firebase Cloud Messaging notifications
 */
@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID_ATTACKS = "attacks"
        private const val CHANNEL_ID_CONSTRUCTION = "construction"
        private const val CHANNEL_ID_ALLIANCE = "alliance"
        private const val CHANNEL_ID_GENERAL = "general"

        private const val NOTIFICATION_ID_ATTACK = 1001
        private const val NOTIFICATION_ID_CONSTRUCTION = 1002
        private const val NOTIFICATION_ID_ALLIANCE = 1003
        private const val NOTIFICATION_ID_GENERAL = 1004
    }

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels (Android O+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_ATTACKS,
                    "Incoming Attacks",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for incoming enemy attacks"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_ID_CONSTRUCTION,
                    "Construction & Training",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for completed construction and training"
                },
                NotificationChannel(
                    CHANNEL_ID_ALLIANCE,
                    "Alliance",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alliance messages and notifications"
                },
                NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "General game notifications"
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }

            Timber.d("Notification channels created")
        }
    }

    /**
     * Show attack notification
     */
    fun showAttackNotification(
        attackerName: String,
        colonyName: String,
        arrivalTime: String
    ) {
        val intent = Intent(context, getMainActivityClass()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "battles")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ATTACKS)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Use custom icon
            .setContentTitle("⚔️ Incoming Attack!")
            .setContentText("$attackerName is attacking $colonyName! Arrival: $arrivalTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(NOTIFICATION_ID_ATTACK, notification)
        Timber.d("Attack notification shown: $attackerName -> $colonyName")
    }

    /**
     * Show construction complete notification
     */
    fun showConstructionCompleteNotification(
        buildingName: String,
        level: Int
    ) {
        val intent = Intent(context, getMainActivityClass()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "colony")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or Intent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CONSTRUCTION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🏗️ Construction Complete")
            .setContentText("$buildingName Level $level is now complete!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_CONSTRUCTION, notification)
        Timber.d("Construction notification shown: $buildingName L$level")
    }

    /**
     * Show training complete notification
     */
    fun showTrainingCompleteNotification(
        unitType: String,
        quantity: Int
    ) {
        val intent = Intent(context, getMainActivityClass()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "colony")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or Intent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CONSTRUCTION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("👥 Training Complete")
            .setContentText("$quantity $unitType units are ready for battle!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_CONSTRUCTION, notification)
        Timber.d("Training notification shown: $quantity $unitType")
    }

    /**
     * Show alliance message notification
     */
    fun showAllianceMessageNotification(
        senderName: String,
        message: String
    ) {
        val intent = Intent(context, getMainActivityClass()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "alliance")
            putExtra("tab", "chat")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or Intent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALLIANCE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💬 $senderName")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(NOTIFICATION_ID_ALLIANCE, notification)
        Timber.d("Alliance message notification shown")
    }

    /**
     * Show achievement unlocked notification
     */
    fun showAchievementNotification(
        achievementName: String,
        icon: String
    ) {
        val intent = Intent(context, getMainActivityClass()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "achievements")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or Intent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🏆 Achievement Unlocked!")
            .setContentText("$icon $achievementName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_GENERAL, notification)
        Timber.d("Achievement notification shown: $achievementName")
    }

    /**
     * Show daily reward notification
     */
    fun showDailyRewardNotification() {
        val intent = Intent(context, getMainActivityClass()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "daily_rewards")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or Intent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🎁 Daily Reward Available!")
            .setContentText("Claim your daily reward to keep your streak!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_GENERAL, notification)
        Timber.d("Daily reward notification shown")
    }

    /**
     * Show battle result notification
     */
    fun showBattleResultNotification(
        isVictory: Boolean,
        opponentName: String
    ) {
        val intent = Intent(context, getMainActivityClass()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "battles")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or Intent.FLAG_UPDATE_CURRENT
        )

        val title = if (isVictory) "🎉 Victory!" else "💥 Defeat"
        val text = if (isVictory)
            "You defeated $opponentName!"
        else
            "You were defeated by $opponentName"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ATTACKS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_ATTACK, notification)
        Timber.d("Battle result notification shown: victory=$isVictory")
    }

    /**
     * Cancel all notifications
     */
    fun cancelAll() {
        notificationManager.cancelAll()
        Timber.d("All notifications cancelled")
    }

    /**
     * Get MainActivity class (workaround for circular dependency)
     */
    private fun getMainActivityClass(): Class<*> {
        return try {
            Class.forName("com.battledawn.MainActivity")
        } catch (e: ClassNotFoundException) {
            Timber.e(e, "MainActivity not found")
            // Return a placeholder
            Context::class.java
        }
    }
}

/**
 * FCM Service for handling push notifications
 * TODO: Add Firebase Cloud Messaging dependency
 * TODO: Extend FirebaseMessagingService
 */
/*
@AndroidEntryPoint
class BattleDawnFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Timber.d("FCM message received from: ${remoteMessage.from}")

        remoteMessage.data.let { data ->
            when (data["type"]) {
                "attack" -> {
                    notificationManager.showAttackNotification(
                        attackerName = data["attackerName"] ?: "Unknown",
                        colonyName = data["colonyName"] ?: "Your colony",
                        arrivalTime = data["arrivalTime"] ?: "Soon"
                    )
                }
                "construction_complete" -> {
                    notificationManager.showConstructionCompleteNotification(
                        buildingName = data["buildingName"] ?: "Building",
                        level = data["level"]?.toIntOrNull() ?: 1
                    )
                }
                "alliance_message" -> {
                    notificationManager.showAllianceMessageNotification(
                        senderName = data["senderName"] ?: "Alliance",
                        message = data["message"] ?: ""
                    )
                }
                else -> {
                    Timber.w("Unknown notification type: ${data["type"]}")
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token: $token")
        // TODO: Send token to backend server
    }
}
*/
