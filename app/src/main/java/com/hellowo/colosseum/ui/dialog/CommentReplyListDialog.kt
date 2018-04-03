package com.hellowo.colosseum.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Comment
import com.hellowo.colosseum.ui.activity.UserActivity
import com.hellowo.colosseum.ui.adapter.CommentListAdapter
import com.hellowo.colosseum.utils.makeMessageLastTimeText
import com.hellowo.colosseum.utils.makePublicPhotoUrl
import com.hellowo.colosseum.utils.startUserActivity
import com.hellowo.colosseum.viewmodel.IssueViewModel
import jp.wasabeef.glide.transformations.CropCircleTransformation
import java.util.*


@SuppressLint("ValidFragment")
class CommentReplyListDialog(private val issueId: String, private val comment : Comment,
                             private val viewModel: IssueViewModel) : BottomSheetDialog() {
    lateinit var profileImage: ImageView
    lateinit var nameText: TextView
    lateinit var activeTimeText: TextView
    lateinit var messageText: TextView
    lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var replyBtn: TextView
    val replyList = ArrayList<Comment>()
    val ref = FirebaseFirestore.getInstance().collection("issues").document(issueId)
            .collection("comments").document(comment.id!!).collection("comments")

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_comment_reply_list, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)

            profileImage = contentView.findViewById(R.id.profileImage)
            nameText = contentView.findViewById(R.id.nameText)
            activeTimeText = contentView.findViewById(R.id.activeTimeText)
            messageText = contentView.findViewById(R.id.messageText)
            recyclerView = contentView.findViewById(R.id.recyclerView)
            progressBar = contentView.findViewById(R.id.progressBar)
            replyBtn = contentView.findViewById(R.id.replyBtn)

            nameText.text = comment.userName
            messageText.text = comment.text
            activeTimeText.text = makeMessageLastTimeText(context!!, comment.dtCreated)

            Glide.with(context)
                    .load(makePublicPhotoUrl(comment.userId))
                    .bitmapTransform(CropCircleTransformation(context))
                    .placeholder(R.drawable.default_profile)
                    .into(profileImage)

            recyclerView.layoutManager = LinearLayoutManager(context!!)
            recyclerView.adapter = CommentListAdapter(context!!, replyList, {}, {}, { userId ->
                startUserActivity(activity!!, userId)
            }, true)

            replyBtn.setOnClickListener {
                EnterCommentDialog{
                    postReply(it)
                }.show(activity?.supportFragmentManager, null)
            }
        }

        loadComments()
    }

    fun loadComments() {
        progressBar.visibility = View.VISIBLE
        replyList.clear()
        ref.orderBy("dtCreated").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.documents.forEach {
                    replyList.add(it.toObject(Comment::class.java))
                }
            }
            recyclerView.adapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
        }
    }

    fun postReply(text: String) {
        if(text.isNotEmpty()) {
            val reply = Comment(id = UUID.randomUUID().toString(),
                    text = text,
                    userId = Me.value?.id,
                    userName = Me.value?.nickName,
                    userPushId = FirebaseInstanceId.getInstance().token,
                    dtCreated = System.currentTimeMillis())

            ref.document(reply.id!!).set(reply).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModel.addedReply(comment)
                    loadComments()
                }
            }


        }
    }
}