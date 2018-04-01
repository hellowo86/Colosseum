package com.hellowo.colosseum.ui.activity

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.distFrom
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_user.*
import java.util.*

class UserActivity : BaseActivity() {
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        viewModel.initUser(intent.getStringExtra("userId"))
        setContentView(R.layout.activity_user)
        initLayout()
        initObserve()
    }

    private fun initLayout() {

    }

    private fun initObserve() {
        viewModel.user.observe(this, Observer { it?.let { updateUserUI(it) } })
        viewModel.loading.observe(this, Observer { progressBar.visibility = if(it as Boolean) View.VISIBLE else View.GONE })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserUI(user: User) {
        val cal = Calendar.getInstance()
        Glide.with(this).load(makePublicPhotoUrl(user.id)).placeholder(User.getDefaultImgId(user.gender)).into(profileImg)
        nameText.text = user.nickName
        try{
            locText.text = " : ${Math.round(distFrom(user.lat, user.lng, Me.value?.lat!!, Me.value?.lng!!) * 100) / 100.0}km"
        }catch (e: Exception){}
        ageText.text = " : ${cal.get(Calendar.YEAR) - user.birth + 1}"
        moreText.text = user.moreInfo
    }
}