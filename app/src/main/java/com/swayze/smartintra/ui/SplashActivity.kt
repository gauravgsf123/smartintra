package com.swayze.smartintra.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.swayze.smartintra.R
import com.swayze.smartintra.app.BaseActivity
import com.swayze.smartintra.app.Constant
import com.swayze.smartintra.ui.bluetooth_device.SetupDeviceActivity
import com.swayze.smartintra.ui.dashboard.DashboardActivity
import com.swayze.smartintra.ui.login.LoginActivity

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(sharedPreference.getValueBoolean(Constant.IS_LOGIN,false)){
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }else {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
            },
            1000 // value in milliseconds
        )
    }
}