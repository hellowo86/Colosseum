package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.log


class ChoiceViewModel : ViewModel() {
    //val ref: DatabaseReference = FirebaseDatabase.getInstance().reference
    var newList = MutableLiveData<ArrayList<User>>()
    var interestMeList = MutableLiveData<ArrayList<User>>()
    var loading = MutableLiveData<Boolean>()
    var viewMode = MutableLiveData<Int>()

    init {
        newList.value = ArrayList()
        interestMeList.value = ArrayList()
        loading.value = false
    }

    fun loadInterestMeList() {
        loading.value = true
        interestMeList.value?.clear()
        FirebaseFirestore.getInstance().collection("users").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    interestMeList.value?.add(document.toObject(User::class.java))
                    log("!!!"+document.toObject(User::class.java).toString())
                }
            }
            if(viewMode.value == null) {
                viewMode.value = 0
            }
            interestMeList.value = interestMeList.value
            loading.value = false
        }
    }

    fun startNewSearch(lastVisible: DocumentSnapshot?) {
        viewMode.value = 1
        var query = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("gender", if(Me.value?.gender == 0) 1 else 0)
                .limit(1)

        lastVisible?.let { query = query.startAfter(lastVisible) }

        query.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            newList.value?.add(document.toObject(User::class.java))
                            log(document.toObject(User::class.java).toString())
                        }
                        if(task.result.size() > 0 && newList.value?.size!! < 2) {
                            startNewSearch(task.result.last())
                        }else {
                            newList.value = newList.value
                        }
                    }
                }
    }


}