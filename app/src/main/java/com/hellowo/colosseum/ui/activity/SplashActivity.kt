package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Me.observe(this, Observer { user ->
            if (user != null) {
                val mainIntent = Intent(this, MainActivity::class.java)
                intent.extras?.let { mainIntent.putExtras(it) }
                startActivity(mainIntent)
                finish()
            } else {
                startActivity(Intent(this, SignUpActivity::class.java))
                finish()
            }
            Me.removeObservers(this)
        })
    }
}