package com.hellowo.colosseum.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.transition.Slide
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.ui.adapter.SwipeStackAdapter
import com.hellowo.colosseum.ui.dialog.InterestCompletedDialog
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.utils.makeSlideFromBottomTransition
import com.hellowo.colosseum.viewmodel.InterestViewModel
import com.pixplicity.easyprefs.library.Prefs
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.SwipeDirection
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_interest.*


class InterestFragment : Fragment() {
    private lateinit var viewModel: InterestViewModel
    private lateinit var adapter: SwipeStackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(InterestViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_interest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
        setObserver()
        viewModel.loadInterestMeList()
    }

    private fun setLayout() {
        adapter = SwipeStackAdapter(activity!!)
        swipeStack.setAdapter(adapter)
        swipeStack.setCardEventListener(object : CardStackView.CardEventListener{
            override fun onCardDragging(percentX: Float, percentY: Float) {}
            override fun onCardSwiped(direction: SwipeDirection?) {
                val user = adapter.getItem(swipeStack.topIndex - 1)
                when(direction) {
                    SwipeDirection.Right -> { viewModel.interest(user, 1) }
                    SwipeDirection.Left ->{ viewModel.interest(user, 0) }
                    else -> {}
                }
                if(swipeStack.topIndex == adapter.count) {
                    swipeStack.visibility = View.INVISIBLE
                    viewModel.stackEmpty()
                }
            }
            override fun onCardReversed() {}
            override fun onCardMovedToOrigin() {}
            override fun onCardClicked(index: Int) {}
        })

        searchBtn.setOnClickListener { viewModel.startNewSearch(null) }

        if(Prefs.getBoolean("interest_tutorial", false)) {
            tutorialLy.visibility = View.GONE
        }
        tutorialLy.setOnClickListener {
            tutorialLy.visibility = View.GONE
            Prefs.putBoolean("interest_tutorial", true)
        }
    }

    private fun setObserver() {
        viewModel.interestMeList.observe(this, Observer { list ->
            if(list != null && list.isNotEmpty()) {
                responseBtn.isEnabled = true
                responseBtn.alpha = 1f
                val user = list[0]
                Glide.with(this).load(makePublicPhotoUrl(user.id)).bitmapTransform(CropCircleTransformation(context))
                        .placeholder(User.getDefaultImgId(user.gender)).into(responseProfileImg)
                responseText.text = String.format(getString(R.string.response_btn_text), user.nickName, list.size)
                responseBtn.setOnClickListener {

                }
            }else {
                responseBtn.isEnabled = false
                responseBtn.alpha = 0.5f
                responseText.text = getString(R.string.response_empty)
            }
        })
        viewModel.newList.observe(this, Observer { it?.let { if(it.size > 0){
            adapter.clear()
            adapter.addAll(it)
            adapter.notifyDataSetChanged()
        }}})
        viewModel.loading.observe(this, Observer { progressBar.visibility = if(it as Boolean) View.VISIBLE else View.GONE })
        viewModel.viewMode.observe(this, Observer { updateUI(it) })
        viewModel.interestCompleted.observe(this, Observer { it?.let {
            InterestCompletedDialog(activity!!, it).showDialog(true, true, true, false)
        }})
    }

    private fun updateUI(viewMode: Int?) {
        TransitionManager.beginDelayedTransition(rootLy, makeSlideFromBottomTransition())
        when(viewMode){
            0 -> {
                optionLy.visibility = View.VISIBLE
                optionChild1.visibility = View.VISIBLE
                if(!Prefs.getBoolean("interest_tutorial", false)) {
                    tutorialLy.visibility = View.VISIBLE
                }
                responseBtn.visibility = View.VISIBLE
                searchBtn.visibility = View.VISIBLE
                loadingLy.visibility = View.INVISIBLE
                rippleView.visibility = View.INVISIBLE
                loadingChild1.visibility = View.INVISIBLE
                loadingChild2.visibility = View.INVISIBLE
                choiceLy.visibility = View.INVISIBLE
                swipeStack.visibility = View.INVISIBLE
            }
            1 -> {
                optionLy.visibility = View.INVISIBLE
                optionChild1.visibility = View.INVISIBLE
                if(!Prefs.getBoolean("interest_tutorial", false)) {
                    tutorialLy.visibility = View.INVISIBLE
                }
                responseBtn.visibility = View.INVISIBLE
                searchBtn.visibility = View.INVISIBLE
                loadingLy.visibility = View.VISIBLE
                rippleView.visibility = View.VISIBLE
                loadingChild1.visibility = View.VISIBLE
                loadingChild2.visibility = View.VISIBLE
                choiceLy.visibility = View.INVISIBLE
                swipeStack.visibility = View.INVISIBLE
                rippleView.startRippleAnimation()
                val anim = TranslateAnimation(0f, 0f, 0f, -15f)
                anim.duration = 500
                anim.repeatCount = Animation.INFINITE
                anim.repeatMode = Animation.REVERSE
                centerImage.startAnimation(anim)
            }
            2 -> {
                optionLy.visibility = View.INVISIBLE
                optionChild1.visibility = View.INVISIBLE
                if(!Prefs.getBoolean("interest_tutorial", false)) {
                    tutorialLy.visibility = View.INVISIBLE
                }
                responseBtn.visibility = View.INVISIBLE
                searchBtn.visibility = View.INVISIBLE
                loadingLy.visibility = View.INVISIBLE
                rippleView.visibility = View.INVISIBLE
                loadingChild1.visibility = View.INVISIBLE
                loadingChild2.visibility = View.INVISIBLE
                choiceLy.visibility = View.VISIBLE
                swipeStack.visibility = View.VISIBLE
            }
        }
    }
}
