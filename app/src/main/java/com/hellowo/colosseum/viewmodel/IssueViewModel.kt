package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Comment
import com.hellowo.colosseum.model.Issue
import java.util.*
import kotlin.collections.ArrayList

class IssueViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val issue = MutableLiveData<Issue>()
    val comments = MutableLiveData<ArrayList<Comment>>()
    var loading = MutableLiveData<Boolean>()
    var loadingComments = MutableLiveData<Boolean>()
    var issueId: String? = null
    var lastVisibleSnapshot: DocumentSnapshot? = null
    val limit = 100L

    fun initIssue(id: String?) {
        issueId = id
        comments.value = ArrayList()
        loadIssue()
        loadComments()
    }

    private fun loadIssue() {
        loading.value = true
        db.collection("issues").document(issueId!!).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val issue = document.toObject(Issue::class.java)
                }
            }
            loading.value = false
        }
    }

    fun loadComments() {
        loadingComments.value = true
        var query = db.collection("issues").document(issueId!!).collection("comments").orderBy("dtCreated")
        lastVisibleSnapshot?.let { query = query.startAfter(it) }
        query = query.limit(limit)
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.documents.forEach {
                    comments.value?.add(it.toObject(Comment::class.java))
                }
                if(task.result.documents.isNotEmpty()) {
                    val lastSnapshot = task.result.documents.last()
                    lastVisibleSnapshot = lastSnapshot
                }
            }
            comments.value = comments.value
            loadingComments.value = false
        }
    }

    fun postComment(text: String) {
        if(text.isNotEmpty()) {
            val comment = Comment(text = text, userId = Me.value?.id, userName = Me.value?.nickName, userPushId = FirebaseInstanceId.getInstance().token,
                    dtCreated = System.currentTimeMillis())
            db.collection("issues").document(issueId!!).collection("comments").document(UUID.randomUUID().toString())
                    .set(comment).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                }
            }
        }
    }
}