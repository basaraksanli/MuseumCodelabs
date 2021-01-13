package com.hms.codelabs.museum.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hms.codelabs.museum.MainActivity
import com.hms.codelabs.museum.R

object MuseumNotificationManager {
    const val CHANNEL_ID = "museumCodelabs"

    /**
     * Creation of the notification
     * Sends notification
     */
    fun sendNotification(context: Context, content: String?, label: String?) {
        val notificationLayout = RemoteViews(context.packageName, R.layout.view_notification)
        notificationLayout.setTextViewText(R.id.notification_content, content)
        val notificationLayoutExpand = RemoteViews(context.packageName, R.layout.view_notification_expand)
        notificationLayoutExpand.setTextViewText(R.id.notification_content, content)
        notificationLayoutExpand.setTextViewText(R.id.notification_museum_name, label)
        notificationLayoutExpand.setTextViewText(R.id.notification_museum_description, "Click Explore to see the details!")
        createNotificationChannel(context)
        val extra = Bundle()
        extra.putString("MuseumName", label)
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtras(extra)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpand)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setAutoCancel(true)
        NotificationManagerCompat.from(context).notify(0, builder.build())
    }

    /**
     * Creates Notification Channel
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
                    ?: return
            val name: CharSequence = "Museum Info"
            val description = "Preferential information of surrounding museums"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.description = description
            manager.createNotificationChannel(channel)
        }
    }
}