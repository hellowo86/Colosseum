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
import com.hellowo.colosseum.model.BadgeData
import com.hellowo.colosseum.pushServerKey
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import jp.wasabeef.glide.transformations.CropCircleTransformation
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject


class MessagingService : com.google.firebase.messaging.FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        log(remoteMessage.toString())
        if (remoteMessage?.data!!.isNotEmpty()) {
            try {
                val data = JSONObject(remoteMessage.data["data"])
                val pushType = data.getInt("pushType")
                when(pushType) {
                    0 -> makeChatMessageNoti(data)
                    1 -> makeInterestCompleteNoti(data)
                    2 -> makeChemistryNoti(data)
                    3 -> makeLikeNoti(data)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun makeChatMessageNoti(data: JSONObject) {
        val chatId = data.getString("identifier")

        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { _ ->
            val badgeData = realm.where(BadgeData::class.java).equalTo("id", "chat$chatId").findFirst()
            if(badgeData != null) {
                badgeData.count++
            }else {
                val badgeData = realm.createObject(BadgeData::class.java, "chat$chatId")
                badgeData.type = "chat"
                badgeData.count = 1
            }
        }
        realm.close()

        if(Prefs.getBoolean("chatNotiSwitch", true)) {
            val userId = data.getString("userId")
            val userName = data.getString("userName")
            val message = data.getString("message")
            var resource: Bitmap? = null
            try{
                resource = Glide.with(this).load(makePublicPhotoUrl(userId))
                        .asBitmap().transform(CropCircleTransformation(this)).into(100, 100).get()
            }catch (e: Exception){}

            val manager = packageManager
            val intent = manager.getLaunchIntentForPackage(packageName)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.putExtra("chatId", chatId)
            val pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            sendNotification(userName, message, resource, pendingIntent)
        }
    }

    private fun makeInterestCompleteNoti(data: JSONObject) {
        val coupleId = data.getString("identifier")

        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { _ ->
            val badgeData = realm.where(BadgeData::class.java).equalTo("id", "chemistry$coupleId").findFirst()
            if(badgeData != null) {
                badgeData.count = 1
            }else {
                val badgeData = realm.createObject(BadgeData::class.java, "chemistry$coupleId")
                badgeData.type = "chemistry"
                badgeData.count = 1
            }
        }
        realm.close()

        if(Prefs.getBoolean("interestNotiSwitch", true)) {
            val userId = data.getString("userId")
            val userName = String.format(getString(R.string.interest_completed), data.getString("userName"))
            val message = getString(R.string.interest_completed_sub)
            var resource: Bitmap? = null
            try{
                resource = Glide.with(this).load(makePublicPhotoUrl(userId))
                        .asBitmap().transform(CropCircleTransformation(this)).into(100, 100).get()
            }catch (e: Exception){}

            val manager = packageManager
            val intent = manager.getLaunchIntentForPackage(packageName)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.putExtra("goChemistryTab", true)
            val pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            sendNotification(userName, message, resource, pendingIntent)
        }
    }

    private fun makeChemistryNoti(data: JSONObject) {
        if(Prefs.getBoolean("chemistryNotiSwitch", true)) {
            val userId = data.getString("userId")
            val userName = data.getString("userName")
            val message = data.getString("message")
            var resource: Bitmap? = null
            try{
                resource = Glide.with(this).load(makePublicPhotoUrl(userId))
                        .asBitmap().transform(CropCircleTransformation(this)).into(100, 100).get()
            }catch (e: Exception){}

            val manager = packageManager
            val intent = manager.getLaunchIntentForPackage(packageName)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            sendNotification(userName, message, resource, pendingIntent)
        }
    }

    private fun makeLikeNoti(data: JSONObject) {
        if(Prefs.getBoolean("likeNotiSwitch", true)) {
            val userId = data.getString("userId")
            val userName = data.getString("userName")
            val message = data.getString("message")
            var resource: Bitmap? = null
            try{
                resource = Glide.with(this).load(makePublicPhotoUrl(userId))
                        .asBitmap().transform(CropCircleTransformation(this)).into(100, 100).get()
            }catch (e: Exception){}

            val manager = packageManager
            val intent = manager.getLaunchIntentForPackage(packageName)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            sendNotification(userName, message, resource, pendingIntent)
        }
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

    companion object {
        fun sendPushMessage(pushToken: String, pushType: Int, userId: String, userName: String, text: String, identifier: String) {
            Thread {
                val data = JSONObject()
                data.put("pushType", pushType)
                data.put("userId", userId)
                data.put("userName", userName)
                data.put("message", text)
                data.put("identifier", identifier)

                val bodyBuilder = FormBody.Builder()
                bodyBuilder.add("to", pushToken)
                bodyBuilder.add("data", data.toString())
                val request = Request.Builder()
                        .url("https://fcm.googleapis.com/fcm/send")
                        .addHeader("Authorization", pushServerKey)
                        .post(bodyBuilder.build())
                        .build()
                val response = OkHttpClient().newCall(request).execute()
            }.start()

        }
    }
}