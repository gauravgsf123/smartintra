package com.swayze.smartintra.ui.login

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.annotations.Until
import com.swayze.smartintra.app.BaseActivity
import com.swayze.smartintra.app.Constant
import com.permissionx.guolindev.PermissionX
import com.swayze.smartintra.R
import com.swayze.smartintra.app.ManagePermissions
import com.swayze.smartintra.databinding.ActivityLoginBinding
import com.swayze.smartintra.network.Resource
import com.swayze.smartintra.network.ViewModalFactory
import com.swayze.smartintra.ui.dashboard.DashboardActivity
import com.swayze.smartintra.util.ProgressDialog
import com.swayze.smartintra.util.Utils

class LoginActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel:LoginViewModel
    private lateinit var managePermissions : ManagePermissions
    private val permissionList = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            ViewModalFactory(application)
        )[LoginViewModel::class.java]

        binding.username.setText("SMT0002")
        binding.password.setText("SMT9999")
        /*managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        if(!sharedPreference.getValueBoolean(Constant.IS_LOGIN,false)){
            managePermissions.checkPermissions()
        }*/

        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    //Toast.makeText(this, "All permissions are granted", Toast.LENGTH_SHORT).show()
                } else {
                    //Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_SHORT).show()
                }
            }

        binding.login.setOnClickListener{
            binding.main.hideKeyboard()
            validate()
        }
        setObserver()
    }

    private fun validate() {
        binding?.run {
            if(TextUtils.isEmpty(username.text.toString())){
                Toast.makeText(this@LoginActivity,getString(R.string.please_enter_username), Toast.LENGTH_LONG).show()
            }else if(TextUtils.isEmpty(password.text.toString())){
                Toast.makeText(this@LoginActivity,getString(R.string.please_enter_password), Toast.LENGTH_LONG).show()
            }else {
                val body = mapOf<String, String>(
                    "EMPNO" to username.text.toString(),
                    "EMPPASS" to password.text.toString()
                )
                viewModel.login(body)
            }
        }
    }

    private fun setObserver() {
        viewModel.loginResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
                    sharedPreference.save(Constant.IS_LOGIN,true)
                    sharedPreference.save(Constant.EMP_NO,response.data?.EMPNO!!)
                    sharedPreference.save(Constant.BID, response.data.BID!!)
                    sharedPreference.save(Constant.BCITY,response.data.BCITY!!)
                    sharedPreference.save(Constant.EMPNAME,response.data.EMPNAME!!)
                    Toast.makeText(this,"Login Success",Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }

                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }

                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    response.message?.let { showToast(it) }
                }

                else -> {
                    ProgressDialog.hideProgressBar()
                    response.message?.let { showToast(it) }
                }
            }
        }
    }

    override fun onClick(p0: View?) {

    }
}