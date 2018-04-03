package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.iid.FirebaseInstanceId
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Comment
import com.hellowo.colosseum.model.Issue
import com.hellowo.colosseum.utils.log
import java.util.*
import kotlin.collections.ArrayList


class IssueViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val issue = MutableLiveData<Issue>()
    val comments = MutableLiveData<ArrayList<Comment>>()
    val top3Comments = MutableLiveData<ArrayList<Comment>>()
    val editedComment = MutableLiveData<Comment>()
    var loading = MutableLiveData<Boolean>()
    var loadingComments = MutableLiveData<Boolean>()
    var issueId: String? = null
    var lastVisibleSnapshot: DocumentSnapshot? = null
    val limit = 50L

    fun initIssue(id: String?) {
        loadingComments.value = false
        issueId = id
        comments.value = ArrayList()
        top3Comments.value = ArrayList()
        loadIssue()
        loadComments()
        loadTop3Comments()
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

    private fun loadTop3Comments() {
        loading.value = true
        db.collection("issues").document(issueId!!).collection("comments")
                .orderBy("likeCount", Query.Direction.DESCENDING).limit(3).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.documents.forEach {
                    val comment = it.toObject(Comment::class.java)
                    if(comment.likeCount > 0) {
                        log("top3" + comment.toString())
                        top3Comments.value?.add(comment)
                    }
                }
            }
            top3Comments.value = top3Comments.value
            loading.value = false
        }
    }

    fun loadComments() {
        if(!loadingComments.value!!) {
            log("loadComments")
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
    }

    fun postComment(text: String) {
        if(text.isNotEmpty()) {
            val comment = Comment(id = UUID.randomUUID().toString(),
                    text = text,
                    userId = Me.value?.id,
                    userName = Me.value?.nickName,
                    userPushId = FirebaseInstanceId.getInstance().token,
                    dtCreated = System.currentTimeMillis())
            db.collection("issues").document(issueId!!).collection("comments").document(comment.id!!)
                    .set(comment).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadComments()
                }
            }
        }
    }

    fun like(item: Comment) {
        val ref = db.collection("issues").document(issueId!!).collection("comments")
                .document(item.id!!)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val comment = snapshot.toObject(Comment::class.java)
            val like = comment.like
            if (like.contains(Me.value?.id)) {
                like.remove(Me.value?.id)
            } else {
                like.put(Me.value?.id!!, true)
            }
            item.like = like
            item.likeCount = like.size
            transaction.update(ref, "like", like)
            transaction.update(ref, "likeCount", like.size)
        }
        .addOnSuccessListener { result -> editedComment.value = item }
        .addOnFailureListener { e ->  }
    }

    fun addedReply(item: Comment) {
        val ref = db.collection("issues").document(issueId!!).collection("comments")
                .document(item.id!!)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val comment = snapshot.toObject(Comment::class.java)
            val replyCount = comment.replyCount + 1
            item.replyCount = replyCount
            transaction.update(ref, "replyCount", replyCount)
        }
        .addOnSuccessListener { result -> editedComment.value = item }
        .addOnFailureListener { e ->  }
    }
}