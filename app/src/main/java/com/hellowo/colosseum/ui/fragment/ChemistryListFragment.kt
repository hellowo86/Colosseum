package com.hellowo.colosseum.ui.fragment

import android.animation.LayoutTransition
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.colosseum.R
import com.hellowo.colosseum.ui.activity.ChemistryActivity
import com.hellowo.colosseum.ui.adapter.ChemistryListAdapter
import com.hellowo.colosseum.utils.dpToPx
import com.hellowo.colosseum.utils.makeSlideFromBottomTransition
import com.hellowo.colosseum.viewmodel.ChemistryListViewModel
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_chemistry.*

class ChemistryListFragment : Fragment() {
    private lateinit var listViewModel: ChemistryListViewModel
    var listInitAnimation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listViewModel = ViewModelProviders.of(activity!!).get(ChemistryListViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chemistry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
        setObserver()
        listViewModel.loadCoupleList()
    }

    private fun setLayout() {
        swipeRefreshView.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        swipeRefreshView.setOnRefreshListener { listViewModel.loadCoupleList() }
        swipeRefreshView.setProgressViewOffset(true, dpToPx(context!!, 100f).toInt(), dpToPx(context!!, 200f).toInt())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ChemistryListAdapter(activity!!, listViewModel.coupleList.value!!) { couple ->
            val intent = Intent(activity, ChemistryActivity::class.java)
            intent.putExtra("coupleId", couple.id)
            startActivityForResult(intent, 1)
        }

        if(Prefs.getBoolean("favorability_tutorial", false)) {
            tutorialLy.visibility = View.GONE
        }
        tutorialLy.setOnClickListener {
            tutorialLy.visibility = View.GONE
            Prefs.putBoolean("favorability_tutorial", true)
        }
        contentLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun setObserver() {
        listViewModel.coupleList.observe(this, Observer{ list ->
            if(listInitAnimation) {
                TransitionManager.beginDelayedTransition(recyclerView, makeSlideFromBottomTransition())
                listInitAnimation = list?.size!! == 0
            }
            recyclerView.adapter.notifyDataSetChanged()
        })
        listViewModel.loading.observe(this, Observer { swipeRefreshView.isRefreshing = it as Boolean })
    }

}