package com.swayze.smartintra.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.swayze.smartintra.R
import com.swayze.smartintra.ui.dashboard.DashboardActivity
import com.swayze.smartintra.ui.login.LoginActivity

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(sharedPreference.getValueBoolean(Constant.IS_LOGIN,false)){
                    startNewActivity(DashboardActivity())
                    finish()
                }else {
                    startNewActivity(LoginActivity())
                    finishAffinity()
                }
            },
            1000 // value in milliseconds
        )
    }
}