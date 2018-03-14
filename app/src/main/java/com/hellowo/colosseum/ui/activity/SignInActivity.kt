package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.viewmodel.SignUpViewModel
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignInActivity : AppCompatActivity() {
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
        Me.observe(this, Observer { user ->
            if (user != null) {
                startActivity(Intent(this, SetProfileActivity::class.java))
                finish()
            }
        })
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                viewModel.signIn(this, task.getResult(ApiException::class.java))
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
