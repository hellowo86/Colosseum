package com.hellowo.colosseum.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.Issue
import com.hellowo.colosseum.ui.activity.UserActivity
import com.hellowo.colosseum.ui.adapter.CommentListAdapter
import com.hellowo.colosseum.ui.dialog.CommentReplyListDialog
import com.hellowo.colosseum.ui.dialog.EnterCommentDialog
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.makeSlideFromBottomTransition
import com.hellowo.colosseum.utils.startUserActivity
import com.hellowo.colosseum.viewmodel.IssueViewModel
import kotlinx.android.synthetic.main.fragment_issue.*
import java.util.*

class IssueFragment : Fragment() {
    lateinit var adapter: CommentListAdapter
    lateinit var layoutManager: LinearLayoutManager
    private lateinit var viewModel: IssueViewModel
    var listInitAnimation = true

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

        adapter = CommentListAdapter(activity!!, viewModel.comments.value!!, { item ->
            CommentReplyListDialog(viewModel.issueId!!, item, viewModel)
                    .show(activity?.supportFragmentManager, null)
        }, { likedItem ->
            viewModel.like(likedItem)
        }, { userId ->
            startUserActivity(activity!!, userId)
        }, false)
        layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(layoutManager.findLastVisibleItemPosition() == adapter.itemCount - 1) {
                    viewModel.loadComments()
                }
            }
        })

        appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if(collapsing.height + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsing)) {
                // collapsed
                topLy.animate().alpha(0f).duration = 600
            } else {
                // extended
                topLy.animate().alpha(1f).duration = 600
            }
        }

        fab.setOnClickListener {
            EnterCommentDialog{
                viewModel.postComment(it)
            }.show(activity?.supportFragmentManager, null)
        }

        viewModel.issue.observe(this, Observer { updateIssueUI(it!!) })
        viewModel.comments.observe(this, Observer { list ->
            if(listInitAnimation) {
                TransitionManager.beginDelayedTransition(recyclerView, makeSlideFromBottomTransition())
                listInitAnimation = list?.size!! == 0
            }
            adapter.notifyDataSetChanged()
        })
        viewModel.editedComment.observe(this, Observer {
            it?.let { adapter.notifyItemChanged(viewModel.comments.value?.indexOf(it) as Int) }
        })
        viewModel.loading.observe(this, Observer {  })
        viewModel.loadingComments.observe(this, Observer {
            if(it!!) progressBar.visibility = View.VISIBLE else progressBar.visibility = View.GONE
        })
    }

    private fun updateIssueUI(issue: Issue) {

    }
}