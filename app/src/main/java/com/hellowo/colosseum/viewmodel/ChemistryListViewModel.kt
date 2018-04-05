package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.ArrayMap
import com.google.firebase.firestore.*
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.ChatMember
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.log

class ChemistryListViewModel : ViewModel() {
    val db = FirebaseFirestore.getInstance()
    var coupleList = MutableLiveData<ArrayMap<String, Couple>>()
    var loading = MutableLiveData<Boolean>()
    private var coupleListenerRegistration: ListenerRegistration? = null

    init {
        coupleList.value = ArrayMap()
        loading.value = false
    }

    fun loadCoupleList() {
        loading.value = true
        val myPrefix = Couple.getGenderKey(Me.value?.gender as Int)
        db.collection("couples").whereEqualTo("level", 2).whereEqualTo("${myPrefix}Id", Me.value?.id).addSnapshotListener { snapshots, e ->
            if (e == null) {
                snapshots.documentChanges.forEach {
                    val couple = it.document.toObject(Couple::class.java)
                    couple.id = it.document.id
                    when{
                        it.type == DocumentChange.Type.ADDED -> {
                            coupleList.value?.put(couple.id, couple)
                        }
                        it.type == DocumentChange.Type.REMOVED -> {
                            coupleList.value?.remove(couple.id)
                        }
                        it.type == DocumentChange.Type.MODIFIED -> {
                            coupleList.value?.put(couple.id, couple)
                        }
                    }
                }
                coupleList.value = coupleList.value
            }
            loading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        coupleListenerRegistration?.remove()
    }
}