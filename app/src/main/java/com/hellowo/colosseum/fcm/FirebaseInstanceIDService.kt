package com.hellowo.colosseum.fcm

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService


class FirebaseInstanceIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        sendRegistrationToServer()
    }

    companion object {
        fun sendRegistrationToServer() {
            val pushToken = FirebaseInstanceId.getInstance().token
        }
    }
}