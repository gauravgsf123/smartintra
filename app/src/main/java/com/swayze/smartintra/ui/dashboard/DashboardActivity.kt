package com.swayze.smartintra.ui.dashboard

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.navigation.NavigationView
import com.mpcl.app.BaseActivity
import com.swayze.smartintra.R
import com.swayze.smartintra.databinding.ActivityDashboardBinding
import com.swayze.smartintra.ui.bluetooth_device.SetupDeviceActivity
import com.swayze.smartintra.ui.login.LoginActivity
import com.swayze.smartintra.ui.trip_sheet_printing.TripSheetPrintingActivity

class DashboardActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.navView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, binding.main, binding.toolbar, R.string.open_nav, R.string.close_nav)
        binding.main.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onNavigationItemSelected(menu: MenuItem): Boolean {
        when(menu.itemId) {
            R.id.trip_sheet_printing->{
                startNewActivity(TripSheetPrintingActivity())
            }
            R.id.nav_setup_device->{
                startNewActivity(SetupDeviceActivity())
            }
            R.id.nav_logout -> {
                sharedPreference.clearSharedPreference()
                startNewActivity(LoginActivity())
                finish()
            }
        }
        binding.main.closeDrawer(GravityCompat.START)
        return true
    }
}