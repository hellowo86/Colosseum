package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Issue
import com.hellowo.colosseum.model.Thread
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.ui.adapter.ThreadListAdapter
import com.hellowo.colosseum.ui.dialog.EnterCommentDialog
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ThreadListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        setContentView(R.layout.activity_main)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        val layoutManager = LinearLayoutManager(this)
        adapter = ThreadListAdapter(this, viewModel.threadList, object: ThreadListAdapter.AdapterInterface{
            override fun onUserClicked(userId: String) {
                Log.d("onUserClicked", userId)
            }
            override fun onItemClick(thread: Thread) {
                Log.d("onItemClick", thread.toString())
            }
        })
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1) {
                    viewModel.loadMoreThread()
                }
            }
        })

        profileNameText.setOnClickListener { startActivity(Intent(this, BasicActivity::class.java)) }
        makeThreadBtn.setOnClickListener { showEnterCommentDialog() }
    }

    private fun initObserve() {
        Me.observe(this, Observer { it?.let { updateProfileUI(it) } })
        viewModel.issue.observe(this, Observer { it?.let { updateIssueUI(it) } })
        viewModel.loading.observe(this, Observer { progressBar.visibility = if(it as Boolean) View.VISIBLE else View.GONE })
        viewModel.threadsData.observe(this, Observer { adapter.notifyDataSetChanged() })
    }

    private fun updateProfileUI(user: User) {
        profileNameText.text = user.nickName
    }

    private fun updateIssueUI(issue: Issue) {
        titleText.text = issue.title
        issue.contents?.size?.let { log(it) }
    }

    private fun showEnterCommentDialog() {
        val dialog = EnterCommentDialog{ viewModel.postThread(it) }
        dialog.show(supportFragmentManager, dialog.tag)
    }
}
