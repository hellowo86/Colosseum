package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.log
import com.google.firebase.firestore.DocumentReference
import android.provider.SyncStateContract.Helpers.update
import com.google.firebase.firestore.WriteBatch




class ChoiceViewModel : ViewModel() {
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
        FirebaseFirestore.getInstance().collection("interests").whereEqualTo("to_${Me.value?.id}", 1).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            log("!!!!!!!!!!" + document.toString())
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
        val data = HashMap<String, Any?>()
        data.put("from_${Me.value?.id}", interest)
        data.put("to_${user.id}", interest)
        FirebaseFirestore.getInstance().collection("interests").document(User.makeCoupleKey(Me.value!!, user)).set(data, SetOptions.merge())
                .addOnCompleteListener { task -> if (task.isSuccessful) {} }
    }

    fun stackEmpty() {
        viewMode.value = 0
    }


}