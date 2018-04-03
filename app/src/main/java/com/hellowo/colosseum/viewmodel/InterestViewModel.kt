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
        val myPrefix = Couple.getGenderKey(Me.value?.gender as Int)
        val yourPrefix = Couple.getGenderKey(Me.value?.getYourGender() as Int)
        db.collection("couples").whereEqualTo("level", 1).whereEqualTo("${myPrefix}Id", Me.value?.id)
                .whereEqualTo("${yourPrefix}Interest", 1).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val totalCount = task.result.size()
                        if(totalCount > 0) {
                            var count = 0
                            task.result.forEach {
                                        db.collection("users").document(it.getString("${yourPrefix}Id")).get().addOnCompleteListener { userTask ->
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

                    if(viewMode.value == null) {
                        viewMode.value = 0
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
            val yourPrefix = Couple.getGenderKey(user.gender)
            val myPrefix = Couple.getGenderKey(Me.value?.gender!!)
            data.put("${myPrefix}Interest", interest)
            if (doc.exists()) {
                if(interest == 1 && doc.get("${yourPrefix}Interest").toString() == "1") {
                    log(doc.get("${yourPrefix}Interest").toString())
                    data.put("level", 2)
                    interestCompleted.value = user
                    interestCompleted.value = null
                }else if(interest == 0) {
                    data.put("level", 0)
                }
                ref.update(data)
            } else {
                data.put("level", 1)
                data.put("${yourPrefix}Interest", -1)
                data.put("${yourPrefix}Id", user.id)
                data.put("${yourPrefix}Name", user.nickName)
                data.put("${yourPrefix}PushToken", user.pushToken)
                data.put("${myPrefix}Id", Me.value?.id)
                data.put("${myPrefix}Name", Me.value?.nickName)
                data.put("${myPrefix}PushToken", Me.value?.pushToken)
                ref.set(data)
            }
        }
    }

    fun stackEmpty() {
        viewMode.value = 0
    }

}