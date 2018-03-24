package com.hellowo.colosseum.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.RemoteMessage
import com.hellowo.colosseum.R
import jp.wasabeef.glide.transformations.CropCircleTransformation
import org.json.JSONException
import org.json.JSONObject


class MessagingService : com.google.firebase.messaging.FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        if (remoteMessage?.data!!.isNotEmpty()) {
            try {
                val data = JSONObject(remoteMessage.data["data"])
                val pushType = data.getInt("pushType")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun makeChatMessageNoti(data: JSONObject) {
        val userId = data.getString("userId")
        val userName = data.getString("userName")
        val message = data.getString("message")
        val chatId = data.getString("chatId")
        var resource: Bitmap? = null
        try{
            resource = Glide.with(this).load("")
                    .asBitmap().transform(CropCircleTransformation(this)).into(100, 100).get()
        }catch (e: Exception){}

        val manager = packageManager
        val intent = manager.getLaunchIntentForPackage(packageName)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_ONE_SHOT)

        sendNotification(userName, message, resource, pendingIntent)
    }

    private fun sendNotification(subject: String, message: String, icon: Bitmap?, pIntent: PendingIntent) {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.small_logo)
                .setColor(getColor(R.color.colorPrimary))
                .setContentTitle(subject)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pIntent)

        icon?.let { notificationBuilder.setLargeIcon(it) }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notificationBuilder.build())
    }
}