package com.hellowo.colosseum.fcm

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.hellowo.colosseum.data.Me


class FirebaseInstanceIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        sendRegistrationToServer()
    }

    companion object {
        fun sendRegistrationToServer() {
            Me.value?.id?.let {
                val pushToken = FirebaseInstanceId.getInstance().token
                FirebaseFirestore.getInstance().collection("users").document(it).update("pushToken", pushToken)
            }
        }
    }
}