package com.hellowo.colosseum.ui.fragment

import android.arch.lifecycle.Observer
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
import com.hellowo.colosseum.ui.adapter.MyChatListAdapter
import com.hellowo.colosseum.utils.dpToPx
import kotlinx.android.synthetic.main.fragment_my_chat_list.*

class MyChatListFragment : Fragment() {
    lateinit var adapter: MyChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshView.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        swipeRefreshView.setOnRefreshListener { MyChatList.loadChatList() }
        swipeRefreshView.setProgressViewOffset(true, dpToPx(context!!, 100f), dpToPx(context!!, 200f))

        adapter = MyChatListAdapter(activity!!, MyChatList.value!!) {
            val intent = Intent(activity, ChatingActivity::class.java)
            intent.putExtra("chatId", it.id)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        MyChatList.observe(this, Observer { adapter.notifyDataSetChanged() })
        MyChatList.loading.observe(this, Observer { swipeRefreshView.isRefreshing = it as Boolean })
    }
}
