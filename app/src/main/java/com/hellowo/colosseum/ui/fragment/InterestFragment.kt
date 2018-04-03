package com.hellowo.colosseum.ui.fragment

import android.animation.LayoutTransition
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
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
import com.hellowo.colosseum.utils.startUserActivity
import com.hellowo.colosseum.viewmodel.InterestViewModel
import com.pixplicity.easyprefs.library.Prefs
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.SwipeDirection
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
                responseBtn.visibility = View.VISIBLE
                peopleLy.visibility = View.VISIBLE

                val user = list[0]
                people1Ly.visibility = View.VISIBLE
                people1Name.text = user.nickName
                Glide.with(this).load(makePublicPhotoUrl(user.id)).centerCrop().into(people1Img)
                people1Ly.setOnClickListener { startUserActivity(activity!!, user.id!!) }

                if(list.size > 1) {
                    val user = list[1]
                    people2Ly.visibility = View.VISIBLE
                    people2Name.text = user.nickName
                    Glide.with(this).load(makePublicPhotoUrl(user.id)).centerCrop().into(people2Img)
                    people2Ly.setOnClickListener { startUserActivity(activity!!, user.id!!) }
                }else {
                    people2Ly.visibility = View.GONE
                }

                if(list.size > 2) {
                    val user = list[2]
                    people3Ly.visibility = View.VISIBLE
                    people3Name.text = user.nickName
                    Glide.with(this).load(makePublicPhotoUrl(user.id)).centerCrop().into(people3Img)
                    people3Ly.setOnClickListener { startUserActivity(activity!!, user.id!!) }
                }else {
                    people3Ly.visibility = View.GONE
                }

                if(list.size == 1) {
                    interestMeText.text = String.format(getString(R.string.people_interest_me_one), user.nickName)
                }else {
                    interestMeText.text = String.format(getString(R.string.people_interest_me_many), user.nickName, list.size - 1)
                }
                responseBtn.setOnClickListener {
                    viewModel.response()
                }
            }else {
                responseBtn.visibility = View.GONE
                peopleLy.visibility = View.GONE
                interestMeText.text = getString(R.string.response_empty)
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
        when(viewMode){
            0 -> {
                TransitionManager.beginDelayedTransition(rootLy, makeSlideFromBottomTransition())
                optionLy.visibility = View.VISIBLE
                loadingLy.visibility = View.INVISIBLE
                rippleView.visibility = View.INVISIBLE
                loadingChild1.visibility = View.INVISIBLE
                loadingChild2.visibility = View.INVISIBLE
                choiceLy.visibility = View.INVISIBLE
                swipeStack.visibility = View.INVISIBLE
            }
            1 -> {
                TransitionManager.beginDelayedTransition(rootLy, makeSlideFromBottomTransition())
                optionLy.visibility = View.INVISIBLE
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
            2 -> { rootLy.postDelayed({ viewModel.viewMode.value = 3 }, 2000) }
            3 -> { showChoiceLayout() }
        }
    }

    private fun showChoiceLayout() {
        TransitionManager.beginDelayedTransition(rootLy, makeSlideFromBottomTransition())
        optionLy.visibility = View.INVISIBLE
        loadingLy.visibility = View.INVISIBLE
        rippleView.visibility = View.INVISIBLE
        loadingChild1.visibility = View.INVISIBLE
        loadingChild2.visibility = View.INVISIBLE
        choiceLy.visibility = View.VISIBLE
        swipeStack.visibility = View.VISIBLE
    }
}
