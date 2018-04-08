package com.hellowo.colosseum

import android.app.Application
import android.content.ContextWrapper
import com.hellowo.colosseum.data.ChemistryQuest
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import io.realm.RealmConfiguration

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initPrefs()
        initRealm()
        ChemistryQuest.init(this)
    }

    private fun initPrefs() {
        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()
    }

    private fun initRealm() {
        Realm.init(this)
        val config = RealmConfiguration.Builder ()
                .schemaVersion(1) // 스키마가 바뀌면 값을 올려야만 합니다
                .build()
        Realm.setDefaultConfiguration(config)
    }

}
