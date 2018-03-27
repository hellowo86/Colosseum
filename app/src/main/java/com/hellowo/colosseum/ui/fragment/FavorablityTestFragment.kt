package com.hellowo.colosseum.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.transition.Slide
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.colosseum.R
import com.hellowo.colosseum.ui.activity.FavorablityTestActivity
import com.hellowo.colosseum.ui.adapter.CoupleTestAdapter
import com.hellowo.colosseum.utils.dpToPx
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.viewmodel.FavobalityTestViewModel
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_favorabiliy_test.*

class FavorablityTestFragment : Fragment() {
    private lateinit var viewModel: FavobalityTestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(FavobalityTestViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorabiliy_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
        setObserver()
        viewModel.loadCoupleList()
    }

    private fun setLayout() {
        swipeRefreshView.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        swipeRefreshView.setOnRefreshListener { viewModel.loadCoupleList() }
        swipeRefreshView.setProgressViewOffset(true, dpToPx(context!!, 100f), dpToPx(context!!, 200f))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = CoupleTestAdapter(activity!!, ArrayList()) { couple ->
            val intent = Intent(activity, FavorablityTestActivity::class.java)
            intent.putExtra("couple", couple)
            startActivityForResult(intent, 1)
        }

        if(Prefs.getBoolean("favorability_tutorial", false)) {
            tutorialLy.visibility = View.GONE
        }
        tutorialLy.setOnClickListener {
            tutorialLy.visibility = View.GONE
            Prefs.putBoolean("favorability_tutorial", true)
        }
    }

    private fun setObserver() {
        viewModel.coupleList.observe(this, Observer{ list ->
            if(list != null && list.isNotEmpty()) {
                val transition = Slide()
                transition.slideEdge = Gravity.BOTTOM
                transition.duration = 500
                transition.interpolator = FastOutSlowInInterpolator()
                TransitionManager.beginDelayedTransition(recyclerView, transition)
                (recyclerView.adapter as CoupleTestAdapter).refresh(list)
            }else {

            }
        })
        viewModel.loading.observe(this, Observer { swipeRefreshView.isRefreshing = it as Boolean })
    }

}