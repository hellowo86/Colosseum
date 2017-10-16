package com.hellowo.colosseum

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import com.google.firebase.firestore.FirebaseFirestore

import com.pixplicity.easyprefs.library.Prefs

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initPrefs()
    }

    private fun initPrefs() {
        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()
    }
}
