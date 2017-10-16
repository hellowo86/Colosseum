package com.hellowo.colosseum

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.ContextWrapper

import com.pixplicity.easyprefs.library.Prefs

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
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
