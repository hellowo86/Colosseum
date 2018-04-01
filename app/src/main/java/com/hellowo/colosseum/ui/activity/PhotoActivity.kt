package com.hellowo.colosseum.ui.activity

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.hellowo.colosseum.R
import kotlinx.android.synthetic.main.activity_photo.*
import java.lang.Exception

class PhotoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        progressBar.visibility = View.VISIBLE
        backBtn.setOnClickListener{ finish() }
        if(intent.getBooleanExtra("evaluation", false)) {
            evaluationLy.visibility = View.VISIBLE
            likeBtn.setOnClickListener {
                setResult(2)
                finish()
            }
            hateBtn.setOnClickListener {
                setResult(1)
                finish()
            }
        }else {
            evaluationLy.visibility = View.GONE
        }
    }

    private fun initObserve() {
        Glide.with(this).load(intent.getStringExtra("photoUrl")).listener(object : RequestListener<String, GlideDrawable> {
                    override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean = false
                    override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?,
                                                 isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        progressBar.visibility = View.GONE
                        photoView.setImageDrawable(resource)
                        return true
                    }}).into(photoView)
    }
}