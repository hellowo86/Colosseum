package com.hellowo.colosseum.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.colosseum.R
import com.hellowo.colosseum.viewmodel.FavobalityTestViewModel
import kotlinx.android.synthetic.main.fragment_choice.*

class FavorablityTestFragment : Fragment() {
    lateinit var viewModel: FavobalityTestViewModel

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

    }

    private fun setObserver() {
        viewModel.coupleList.observe(this, Observer{ list ->
            if(list != null && list.isNotEmpty()) {

            }else {

            }
        })
        viewModel.loading.observe(this, Observer { progressBar.visibility = if(it as Boolean) View.VISIBLE else View.GONE })
    }

}