package com.example.firebaseconcloud.firebase

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.firebaseconcloud.MainActivity
import com.example.firebaseconcloud.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notification = remoteMessage.notification
        if (notification != null) {
            // Mostrar notificación del sistema con datos adjuntos
            sendNotification(notification.title, notification.body)

            // Enviar a la interfaz si la app está en primer plano
            val intent = Intent("FCM_MESSAGE")
            intent.putExtra("title", notification.title)
            intent.putExtra("body", notification.body)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(title: String?, message: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", title)
            putExtra("body", message)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.pizza)
            .setContentTitle(title ?: "Notificación")
            .setContentText(message ?: "Mensaje recibido")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(1001, builder.build())
        }
    }
}