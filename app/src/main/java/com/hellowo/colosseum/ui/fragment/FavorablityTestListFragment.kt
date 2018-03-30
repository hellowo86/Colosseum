package com.hellowo.colosseum.ui.fragment

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
import com.hellowo.colosseum.ui.activity.FavorabilityTestActivity
import com.hellowo.colosseum.ui.adapter.FavorabilityTestAdapter
import com.hellowo.colosseum.utils.dpToPx
import com.hellowo.colosseum.utils.makeSlideFromBottomTransition
import com.hellowo.colosseum.viewmodel.FavobalityTestListViewModel
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_favorabiliy_test.*

class FavorablityTestListFragment : Fragment() {
    private lateinit var listViewModel: FavobalityTestListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listViewModel = ViewModelProviders.of(activity!!).get(FavobalityTestListViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorabiliy_test, container, false)
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
        swipeRefreshView.setProgressViewOffset(true, dpToPx(context!!, 100f), dpToPx(context!!, 200f))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = FavorabilityTestAdapter(activity!!, listViewModel.coupleList.value!!) { couple ->
            val intent = Intent(activity, FavorabilityTestActivity::class.java)
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
    }

    private fun setObserver() {
        listViewModel.coupleList.observe(this, Observer{ list ->
            if(list != null && list.isNotEmpty()) {
                recyclerView.adapter.notifyDataSetChanged()
            }else {

            }
        })
        listViewModel.loading.observe(this, Observer { swipeRefreshView.isRefreshing = it as Boolean })
    }

}