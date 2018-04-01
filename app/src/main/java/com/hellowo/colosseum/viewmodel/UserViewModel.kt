package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.model.User

class UserViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val user = MutableLiveData<User>()
    var loading = MutableLiveData<Boolean>()
    var userId: String? = null

    fun initUser(id: String?) {
        userId = id
        loadUser()
    }

    private fun loadUser() {
        userId?.let {
            loading.value = true
            db.collection("users").document(it).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        user.value = document.toObject(User::class.java)
                    }
                }
                loading.value = false
            }
        }
    }
}