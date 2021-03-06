package com.hellowo.colosseum.ui.fragment

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.transition.TransitionManager
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
import com.hellowo.colosseum.utils.makeSlideFromBottomTransition
import kotlinx.android.synthetic.main.fragment_my_chat_list.*

class MyChatListFragment : Fragment() {
    lateinit var adapter: MyChatListAdapter
    var listInitAnimation = true

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
        swipeRefreshView.setProgressViewOffset(true, dpToPx(context!!, 100f).toInt(), dpToPx(context!!, 200f).toInt())

        adapter = MyChatListAdapter(activity!!, MyChatList.value!!) {
            val intent = Intent(activity, ChatingActivity::class.java)
            intent.putExtra("chatId", it.id)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        MyChatList.observe(this, Observer { list ->
            if(listInitAnimation) {
                TransitionManager.beginDelayedTransition(recyclerView, makeSlideFromBottomTransition())
                listInitAnimation = list?.size!! == 0
            }
            adapter.notifyDataSetChanged()
            swipeRefreshView.isRefreshing = false
        })
        MyChatList.loading.observe(this, Observer { progressBar.visibility =
                if(it as Boolean && swipeRefreshView.isRefreshing == false) View.VISIBLE else View.GONE })
    }
}
