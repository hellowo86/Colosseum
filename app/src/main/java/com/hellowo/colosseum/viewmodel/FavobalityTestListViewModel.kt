package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.log

class FavobalityTestListViewModel : ViewModel() {
    val db = FirebaseFirestore.getInstance()
    var coupleList = MutableLiveData<ArrayList<Couple>>()
    var loading = MutableLiveData<Boolean>()

    init {
        coupleList.value = ArrayList()
        loading.value = false
    }

    fun loadCoupleList() {
        loading.value = true
        coupleList.value?.clear()
        db.collection("couples").whereEqualTo("level", 2).whereEqualTo("${Me.value?.id}", 1).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val totalCount = task.result.size()
                        if(totalCount > 0) {
                            val myGender = Me.value?.gender as Int
                            var count = 0
                            task.result.forEach { doc ->
                                val yourId = Couple.getYourIdFromCoupleKey(myGender, doc.id)
                                val couple = doc.toObject(Couple::class.java)
                                couple.id = doc.id
                                couple.me = Me.value!!

                                db.collection("users").document(yourId).get().addOnCompleteListener { userTask ->
                                    count++
                                    if (userTask.isSuccessful) {
                                        couple.you = userTask.result.toObject(com.hellowo.colosseum.model.User::class.java)
                                        coupleList.value?.add(couple)
                                        log(couple.toString())
                                    }
                                    if(count == totalCount) {
                                        coupleList.value = coupleList.value
                                        loading.value = false
                                    }
                                }
                            }
                        }else {
                            coupleList.value = coupleList.value
                            loading.value = false
                        }
                    }else {
                        coupleList.value = coupleList.value
                        loading.value = false
                    }
                }
    }
}