package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.support.design.widget.Snackbar
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.isEmailValid

class SignUpViewModel : ViewModel() {
    enum class SignUpStatus { InvalidNickName, InvalidEmail, InvalidPassword, PolicyUncheck, CompleteSignUp }

    val db = FirebaseFirestore.getInstance()
    var signUpStatus = MutableLiveData<SignUpStatus>()
    var loading = MutableLiveData<Boolean>()

    fun signUp(context: Context, nickName: String, email: String, password: String, policyCheck: Boolean) {
        loading.value = true
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener({
            if (!it.isSuccessful) {
                try{
                    throw it.exception!!
                } catch (e: FirebaseAuthUserCollisionException){
                    Toast.makeText(context, R.string.existed_email, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.signup_failed, Toast.LENGTH_SHORT).show()
                }
                loading.value = false
            }else {
                createUserProfile(it.result.user, nickName)
            }
        })
    }

    private fun createUserProfile(fUser: FirebaseUser, nickName: String) {
        db.collection("users").document(fUser.uid).set(User(fUser.uid, nickName, null, System.currentTimeMillis()))
    }
}
