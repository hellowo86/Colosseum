package com.hellowo.colosseum.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.MyChatList
import com.hellowo.colosseum.ui.activity.ChatingActivity
import com.hellowo.colosseum.ui.adapter.CommentListAdapter
import com.hellowo.colosseum.ui.adapter.MyChatListAdapter
import com.hellowo.colosseum.ui.dialog.EnterCommentDialog
import com.hellowo.colosseum.utils.dpToPx
import com.hellowo.colosseum.viewmodel.IssueViewModel
import kotlinx.android.synthetic.main.fragment_issue.*
import java.util.*

class IssueFragment : Fragment() {
    lateinit var adapter: CommentListAdapter
    private lateinit var viewModel: IssueViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(IssueViewModel::class.java)
        val cal = Calendar.getInstance()
        cal.get(Calendar.DAY_OF_YEAR)
        viewModel.initIssue("0")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_issue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CommentListAdapter(activity!!, viewModel.comments.value!!) {
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            EnterCommentDialog{
                viewModel.postComment(it)
            }.show(activity?.supportFragmentManager, null)
        }

        viewModel.issue.observe(this, Observer {  })
        viewModel.comments.observe(this, Observer { adapter.notifyDataSetChanged() })
        viewModel.loading.observe(this, Observer {  })
    }
}