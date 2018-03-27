package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.log


class InterestViewModel : ViewModel() {
    val db = FirebaseFirestore.getInstance()
    var newList = MutableLiveData<ArrayList<User>>()
    var interestMeList = MutableLiveData<ArrayList<User>>()
    var loading = MutableLiveData<Boolean>()
    var viewMode = MutableLiveData<Int>()
    var interestCompleted = MutableLiveData<User>()

    init {
        newList.value = ArrayList()
        interestMeList.value = ArrayList()
        loading.value = false
    }

    fun loadInterestMeList() {
        loading.value = true
        interestMeList.value?.clear()
        if(viewMode.value == null) {
            viewMode.value = 0
        }
        db.collection("couples").whereEqualTo("level", 1).whereEqualTo("${Me.value?.id}", 1).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val totalCount = task.result.size()
                        if(totalCount > 0) {
                            val myGender = Me.value?.gender as Int
                            var count = 0
                            task.result
                                    .map { Couple.getYourIdFromCoupleKey(myGender, it.id) }
                                    .forEach {
                                        db.collection("users").document(it).get().addOnCompleteListener { userTask ->
                                            count++
                                            if (userTask.isSuccessful) {
                                                interestMeList.value?.add(userTask.result.toObject(User::class.java))
                                            }
                                            if(count == totalCount) {
                                                interestMeList.value = interestMeList.value
                                                loading.value = false
                                            }
                                        }
                                    }
                        }else {
                            interestMeList.value = interestMeList.value
                            loading.value = false
                        }
                    }else {
                        interestMeList.value = interestMeList.value
                        loading.value = false
                    }
                }
    }

    fun startNewSearch(lastVisible: DocumentSnapshot?) {
        var query = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("gender", if(Me.value?.gender == 0) 1 else 0)
                .limit(100)

        if(lastVisible == null) {
            newList.value?.clear()
            viewMode.value = 1
        }else {
            query = query.startAfter(lastVisible)
        }

        query.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            newList.value?.add(document.toObject(User::class.java))
                            log(document.toObject(User::class.java).toString())
                        }
                        if(task.result.size() > 0 && newList.value?.size!! < 10) {
                            startNewSearch(task.result.last())
                        }else {
                            newList.value = newList.value
                            viewMode.value = 2
                        }
                    }
                }
    }

    fun interest(user: User, interest: Int) {
        val key = Couple.makeCoupleKey(Me.value!!, user)
        val ref = db.collection("couples").document(key)
        ref.get().addOnCompleteListener { task ->
            val doc = task.result
            val data = HashMap<String, Any?>()
            data.put("${user.id}", interest)
            if (doc.exists()) {
                if(interest == 1 && doc.get("${Me.value?.id}").toString() == "1") {
                    data.put("level", 2)
                    interestCompleted.value = user
                    interestCompleted.value = null
                }else if(interest == 0) {
                    data.put("level", 0)
                }
                ref.update(data)
            } else {
                data.put("level", 1)
                data.put("${Me.value?.id}", -1)
                ref.set(data)
            }
        }
    }

    fun stackEmpty() {
        viewMode.value = 0
    }

}