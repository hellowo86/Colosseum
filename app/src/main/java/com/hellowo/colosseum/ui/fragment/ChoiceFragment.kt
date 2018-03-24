package com.hellowo.colosseum.ui.fragment

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.transition.*
import android.support.v4.app.Fragment
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.TranslateAnimation
import android.widget.BaseAdapter
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.setScale
import com.hellowo.colosseum.viewmodel.ChoiceViewModel
import kotlinx.android.synthetic.main.fragment_choice.*
import link.fls.swipestack.SwipeStack
import com.hellowo.colosseum.R


class ChoiceFragment : Fragment() {
    lateinit var viewModel: ChoiceViewModel
    lateinit var adapter: SwipeStackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(ChoiceViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
        setObserver()
        viewModel.loadInterestMeList()
    }

    private fun setLayout() {
        adapter = SwipeStackAdapter(listOf("!!"))
        swipeStack.adapter = adapter
        swipeStack.setListener(object : SwipeStack.SwipeStackListener{
            override fun onViewSwipedToLeft(position: Int) {
            }
            override fun onViewSwipedToRight(position: Int) {
            }
            override fun onStackEmpty() {
            }
        })
        swipeStack.setSwipeProgressListener(object : SwipeStack.SwipeProgressListener{
            override fun onSwipeEnd(position: Int) {
                setScale(choiceYesBtn, 1f)
                setScale(choiceNoBtn, 1f)
            }
            override fun onSwipeStart(position: Int) {}
            override fun onSwipeProgress(position: Int, progress: Float) {
                log(progress.toString())
                if(progress > 0) {
                    choiceYesBtn.scaleX = 1f + Math.abs(progress)
                    choiceYesBtn.scaleY = 1f + Math.abs(progress)
                }else {
                    choiceNoBtn.scaleX = 1f + Math.abs(progress)
                    choiceNoBtn.scaleY = 1f + Math.abs(progress)
                }
            }
        })

        searchBtn.setOnClickListener { viewModel.startNewSearch(null) }
    }

    private fun setObserver() {
        viewModel.interestMeList.observe(this, Observer { it?.let { if(it.size > 0){

        }}})
        viewModel.loading.observe(this, Observer { progressBar.visibility = if(it as Boolean) View.VISIBLE else View.GONE })
        viewModel.viewMode.observe(this, Observer {
            makeTransition()
            when(it){
                0 -> {
                    optionLy.visibility = View.VISIBLE
                    optionChild1.visibility = View.VISIBLE
                    optionChild2.visibility = View.VISIBLE
                    responseBtn.visibility = View.VISIBLE
                    searchBtn.visibility = View.VISIBLE
                    loadingLy.visibility = View.INVISIBLE
                    rippleView.visibility = View.INVISIBLE
                    loadingChild1.visibility = View.INVISIBLE
                    loadingChild2.visibility = View.INVISIBLE
                }
                1 -> {
                    optionLy.visibility = View.INVISIBLE
                    optionChild1.visibility = View.INVISIBLE
                    optionChild2.visibility = View.INVISIBLE
                    responseBtn.visibility = View.INVISIBLE
                    searchBtn.visibility = View.INVISIBLE
                    loadingLy.visibility = View.VISIBLE
                    rippleView.visibility = View.VISIBLE
                    loadingChild1.visibility = View.VISIBLE
                    loadingChild2.visibility = View.VISIBLE
                    rippleView.startRippleAnimation()
                    val anim = TranslateAnimation(0f, 0f, 0f, -15f)
                    anim.duration = 500
                    anim.repeatCount = Animation.INFINITE
                    anim.repeatMode = Animation.REVERSE
                    centerImage.startAnimation(anim)
                }
            }
        })
    }

    private fun makeTransition(){ // VISIBLITIY 설정 전에 호출해야함
        val transition = Slide()
        transition.slideEdge = Gravity.BOTTOM
        transition.duration = 500
        transition.interpolator = FastOutSlowInInterpolator()
        TransitionManager.beginDelayedTransition(rootLy, transition)
    }

    inner class SwipeStackAdapter(private val mData: List<String>) : BaseAdapter() {

        override fun getCount(): Int {
            return 10
        }

        override fun getItem(position: Int): String {
            return mData[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = layoutInflater.inflate(R.layout.item_card_stack, parent, false)
            return convertView
        }
    }
}
