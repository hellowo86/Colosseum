package com.hellowo.colosseum.viewmodel

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Issue
import com.hellowo.colosseum.model.Thread
import java.text.SimpleDateFormat
import java.util.*


class MainViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    var loading = MutableLiveData<Boolean>()
    var issue = MutableLiveData<Issue>()
    var threadsData = MutableLiveData<ArrayList<Thread>>()
    var threadList = ArrayList<Thread>()
    var issueKey: String = "00000000"

    init {
        //refreshIssueKey()
        loadIssue()
        loadThread()
    }

    private fun loadIssue() {
        loading.value = true
        val docRef = db.collection("issue").document(issueKey)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    Log.d("MainViewModel.init()", "DocumentSnapshot data: " + document.data)
                    issue.value = document.toObject(Issue::class.java)
                } else {
                    Log.d("MainViewModel.init()", "No such document")
                }
            } else {
                Log.d("MainViewModel.init()", "get failed with ", task.exception)
            }
            loading.value = false
        }
    }

    private fun loadThread() {
        db.collection("issue").document(issueKey).collection("threads").orderBy("lastUpdated").get().addOnCompleteListener { task->
            if (task.isSuccessful) {
                for (document in task.result) {
                    Log.d("loadThread", document.id + " => " + document.data)
                    threadList.add(document.toObject(Thread::class.java))
                }
                threadsData.value = threadList
            } else {
                Log.d("loadThread", "Error getting documents: ", task.exception)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun refreshIssueKey() {
        val df = SimpleDateFormat("yyyyMMdd")
        issueKey = df.format(Date())
    }

    fun postThread(contents: String) {
        Me.value?.let {
            db.collection("issue").document(issueKey).collection("threads")
                    .add(Thread(null, it.id, it.nickName, contents, 0, null, Date()))
                    .addOnSuccessListener{ ref ->
                        Log.d("postThread", "DocumentSnapshot written with ID: " + ref.id)
                    }
        }
    }

    fun loadMoreThread() {
        Log.d("loadMoreThread", "loadMoreThread")
    }
}