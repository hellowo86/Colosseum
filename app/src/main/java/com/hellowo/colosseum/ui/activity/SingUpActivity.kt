package com.hellowo.colosseum.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.hellowo.colosseum.App
import com.hellowo.colosseum.R

import com.hellowo.colosseum.viewmodel.SignUpViewModel
import kotlinx.android.synthetic.main.activity_sign_up.*

class SingUpActivity : AppCompatActivity() {
    internal lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SignUpViewModel::class.java)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        backBtn.setOnClickListener{ v -> finish() }
        goSignInViewBtn.setOnClickListener{ v -> goSignInView() }
        signUpBtn.setOnClickListener{ v -> signUp() }
    }

    private fun initObserve() {
        viewModel.signUpStatus.observe(this, Observer{ status ->
            when (status) {
                SignUpViewModel.SignUpStatus.InvalidNickName -> nameEdit.error = getString(R.string.invalid_nick_name)
                SignUpViewModel.SignUpStatus.InvalidEmail -> emailEdit.error = getString(R.string.invalid_email)
                SignUpViewModel.SignUpStatus.InvalidPassword -> passwordEdit.error = getString(R.string.invalid_password)
                SignUpViewModel.SignUpStatus.PolicyUncheck -> Toast.makeText(App.context, R.string.plz_check_policy, Toast.LENGTH_SHORT).show()
                SignUpViewModel.SignUpStatus.CompleteSignUp -> {
                    startActivity(Intent(this@SingUpActivity, MainActivity::class.java))
                    finish()
                }
                else -> {
                }
            }
        })

        viewModel.loading.observe(this, Observer{ bool ->
            progressBar.visibility = if (bool as Boolean) View.VISIBLE else View.GONE
            signUpBtn.visibility = if (bool) View.GONE else View.VISIBLE
        })
    }

    private fun signUp() {
        viewModel.signUp(
                nameEdit.getText().toString().trim(),
                emailEdit.getText().toString().trim(),
                passwordEdit.getText().toString(),
                policyCheck.isChecked()
        )
    }

    fun goSignInView() {
        //startActivity(new Intent(SingUpActivity.this, SignInActivity.class));
        finish()
    }
}
