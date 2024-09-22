package com.swayze.smartintra.ui.login

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.annotations.Until
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.swayze.smartintra.R
import com.swayze.smartintra.databinding.ActivityLoginBinding
import com.swayze.smartintra.network.Resource
import com.swayze.smartintra.network.ViewModalFactory
import com.swayze.smartintra.ui.dashboard.DashboardActivity
import com.swayze.smartintra.util.ProgressDialog
import com.swayze.smartintra.util.Utils

class LoginActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel:LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            ViewModalFactory(application)
        )[LoginViewModel::class.java]
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
                    startNewActivity(DashboardActivity())
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