package com.art.fartapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.art.fartapp.R
import com.art.fartapp.data.PreferencesManager
import com.art.fartapp.di.ApplicationScope
import com.art.fartapp.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = data["title"]
        val body = data["body"]
        sendNotification(title!!, body!!)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        applicationScope.launch {
            preferencesManager.updateToken(p0)
        }
    }

    private fun sendNotification(title: String, body: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val sound =
            Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.farting)

        val notificationBuilder =
            NotificationCompat.Builder(this, "fart_channel")
                .setSmallIcon(R.mipmap.ic_fart_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sound)
                .setVibrate(longArrayOf(500, 500))
                .setContentIntent(getMainActivityPendingIntent())

        notificationManager.notify(
            1,
            notificationBuilder.build()
        )
    }

    private fun getMainActivityPendingIntent() =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val sound: Uri =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.farting)

        val channel = NotificationChannel(
            "fart_channel",
            "Fart",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        channel.enableLights(true)
        channel.setSound(sound, attributes)
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)
    }
}