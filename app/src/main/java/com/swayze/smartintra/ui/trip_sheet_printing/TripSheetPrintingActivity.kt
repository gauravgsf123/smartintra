package com.swayze.smartintra.ui.trip_sheet_printing

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tscdll.TSCActivity
import com.swayze.smartintra.app.BaseActivity
import com.swayze.smartintra.app.Constant
import com.swayze.smartintra.R
import com.swayze.smartintra.app.ManagePermissions
import com.swayze.smartintra.databinding.ActivityTripSheetPrintingBinding
import com.swayze.smartintra.network.Resource
import com.swayze.smartintra.network.ViewModalFactory
import com.swayze.smartintra.util.ProgressDialog
import com.swayze.smartintra.util.qr_code_scanner.QRcodeScanningActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class TripSheetPrintingActivity : BaseActivity() {
    var TscDll = TSCActivity()
    private lateinit var binding: ActivityTripSheetPrintingBinding
    private lateinit var viewModel: TripSheetViewModel
    private var tripSheetList= mutableListOf<TripSheetResponse>()
    private var tripSheet : TripSheetResponse?=null
    private lateinit var managePermissions: ManagePermissions
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private var isCamera = false
    private val REQUEST_CAMERA_CAPTURE = 1002
    private var searchJob: Job? = null
    private lateinit var adapter :TripSheetAdapter
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripSheetPrintingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appBar.ivHome.setOnClickListener {
            onBackPressed()
        }

        if(sharedPreference.getValueString(Constant.MAC_ADDRESS).isNullOrEmpty())
         showToast(getString(R.string.please_setup_bluetooth_device))

        viewModel = ViewModelProvider(
            this,
            ViewModalFactory(application)
        )[TripSheetViewModel::class.java]
        //binding.etTripSheetNo.setText("123456")
        setObserver()
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        binding.ivDownload.setOnClickListener {
            binding.main.hideKeyboard()
            if(!TextUtils.isEmpty(binding.etTripSheetNo.text.toString())) {
                val body = mapOf<String, String>(
                    "CID" to "SMARTINTRA",
                    "BID" to sharedPreference.getValueString(Constant.BID)!!,
                    "DOCNUMBER" to binding.etTripSheetNo.text.toString()
                )
                viewModel.getTripSheet(body)
            }else showToast(getString(R.string.enter_trip_sheet_number))

        }
        binding.ivPrint.setOnClickListener {
            binding.main.hideKeyboard()
            isCamera = true
            managePermissions.checkPermissions()
            selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
            startScanning()
        }
        binding.barCode.setOnClickListener {
            isCamera = false
        }
        adapter = TripSheetAdapter().apply {
            itemClick = { scan ->

            }
        }
        binding.rvStickerListRecyclerview.adapter =adapter

        binding.barCode.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            val inputMethod: InputMethodManager =
                v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethod != null) {
                inputMethod.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        binding.barCode.doOnTextChanged { text, start, count, after ->
            if(!TextUtils.isEmpty(binding.barCode.text.toString().trim())){
                /*searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(50)*/
                    if (!TextUtils.isEmpty(binding.barCode.text.toString().trim())) {
                        tripSheet = tripSheetList[0]
                        findCNote(text.toString().trim())
                    }
               // }
                /*if(findCNote(binding.barCode.text.toString())){
                    Log.d("barCode",binding.barCode.text.toString())
                    printBarCode()
                }*/
            }
        }

    }

    override fun onPostResume() {
        super.onPostResume()

        if(!sharedPreference.getValueString("result").isNullOrEmpty()){
            var str = sharedPreference.getValueString("result")
            binding.barCode.setText(str)
            sharedPreference.removeValue("result")
        }
    }

    private fun setObserver() {
        viewModel.tripSheetResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
                    tripSheetList.clear()
                    tripSheetList = response.data as MutableList<TripSheetResponse>
                    response.data?.let {
                        (binding.rvStickerListRecyclerview.adapter as TripSheetAdapter).setItems(
                            it,this
                        )
                    }

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

    private fun startScanning() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraWithScanner()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_CAPTURE
            )
        }
    }

    private fun openCameraWithScanner() {
        QRcodeScanningActivity.start(this, selectedScanningSDK)
    }

    private fun findCNote(str: String) {
        lifecycleScope.launch(Dispatchers.Default) {
            var value = false
            //ProgressDialog.showProgressBar(this@TripSheetPrintingActivity)
            tripSheetList.forEach{
                if (it.BarCodeNo == str.trim() && it.printDone==false) {
                    //withContext(Dispatchers.Main) {

                        value = true
                        tripSheet = it
                        it.printDone = true
                        var index = tripSheetList.indexOf(it)
                        tripSheetList[index] = it
                        runOnUiThread {
                            (binding.rvStickerListRecyclerview.adapter as TripSheetAdapter).setItems(
                                tripSheetList,  this@TripSheetPrintingActivity
                            )
                            printBarCode()
                        }
                   // }


                    //return@lit

                }
            }

        }

        //return  value
    }


    private fun printBarCode() {
        showToast("Printing : ${tripSheet?.BarCodeNo}")
        try {
            TscDll.openport(sharedPreference.getValueString(Constant.MAC_ADDRESS)) //BT
            TscDll.sendcommand("SIZE 76 mm, 50 mm\r\n")
            TscDll.sendcommand("SPEED 4\r\n")
            TscDll.sendcommand("DENSITY 12\r\n")
            TscDll.sendcommand("CODEPAGE UTF-8\r\n")
            TscDll.sendcommand("SET TEAR ON\r\n")
            TscDll.sendcommand("SET GAP 1\r\n")
            TscDll.clearbuffer()
            TscDll.sendcommand("BOX 0,0,866,866,5")
            TscDll.sendcommand("TEXT 100,300,\"ROMAN.TTF\",0,12,12,@1\r\n")
            TscDll.printerfont(10, 10, "4", 0, 1, 1, "${tripSheet?.Company}")
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
            TscDll.printerfont(25, 90, "3", 0, 1, 1, "${tripSheet?.CNoteNo}")
            TscDll.printerfont(380, 65, "2", 0, 1, 1, "Pkts No.")
            TscDll.printerfont(400, 90, "3", 0, 1, 1, "${tripSheet?.TotalBox}")
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
            TscDll.printerfont(20, 170, "3", 0, 1, 1, "${tripSheet?.Origin}")
            TscDll.printerfont(360, 140, "2", 0, 1, 1, "Destination")
            TscDll.printerfont(360, 170, "3", 0, 1, 1, "${tripSheet?.Destination}")
            TscDll.printerfont(
                10,
                200,
                "1",
                0,
                1,
                1,
                "________________________________________________________"
            )
            TscDll.barcode(40, 230, "128", 100, 1, 0, 4, 5, "${tripSheet?.BarCodeNo}")
            TscDll.printlabel(1, 1)
            TscDll.closeport(5000)
        } catch (ex: Exception) {
        }
        binding.barCode.setText("")
        //if(isCamera) startScanning()

    }
}