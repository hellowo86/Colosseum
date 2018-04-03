package com.hellowo.colosseum.ui.activity

import android.Manifest
import android.animation.LayoutTransition
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.firebase.iid.FirebaseInstanceId
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.App
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.ui.activity.MainActivity
import com.hellowo.colosseum.utils.isEmailValid
import com.hellowo.colosseum.utils.log
import com.hellowo.colosseum.utils.toast
import com.hellowo.colosseum.viewmodel.SplashViewModel
import gun0912.tedbottompicker.TedBottomPicker
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*

class SplashActivity : AppCompatActivity() {
    lateinit var viewModel: SplashViewModel
    private var mode = 1
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var gender = -1
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)
        initLayout()
        initObserve()
    }

    private fun initLayout() {
        profileLy.visibility = View.GONE
        emailEdit.visibility = View.GONE
        passwordEdit.visibility = View.GONE
        nameEdit.visibility = View.GONE
        ageEdit.visibility = View.GONE
        locationLy.visibility = View.GONE
        loginBtn.visibility = View.GONE
        optionBtn.visibility = View.GONE
        rootLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        loginBtn.setOnClickListener { login() }
        loginBtn.setOnLongClickListener {
            viewModel.signIn(this, "admin@gmail.com", "aaaaaaaa")
            return@setOnLongClickListener false
        }
        optionBtn.setOnLongClickListener {
            viewModel.signIn(this, "sj@gmail.com", "aaaaaaaa")
            return@setOnLongClickListener false
        }
    }

    private fun initObserve() {
        Me.observe(this, Observer { user ->
            if (user != null) {
                Me.removeObservers(this)
                val mainIntent = Intent(this, MainActivity::class.java)
                intent.extras?.let { mainIntent.putExtras(it) }
                startActivity(mainIntent)
                finish()
            } else {
                updateUI()
            }
        })

        viewModel.loading.observe(this, Observer{ status ->
            progressBar.visibility = if (status as Boolean) View.VISIBLE else View.GONE
            loginBtn.visibility = if (status) View.GONE else View.VISIBLE
            optionBtn.visibility = if (status) View.GONE else View.VISIBLE
        })
    }

    private fun updateUI() {
        progressBar.visibility = View.GONE
        if(mode == 1) {
            logoImg.visibility = View.VISIBLE
            profileLy.visibility = View.GONE
            emailEdit.visibility = View.VISIBLE
            passwordEdit.visibility = View.VISIBLE
            nameEdit.visibility = View.GONE
            ageEdit.visibility = View.GONE
            locationLy.visibility = View.GONE
            loginBtn.visibility = View.VISIBLE
            optionBtn.visibility = View.VISIBLE
            optionBtn.setOnClickListener {
                mode = 2
                updateUI()
            }
            optionBtn.text = getString(R.string.do_sign_up)
            ageEdit.setOnEditorActionListener(null)
            passwordEdit.imeOptions = EditorInfo.IME_ACTION_DONE
            passwordEdit.setOnEditorActionListener{ v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    login()
                    return@setOnEditorActionListener false
                }
                return@setOnEditorActionListener true
            }
        }else if(mode == 2) {
            logoImg.visibility = View.GONE
            profileLy.visibility = View.VISIBLE
            emailEdit.visibility = View.VISIBLE
            passwordEdit.visibility = View.VISIBLE
            nameEdit.visibility = View.VISIBLE
            ageEdit.visibility = View.VISIBLE
            locationLy.visibility = View.VISIBLE
            loginBtn.visibility = View.VISIBLE
            optionBtn.visibility = View.VISIBLE
            optionBtn.setOnClickListener {
                mode = 1
                updateUI()
            }
            optionBtn.text = getString(R.string.do_login)
            profileImage.setOnClickListener {
                com.hellowo.colosseum.utils.showPhotoPicker(this) { uri ->
                    this.uri = uri
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { profileImage.imageTintList = null }
                    Glide.with(this).load(uri).placeholder(R.drawable.default_profile)
                            .bitmapTransform(CropCircleTransformation(this)).into(profileImage)
                }
            }
            maleBtn.setOnClickListener {
                gender = 0
                maleBtn.setTextColor(resources.getColor(R.color.colorPrimary))
                femaleBtn.setTextColor(resources.getColor(R.color.iconTint))
            }
            femaleBtn.setOnClickListener {
                gender = 1
                maleBtn.setTextColor(resources.getColor(R.color.iconTint))
                femaleBtn.setTextColor(resources.getColor(R.color.colorPrimary))
            }
            locationBtn.setOnClickListener {
                val builder = PlacePicker.IntentBuilder()
                startActivityForResult(builder.build(this), 1)
            }
            passwordEdit.setOnEditorActionListener(null)
            ageEdit.imeOptions = EditorInfo.IME_ACTION_DONE
            ageEdit.setOnEditorActionListener{ v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    val builder = PlacePicker.IntentBuilder()
                    startActivityForResult(builder.build(this), 1)
                    return@setOnEditorActionListener false
                }
                return@setOnEditorActionListener true
            }
        }
    }

    private fun login() {
        if (!isEmailValid(emailEdit.text.toString())) {
            emailEdit.error = getString(R.string.invalid_email)
            return
        } else if (passwordEdit.text.length < 8) {
            passwordEdit.error = getString(R.string.invalid_password)
            return
        } else if (mode == 2 && (nameEdit.text.length < 2 || nameEdit.text.length > 8)) {
            nameEdit.error = getString(R.string.invalid_nick_name)
            return
        } else if (mode == 2 && gender == -1) {
            toast(this, R.string.plz_check_gender)
            return
        } else if (mode == 2 && lat == 0.0) {
            toast(this, R.string.plz_check_location)
            return
        } else if (mode == 2 && ageEdit.text.length < 2) {
            ageEdit.error = getString(R.string.plz_check_age)
            return
        } else if (mode == 2 && uri == null) {
            toast(this, R.string.plz_check_profile_img)
            return
        } else if (false) {
            toast(this, R.string.plz_check_policy)
            return
        }

        if(mode == 1) {
            viewModel.signIn(this, emailEdit.text.toString().trim(), passwordEdit.text.toString())
        }else {
            val cal = Calendar.getInstance()
            val user = User(nickName = nameEdit.text.toString().trim(),
                    email = emailEdit.text.toString().trim(),
                    gender = gender,
                    birth = cal.get(Calendar.YEAR) - ageEdit.text.toString().toInt() + 1,
                    lat = lat,
                    lng = lng,
                    location = locationText.text.toString(),
                    dtConnected = cal.timeInMillis,
                    dtCreated = cal.timeInMillis,
                    pushToken = FirebaseInstanceId.getInstance().token)
            viewModel.signUp(this, user, passwordEdit.text.toString(), uri!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)
            lat = place.latLng.latitude
            lng = place.latLng.longitude
            val slice = place.address.split(" ")
            if(slice.isNotEmpty()){
                val stringBuilder = StringBuilder()
                var count = 0
                ((slice.size - 1) downTo 0).forEach {
                    count++
                    if(count < 4) {
                        stringBuilder.insert(0, " ${slice[it]}")
                    }
                }
                locationText.setText(stringBuilder.trim())
            }else {
                locationText.setText(place.address.toString())
            }
        }
    }
}
