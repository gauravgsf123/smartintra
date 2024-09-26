package com.swayze.smartintra.ui.bluetooth_device

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tscdll.TSCActivity
import com.swayze.smartintra.app.BaseActivity
import com.swayze.smartintra.app.Constant
import com.swayze.smartintra.databinding.ActivitySetupDeviceBinding


class SetupDeviceActivity : BaseActivity() {
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1
    var TscDll = TSCActivity()
    private lateinit var binding: ActivitySetupDeviceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBar.ivHome.setOnClickListener {
            onBackPressed()
        }
        checkBluetoothPermissions()
        sharedPreference.save(Constant.MAC_ADDRESS, "00:19:0E:A6:48:AC")
        binding.tvLastConnectedDevice.text = sharedPreference.getValueString(Constant.MAC_ADDRESS)

        binding.btnNewAddDevice.setOnClickListener {
            binding.btnAddDevice.visibility = View.VISIBLE
            binding.tvMacId.visibility = View.VISIBLE
        }
        binding.btnAddDevice.setOnClickListener {
            sharedPreference.save(Constant.MAC_ADDRESS, "00:19:0E:A6:48:AC")
            binding.tvLastConnectedDevice.text = sharedPreference.getValueString(Constant.MAC_ADDRESS)!!
        }
        binding.btnTestDevice.setOnClickListener {
            //if (checkBluetoothPermissions()) {
                printBarCode()
            //}
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_BLUETOOTH_PERMISSIONS)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                //printBarCode()
            } else {
                Log.d("DeviceSetupActivity", "Permissions denied.")
            }
        }
    }

    private fun printBarCode() {
        showToast("Printing")
        val macAdd = "00:19:0E:A6:48:AC"//sharedPreference.getValueString(Constant.MAC_ADDRESS)
        Log.d("mac_address", macAdd.toString())
        try {
            TscDll.openport(macAdd) //BT
            TscDll.sendcommand("SIZE 76 mm, 50 mm\r\n")
            TscDll.sendcommand("SPEED 6\r\n")
            TscDll.sendcommand("DENSITY 12\r\n")
            TscDll.sendcommand("CODEPAGE UTF-8\r\n")
            TscDll.sendcommand("SET TEAR ON\r\n")
            TscDll.clearbuffer()
            TscDll.sendcommand("BOX 0,0,866,866,5")
            TscDll.sendcommand("TEXT 100,300,\"ROMAN.TTF\",0,12,12,@1\r\n")
            TscDll.printerfont(10, 10, "4", 0, 1, 1, "SmartIntra Cloud Technologies")
            TscDll.printerfont(
                10,
                45,
                "1",
                0,
                1,
                1,
                "________________________________________________________"
            )
            TscDll.printerfont(70, 65, "2", 0, 1, 1, "AWB No.")
            TscDll.printerfont(25, 90, "3", 0, 1, 1, "20250000037")
            TscDll.printerfont(380, 65, "2", 0, 1, 1, "Pkts No.")
            TscDll.printerfont(400, 90, "3", 0, 1, 1, "1/3")
            TscDll.printerfont(
                10,
                120,
                "1",
                0,
                1,
                1,
                "________________________________________________________"
            )
            TscDll.printerfont(20, 140, "2", 0, 1, 1, "Origin")
            TscDll.printerfont(20, 170, "3", 0, 1, 1, "LUCKNOW")
            TscDll.printerfont(360, 140, "2", 0, 1, 1, "Destination")
            TscDll.printerfont(360, 170, "3", 0, 1, 1, "MIRZAPUR")
            TscDll.printerfont(
                10,
                200,
                "1",
                0,
                1,
                1,
                "________________________________________________________"
            )
            TscDll.barcode(40, 230, "128", 100, 1, 0, 4, 5, "PBOX052441")
            TscDll.printlabel(1, 1)
            TscDll.closeport(5000)
        } catch (ex: Exception) {
            Log.d("Device Setup", "This is an error $ex")
        }

        showToast("Print Done")
    }

}