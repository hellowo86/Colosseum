package com.hellowo.colosseum.data

import android.arch.lifecycle.LiveData
import com.google.firebase.firestore.*
import com.hellowo.colosseum.model.User
import com.google.firebase.firestore.DocumentChange
import com.hellowo.colosseum.split
import com.hellowo.colosseum.utils.log

object InterestedCouple : LiveData<String>() {
    private val ref = FirebaseFirestore.getInstance().collection("couples")
            .whereEqualTo("${Me.value?.id}", 1)
            .whereEqualTo("level", 2)
    private val listener = EventListener<QuerySnapshot> { snapshot, e ->
        if (e != null) {
            return@EventListener
        }
        snapshot.documentChanges
                .filter { it.type == DocumentChange.Type.ADDED }
                .forEach { log("!!!!" + it.document.data.toString()) }
    }
    private var listenerRegistration: ListenerRegistration? = null

    override fun onActive() {
        listenerRegistration = ref.addSnapshotListener(listener)
    }

    override fun onInactive() {
        listenerRegistration?.remove()
    }
}