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
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.exitTransition = Explode()
        }*/
        setContentView(R.layout.activity_splash)

        Me.observe(this, Observer { user ->
            Me.removeObservers(this)
            val startIntent: Intent

            if (user != null) {
                startIntent = Intent(this, MainActivity::class.java)
                intent.extras?.let { startIntent.putExtras(it) }
                finish()
            } else {
                startIntent = Intent(this, SignInActivity::class.java)
            }

            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivity(startIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                ActivityCompat.finishAfterTransition(this)
            }else {
                startActivity(startIntent)
                finish()
            }*/
            startActivity(startIntent)
            finish()
        })
    }
}