package com.hellowo.colosseum.ui.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.transition.Slide
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.CardView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.distFrom
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.viewmodel.ChoiceViewModel
import kotlinx.android.synthetic.main.fragment_choice.*
import link.fls.swipestack.SwipeStack
import java.util.*


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
        adapter = SwipeStackAdapter(ArrayList())
        swipeStack.adapter = adapter
        swipeStack.setListener(object : SwipeStack.SwipeStackListener{
            override fun onViewSwipedToLeft(position: Int) {
                viewModel.interest(adapter.getItem(position), 0)
            }
            override fun onViewSwipedToRight(position: Int) {
                viewModel.interest(adapter.getItem(position), 1)
            }
            override fun onStackEmpty() {
                adapter.clear()
                viewModel.stackEmpty()
            }
        })
        swipeStack.setSwipeProgressListener(object : SwipeStack.SwipeProgressListener{
            val leftColor = resources.getDrawable(R.drawable.fill_circle_grey)
            val rightColor = resources.getDrawable(R.drawable.fill_circle_primary)
            val leftImg = resources.getDrawable(R.drawable.broken_heart)
            val rightImg = resources.getDrawable(R.drawable.heart)

            override fun onSwipeEnd(position: Int) {
                val width = swipeStack.topView.width
                val height = swipeStack.topView.height
                val circleCover = swipeStack.topView.findViewById<ImageView>(R.id.circleCover)
                val heartImg = swipeStack.topView.findViewById<ImageView>(R.id.heartImg)
                val direction = circleCover.translationX / Math.abs(circleCover.translationX)
                val animSet = AnimatorSet()
                animSet.playTogether(
                        ObjectAnimator.ofFloat(circleCover, "scaleX", circleCover.scaleX, 0f).setDuration(250),
                        ObjectAnimator.ofFloat(circleCover, "scaleY", circleCover.scaleY, 0f).setDuration(250),
                        ObjectAnimator.ofFloat(circleCover, "alpha", circleCover.alpha, 0f).setDuration(250),
                        ObjectAnimator.ofFloat(circleCover, "translationX", circleCover.translationX, direction * (width / 2).toFloat()).setDuration(250),
                        ObjectAnimator.ofFloat(circleCover, "translationY", circleCover.translationY, (height / 2).toFloat()).setDuration(250),
                        ObjectAnimator.ofFloat(heartImg, "scaleX", heartImg.scaleX, 0f).setDuration(250),
                        ObjectAnimator.ofFloat(heartImg, "scaleY", heartImg.scaleY, 0f).setDuration(250),
                        ObjectAnimator.ofFloat(heartImg, "alpha", heartImg.alpha, 0f).setDuration(250),
                        ObjectAnimator.ofFloat(heartImg, "translationX", heartImg.translationX, direction * (width / 2).toFloat()).setDuration(250),
                        ObjectAnimator.ofFloat(heartImg, "translationY", heartImg.translationY, (height / 2).toFloat()).setDuration(250))
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.start()
            }
            override fun onSwipeStart(position: Int) {}
            override fun onSwipeProgress(position: Int, progress: Float) {
                val width = swipeStack.topView.width
                val height = swipeStack.topView.height
                val circleCover = swipeStack.topView.findViewById<ImageView>(R.id.circleCover)
                val heartImg = swipeStack.topView.findViewById<ImageView>(R.id.heartImg)
                val offset = Math.abs(progress)
                if(progress > 0) {
                    circleCover.setImageDrawable(rightColor)
                    heartImg.setImageDrawable(rightImg)

                    circleCover.scaleX = progress * 10f
                    circleCover.scaleY = progress * 10f
                    circleCover.alpha = 0.8f
                    circleCover.translationX = -(1 - offset) * ( width / 2)
                    circleCover.translationY = (1 - offset) * ( height / 2)

                    heartImg.scaleX = offset * 2
                    heartImg.scaleY = offset * 2
                    heartImg.alpha = 0.8f
                    heartImg.translationX = -(1 - offset * 2) * ( width / 2)
                    heartImg.translationY = (1 - offset * 1.5f) * ( height / 2)
                }else {
                    circleCover.setImageDrawable(leftColor)
                    heartImg.setImageDrawable(leftImg)

                    circleCover.scaleX = progress * 10f
                    circleCover.scaleY = progress * 10f
                    circleCover.alpha = 0.8f
                    circleCover.translationX = (1 - offset) * ( width / 2)
                    circleCover.translationY = (1 - offset) * ( height / 2)
                    heartImg.scaleX = offset * 2
                    heartImg.scaleY = offset * 2
                    heartImg.alpha = 0.8f
                    heartImg.translationX = (1 - offset * 2) * ( width / 2)
                    heartImg.translationY = (1 - offset * 1.5f) * ( height / 2)
                }
            }
        })

        searchBtn.setOnClickListener { viewModel.startNewSearch(null) }
    }

    private fun setObserver() {
        viewModel.interestMeList.observe(this, Observer { it?.let { if(it.size > 0){

        }}})
        viewModel.newList.observe(this, Observer { it?.let { if(it.size > 0){ adapter.refresh(it) }}})
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
                    choiceLy.visibility = View.INVISIBLE
                    swipeStack.visibility = View.INVISIBLE
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
                    optionChild2.visibility = View.INVISIBLE
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
        })
    }

    private fun makeTransition(){ // VISIBLITIY 설정 전에 호출해야함
        val transition = Slide()
        transition.slideEdge = Gravity.BOTTOM
        transition.duration = 500
        transition.interpolator = FastOutSlowInInterpolator()
        TransitionManager.beginDelayedTransition(rootLy, transition)
    }

    inner class SwipeStackAdapter(private val mData: ArrayList<User>) : BaseAdapter() {
        val cal = Calendar.getInstance()

        override fun getCount(): Int {
            return mData.size
        }

        override fun getItem(position: Int): User {
            return mData[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @SuppressLint("ViewHolder", "SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val v = layoutInflater.inflate(R.layout.item_card_stack, parent, false)
            val profileImg = v.findViewById<ImageView>(R.id.profileImg)
            val nameText = v.findViewById<TextView>(R.id.nameText)
            val ageText = v.findViewById<TextView>(R.id.ageText)
            val locText = v.findViewById<TextView>(R.id.locText)
            val moreText = v.findViewById<TextView>(R.id.moreText)

            val user = mData[position]

            Glide.with(context).load(makePublicPhotoUrl(user.id)).into(profileImg)
            nameText.text = "${user.nickName}"
            try{
                locText.text = "${Math.round(distFrom(user.lat, user.lng, Me.value?.lat!!, Me.value?.lng!!) * 100) / 100.0}km"
            }catch (e: Exception){}
            ageText.text = "${cal.get(Calendar.YEAR) - user.birth + 1}"
            moreText.text = user.moreInfo
            return v
        }

        fun refresh(list: ArrayList<User>) {
            mData.addAll(list)
            notifyDataSetChanged()
        }

        fun clear() {
            mData.clear()
            notifyDataSetChanged()
        }
    }
}
