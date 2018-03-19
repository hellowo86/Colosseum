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
import android.view.inputmethod.EditorInfo
import com.hellowo.colosseum.R
import com.hellowo.colosseum.ui.activity.JoinChatActivity
import com.hellowo.colosseum.viewmodel.HomeViewModel
import com.hellowo.teamfinder.ui.adapter.ChatListAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    lateinit var viewModel: HomeViewModel
    lateinit var adapter: ChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLy.setOnRefreshListener { viewModel.search(searchEdit.text.toString().trim()) }

        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                swipeRefreshLy.isRefreshing = true
                viewModel.search(searchEdit.text.toString().trim())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        adapter = ChatListAdapter(context!!, viewModel.chatList.value!!) {
            val intent = Intent(context, JoinChatActivity::class.java)
            intent.putExtra("Chat", it)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewModel.loading.observe(this, Observer { swipeRefreshLy.isRefreshing = (it as Boolean) })
        viewModel.chatList.observe(this, Observer { adapter.notifyDataSetChanged() })
    }
}