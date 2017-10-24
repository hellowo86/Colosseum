package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.hellowo.colosseum.R
import com.hellowo.colosseum.ui.adapter.SwipeStackAdapter
import com.hellowo.colosseum.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.arch.lifecycle.Observer
import com.hellowo.colosseum.model.Issue
import com.hellowo.colosseum.utils.log

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        setContentView(R.layout.activity_main)
        initLayout()
        initObserve()
    }

    private fun initLayout() {

    }

    private fun initObserve() {
        viewModel.issue.observe(this, Observer { it?.let { updateIssueUI(it) } })
        viewModel.loading.observe(this, Observer { progressBar.visibility = if(it as Boolean) View.VISIBLE else View.GONE })
    }

    private fun updateIssueUI(issue: Issue) {
        titleText.text = issue.title
        issue.contents?.size?.let { log(it) }
    }
}
