package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hellowo.colosseum.R
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.viewmodel.SignUpViewModel
import kotlinx.android.synthetic.main.activity_sign_up.*
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull




class SignUpActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SignUpViewModel::class.java)
        setContentView(R.layout.activity_sign_up)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        signUpBtn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            startActivityForResult(GoogleSignIn.getClient(this, gso).signInIntent, 1)
        }
    }

    private fun initObserve() {
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        //showProgressDialog()
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.getCurrentUser()
                    } else {
                        // If sign in fails, display a message to the user.
                    }

                    // [START_EXCLUDE]
                    //hideProgressDialog()
                    // [END_EXCLUDE]
                })
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                //firebaseAuthWithGoogle(account)
                log("성공?")
            } catch (e: ApiException) {
                e.printStackTrace()
            }

        }
    }

    private fun signOut() {
        // Firebase sign out
        //mAuth.signOut()
        // Google sign out
        //mGoogleSignInClient.signOut().addOnCompleteListener(this, OnCompleteListener<Void> {  })
    }
}
