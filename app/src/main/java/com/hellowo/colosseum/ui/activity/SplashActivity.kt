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
        profileImage.visibility = View.GONE
        emailEdit.visibility = View.GONE
        passwordEdit.visibility = View.GONE
        nameEdit.visibility = View.GONE
        genderBtn.visibility = View.GONE
        ageEdit.visibility = View.GONE
        locationBtn.visibility = View.GONE
        loginBtn.visibility = View.GONE
        optionBtn.visibility = View.GONE
        moreInfoEdit.visibility = View.GONE
        rootLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        loginBtn.setOnClickListener { login() }
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
            profileImage.visibility = View.GONE
            emailEdit.visibility = View.VISIBLE
            passwordEdit.visibility = View.VISIBLE
            nameEdit.visibility = View.GONE
            genderBtn.visibility = View.GONE
            ageEdit.visibility = View.GONE
            locationBtn.visibility = View.GONE
            moreInfoEdit.visibility = View.GONE
            loginBtn.visibility = View.VISIBLE
            optionBtn.visibility = View.VISIBLE
            optionBtn.setOnClickListener {
                mode = 2
                updateUI()
            }
            optionBtn.text = getString(R.string.do_sign_up)
            passwordEdit.imeOptions = EditorInfo.IME_ACTION_DONE
            passwordEdit.setOnEditorActionListener{ v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    login()
                    return@setOnEditorActionListener false
                }
                return@setOnEditorActionListener true
            }
            moreInfoEdit.setOnEditorActionListener(null)
        }else if(mode == 2) {
            logoImg.visibility = View.GONE
            profileImage.visibility = View.VISIBLE
            emailEdit.visibility = View.VISIBLE
            passwordEdit.visibility = View.VISIBLE
            nameEdit.visibility = View.VISIBLE
            genderBtn.visibility = View.VISIBLE
            ageEdit.visibility = View.VISIBLE
            locationBtn.visibility = View.VISIBLE
            moreInfoEdit.visibility = View.VISIBLE
            loginBtn.visibility = View.VISIBLE
            optionBtn.visibility = View.VISIBLE
            optionBtn.setOnClickListener {
                mode = 1
                updateUI()
            }
            optionBtn.text = getString(R.string.do_login)
            profileImage.setOnClickListener { checkExternalStoragePermission() }
            genderBtn.setOnClickListener {
                if(gender == -1 || gender == 1) {
                    gender = 0
                    genderBtn.text = getString(R.string.male)
                }else {
                    gender = 1
                    genderBtn.text = getString(R.string.female)
                }
            }
            locationBtn.setOnClickListener {
                val builder = PlacePicker.IntentBuilder()
                startActivityForResult(builder.build(this), 1)
            }
            moreInfoEdit.imeOptions = EditorInfo.IME_ACTION_DONE
            moreInfoEdit.setOnEditorActionListener{ v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    login()
                    return@setOnEditorActionListener false
                }
                return@setOnEditorActionListener true
            }
            passwordEdit.setOnEditorActionListener(null)
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
        } else if (mode == 2 && moreInfoEdit.text.isEmpty()) {
            moreInfoEdit.error = getString(R.string.plz_check_moreInfo)
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
                    location = locationBtn.text.toString().trim(),
                    moreInfo = moreInfoEdit.text.toString().trim(),
                    dtConnected = cal.timeInMillis,
                    dtCreated = cal.timeInMillis,
                    pushToken = FirebaseInstanceId.getInstance().token)
            viewModel.signUp(this, user, passwordEdit.text.toString(), uri!!)
        }
    }

    private val permissionlistener = object : PermissionListener {
        override fun onPermissionGranted() {
            showPhotoPicker()
        }
        override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
    }

    private fun checkExternalStoragePermission() {
        TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
    }

    private fun showPhotoPicker() {
        val bottomSheetDialogFragment = TedBottomPicker.Builder(this)
                .setOnImageSelectedListener { uri -> this.uri = uri
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        profileImage.imageTintList = null
                    }
                    Glide.with(this).load(uri).placeholder(R.drawable.default_profile)
                            .bitmapTransform(CropCircleTransformation(this)).into(profileImage)}
                .setPreviewMaxCount(100)
                .create()
        bottomSheetDialogFragment.show(supportFragmentManager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)
            lat = place.latLng.latitude
            lng = place.latLng.longitude
            locationBtn.text = getString(R.string.selected)
        }
    }
}
