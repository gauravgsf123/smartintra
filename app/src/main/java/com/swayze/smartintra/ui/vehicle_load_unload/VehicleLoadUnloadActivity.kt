package com.swayze.smartintra.ui.vehicle_load_unload

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import com.swayze.smartintra.R
import com.swayze.smartintra.app.BaseActivity
import com.swayze.smartintra.app.Constant
import com.swayze.smartintra.app.ManagePermissions
import com.swayze.smartintra.databinding.ActivityVehicleLoadUnloadBinding
import com.swayze.smartintra.network.ViewModalFactory
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleData
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleListData
import com.swayze.smartintra.ui.vehicle_load_unload.model.VehicleLoadRequest
import com.swayze.smartintra.util.qr_code_scanner.QRcodeScanningActivity
import java.util.Locale


class VehicleLoadUnloadActivity : BaseActivity(),TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityVehicleLoadUnloadBinding
    private lateinit var viewModel: VehicleLoadUnloadModel
    private var valueList: MutableList<String> = mutableListOf()
    private var nameList: MutableList<String> = mutableListOf()
    private var preFixList: MutableList<String> = mutableListOf()
    //private var vechileDataBox: Box<VechileData>? = null
    //private var vechileListDataBox: Box<VehicleListData>? = null
    private lateinit var vehicleData: List<VehicleData>
    private lateinit var vehicleListData: MutableList<VehicleListData>
    private var docType: String =""
    private var enableTrue: Boolean = false
    private var callOneTime: Boolean = false
    private var isCamera = false
    lateinit var dialog: AlertDialog
    private var tts: TextToSpeech? = null
    private val REQUEST_CAMERA_CAPTURE = 100
    private lateinit var managePermissions: ManagePermissions
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVehicleLoadUnloadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.topBar.ivHome.setOnClickListener { onBackPressed() }
        tts = TextToSpeech(this, this)
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        viewModel = ViewModelProvider(
            this,
            ViewModalFactory(application)
        )[VehicleLoadUnloadModel::class.java]
        binding.stockListRecyclerview.adapter = VechileLoadAdapter().apply {
            itemClick = { scan ->

            }
        }
        enableTrue = sharedPreference.getValueBoolean(Constant.VECHICLE_ENABLE, false)


        getDoctype()
        setObserver()

        binding.type.setOnClickListener {
            binding.type.showDropDown()
            Log.e("nameList","${nameList.size}")
        }
        binding.type.setText(resources.getString(R.string.select_option))
        binding.type.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (!binding.type.isPopupShowing) {
                    binding.type.showDropDown()
                }
            }
            false
        }

        if (enableTrue) {
            binding.type.isEnabled = false
            //binding.documentNo.isEnabled = false
            enableDocument(false)
            binding.check.visibility = View.GONE
            binding.boxNo.visibility = View.VISIBLE
            binding.ivPrint.visibility = View.VISIBLE
            binding.type.setText(sharedPreference.getValueString(Constant.VECHICLE_POSITION),false)
            docType = sharedPreference.getValueString(Constant.DOCTYPE)!!
            //binding.documentNo4.setText(sharedPreference.getValueString(Constant.DOCUMENT)!!)
            setDocumentNo()
            binding.totalScan.text = "${sharedPreference.getValueInt(Constant.TOTAL_DOC)}"
        } else {
            binding.type.isEnabled = true
            enableDocument(true)
            binding.check.visibility = View.VISIBLE
            binding.boxNo.visibility = View.GONE
            binding.ivPrint.visibility = View.GONE
        }


        binding.type.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selected = parent.getItemAtPosition(position)
                docType = valueList[position]
                sharedPreference.save(Constant.VECHICLE_POSITION, nameList[position])
                sharedPreference.save(Constant.DOCTYPE, docType)
                reset()
                binding.type.setText(selected.toString())
                when (docType!!) {
                    /*"PRS" -> {
                        showHide(true);binding.documentNo3.setText("PR")
                    }*/
                    "MFIN" -> {
                        showHide(false)
                    }
                    /*"MFOUT" -> {
                        showHide(true);binding.documentNo3.setText("MF")
                    }
                    "DRS", "DRS" -> {
                        showHide(true);binding.documentNo3.setText("DR")
                    }*/
                    else ->{
                        showHide(true); binding.documentNo3.setText(preFixList[position])
                    }
                }

            }

        binding.boxNo.doOnTextChanged { text, start, count, after ->
            if (!TextUtils.isEmpty(binding.boxNo.text.toString().trim())) {
                findDocumentNo(binding.boxNo.text.toString())
            }
        }

        binding.submit.setOnClickListener {
            showDialog(false)
            getJSon(vehicleData)
        }

        binding.ivPrint.setOnClickListener {
            isCamera = true
            managePermissions.checkPermissions()
            selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
            startScanning()
        }

        binding.boxNo.setOnClickListener { isCamera = false }

        binding.view.setOnClickListener{
            callViewDocumentListAPI()
        }


        binding.check.setOnClickListener {
            val mScanDocDataBody = mapOf<String, String>(
                "CID" to sharedPreference.getValueString(Constant.CID)!!,
                "BID" to sharedPreference.getValueString(Constant.BID)!!,
                "DOCNUMBER" to getDocumentNo(),
                "DOCTYPE" to docType
            )
            showDialog()
            callOneTime = false
            Log.d(TAG, mScanDocDataBody.toString())
            viewModel.getVehicleDataList(mScanDocDataBody)
        }

        binding.reset.setOnClickListener {
            reset()
        }

        binding.documentNo4.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
                val enteredString = s.toString()
                if (docType == "MFIN") {
                    if (enteredString.startsWith("0")) {
                        Toast.makeText(
                            this@VehicleLoadUnloadActivity,
                            "should not starts with zero(0)",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (enteredString.isNotEmpty()) {
                            binding.documentNo4.setText(enteredString.substring(1))
                        } else {
                            binding.documentNo4.setText("")
                        }
                    }
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun onPostResume() {
        super.onPostResume()
        if (sharedPreference.getValueString("result")!!.isNotEmpty()) {
            var str = sharedPreference.getValueString("result")
            Log.d(TAG, str!!)
            binding.boxNo.setText(str)
            sharedPreference.removeValue("result")

        }
    }

    private fun openCameraWithScanner() {
        QRcodeScanningActivity.start(this, selectedScanningSDK)
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

    private fun callViewDocumentListAPI() {
        val mScanDocDataBody = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.CID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "DOCNUMBER" to sharedPreference.getValueString(Constant.DOCUMENT)!!,
            "DOCTYPE" to docType
        )
        showDialog()
        viewModel.scanDocTotal(mScanDocDataBody)
    }

    private fun setObserver() {
        setRecylatView(viewModel.vehicleAllListData.value?.toMutableList() ?: mutableListOf())

        viewModel.docTypeListResponse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                responseModel.let { it1 -> Log.d(TAG, it1.size.toString()) }
                valueList.clear()
                nameList.clear()
                preFixList.clear()
                for (branch in responseModel) {
                    valueList.add(branch.Value!!)
                    nameList.add(branch.Name!!)
                    preFixList.add(branch.Prefix!!)
                }
                val traderTypeAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, nameList)
                binding.type.setAdapter(traderTypeAdapter)



            } else {
                showError(
                    getString(R.string.opps),
                    "Some thing wrong"
                )
            }
        })

        viewModel.vehicleResponseModel.observe(this, Observer { it ->
            hideDialog()
            val responseModel = it ?: return@Observer
            Log.d(TAG, responseModel.toString())
            if (responseModel.isNotEmpty()) {
                binding.type.isEnabled = false
                enableDocument(false)
                nameList.clear()
                viewModel.deleteAllVehicleListData()
                viewModel.deleteAllVehicleData()
                val traderTypeAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, nameList)
                binding.type.setAdapter(traderTypeAdapter)
                enableTrue = true
                binding.check.visibility = View.GONE
                binding.boxNo.visibility = View.VISIBLE
                binding.ivPrint.visibility = View.VISIBLE

                sharedPreference.save(Constant.VECHICLE_ENABLE, enableTrue)
                sharedPreference.save(Constant.DOCUMENT, getDocumentNo())
                sharedPreference.save(Constant.TOTAL_DOC, responseModel.size)
                binding.tvScanStatus.text = ""


                viewModel.insertVehicleListBulk(responseModel)



            } else {
                showError(
                    getString(R.string.opps),
                    getString(R.string.data_not_available)
                )
            }
        })

        viewModel.uploadVehicleScanResponse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            Log.d(TAG, responseModel.toString())
            viewModel.deleteAllVehicleData()
            viewModel.deleteAllVehicleListData()

            binding.type.isEnabled = false
            //binding.documentNo.isEnabled = false
            enableDocument(false)
            enableTrue = false
            binding.check.visibility = View.VISIBLE
            binding.submit.visibility = View.GONE
            binding.boxNo.visibility = View.VISIBLE
            binding.ivPrint.visibility = View.VISIBLE
            sharedPreference.save(Constant.VECHICLE_ENABLE, enableTrue)
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Data successfully uploaded on server")
                .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener { sweetAlert ->
                    sweetAlert.dismiss()
                    val mScanDocDataBody = mapOf<String, String>(
                        "CID" to sharedPreference.getValueString(Constant.CID)!!,
                        "BID" to sharedPreference.getValueString(Constant.BID)!!,
                        "DOCNUMBER" to sharedPreference.getValueString(Constant.DOCUMENT).toString(),
                        "DOCTYPE" to sharedPreference.getValueString(Constant.DOCTYPE).toString()
                    )
                    showDialog()
                    callOneTime = false
                    viewModel.getVehicleDataList(mScanDocDataBody)
                })
                .show()
        })

        viewModel.vehicleAllListData.observe(this, Observer { list ->
            if (list == null) {
                Log.d(TAG, "vehicleAllListData is null")
                setRecylatView(mutableListOf())
                binding.totalScan.text = "0"
                return@Observer
            }
            vehicleListData = list.toMutableList()
            setRecylatView(vehicleListData)
            binding.totalScan.text = "${list.size}"
        })

        viewModel.vehicleAllData.observe(this, Observer { list ->
            /*if (list == null) return@Observer
            vehicleData = list*/

            if (list == null) {
                Log.d(TAG, "vehicleAllData is null")
                vehicleData = listOf()
                return@Observer
            }
            vehicleData = list

        })

    }

    private fun getDoctype(){
        showDialog()
        val body = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.CID)!!
        )
        viewModel.getDocTypeList(body)
    }

    private fun showHide(isShow: Boolean) {
        if (isShow) {
            binding.documentNo1.visibility = View.VISIBLE
            binding.documentNo2.visibility = View.VISIBLE
            binding.documentNo3.visibility = View.VISIBLE
        } else {
            binding.documentNo1.visibility = View.GONE
            binding.documentNo2.visibility = View.GONE
            binding.documentNo3.visibility = View.GONE
        }
    }

    fun enableDocument(enable: Boolean) {
        binding.documentNo1.isEnabled = enable
        binding.documentNo2.isEnabled = enable
        binding.documentNo3.isEnabled = enable
        binding.documentNo4.isEnabled = enable
        if (enable) {
            binding.documentNo1.setText("")
            binding.documentNo2.setText("")
            binding.documentNo3.setText("")
            binding.documentNo4.setText("")
        }
    }

    private fun reset(){
        //vechileDataBox?.removeAll()
        viewModel.deleteAllVehicleListData()
        binding.type.isEnabled = true
        enableDocument(true)
        enableTrue = false
        binding.type.setText(resources.getString(R.string.select_option))

        binding.check.visibility = View.VISIBLE
        binding.submit.visibility = View.GONE
        binding.boxNo.visibility = View.GONE
        binding.ivPrint.visibility = View.GONE
        sharedPreference.save(Constant.VECHICLE_ENABLE, enableTrue)
        viewModel.deleteAllVehicleListData()
        //vechileListDataBox?.all?.let { setRecylatView(it) }
        getDoctype()
    }

    private fun findDocumentNo(barcode: String) {
        val stockData = vehicleData.find { it.bar_code == barcode }
        if (stockData != null && stockData.isMatch==true) {
            binding.tvScanStatus.setTextColor(resources.getColor(R.color.yellow))
            binding.tvScanStatus.text = getString(R.string.barcode_already_scan)
            checkListSize()
        } else {
            val barCodeData = vehicleListData.find { it.BarCodeNo==barcode }
            if(barCodeData!=null){
                //vechileDataBox?.put(barcode?.let { it1 -> getData(it1,barCodeData.CNoteNo!!,true) })
                viewModel.insertVehicle(getData(barcode,barCodeData.CNoteNo!!,true))
                barCodeData.isScan = true
                viewModel.updateVehicleList(barCodeData)
                binding.tvScanStatus.setTextColor(resources.getColor(R.color.green))
                binding.tvScanStatus.text = "${getString(R.string.success)} ChgWet: ${barCodeData.ChgWeight}"
                speakOut(barCodeData.Destination!!)
                checkListSize()
            }else{
                binding.tvScanStatus.setTextColor(resources.getColor(R.color.red))
                binding.tvScanStatus.text = getString(R.string.wrong_barcode_scan)
                checkListSize()
            }
            /*else{
                val barCodeData1 = vehicleListData.find { it.BarCodeNo==barcode }
                if(barCodeData1!=null){
                    binding.tvScanStatus.setTextColor(resources.getColor(R.color.red))
                    binding.tvScanStatus.text = getString(R.string.wrong_barcode_scan)
                    sendExtraScan(barcode)
                    checkListSize()
                }else{
                    viewModel.insertVehicle(getData(barcode,"",true))
                    barCodeData1 = VehicleListData(0,"","",barCodeData1?.CNoteNo,barcode,"",true)
                    viewModel.insertVehicleList(barCodeData1)
                    binding.tvScanStatus.setTextColor(resources.getColor(R.color.red))
                    binding.tvScanStatus.text = getString(R.string.wrong_barcode_scan)
                    sendExtraScan(barcode)
                    checkListSize()
                }

            }*/
            //vechileListDataBox?.all?.let { setRecylatView(it) }


        }
        setRecylatView(viewModel.vehicleAllListData.value?.toMutableList() ?: mutableListOf())
        /*setRecylatView(viewModel.vehicleAllListData)
        sharedPreference.removeValue("result")
        binding.boxNo.setText("")*/
    }

    fun setDocumentNo() {
        docType = sharedPreference.getValueString(Constant.DOCTYPE)!!
        var documentNo = sharedPreference.getValueString(Constant.DOCUMENT)!!
        val docNo = documentNo.split("-")
        binding.documentNo1.setText(docNo[0].substring(0,3))
        binding.documentNo2.setText(docNo[0].substring(3,7))
        binding.documentNo3.setText(docNo[1].substring(0,2))
        binding.documentNo4.setText(docNo[1].substring(2,docNo[1].length))
        /*when (docType) {
            "PRS" -> {
                binding.documentNo1.setText(docNo[0])
                binding.documentNo2.setText(docNo[1])
                binding.documentNo3.setText(docNo[2].substring(0,2))
                binding.documentNo4.setText(docNo[2].substring(2))
            }

            "MFIN" -> {
                binding.documentNo4.setText(sharedPreference.getValueString(Constant.DOCUMENT)!!)
            }
            "MFOUT" -> {
                binding.documentNo1.setText(docNo[0])
                binding.documentNo2.setText(docNo[1])
                binding.documentNo3.setText(docNo[2].substring(0,2))
                binding.documentNo4.setText(docNo[2].substring(2))
            }
            "DRS", "DRS" -> {
                binding.documentNo1.setText(docNo[0])
                binding.documentNo2.setText(docNo[1])
                binding.documentNo3.setText(docNo[2].substring(0,2))
                binding.documentNo4.setText(docNo[2].substring(2))
            }
            else -> {
                binding.documentNo1.setText(docNo[0])
                binding.documentNo2.setText(docNo[1])
                binding.documentNo3.setText(docNo[2].substring(0,2))
                binding.documentNo4.setText(docNo[2].substring(2))
            }
        }*/
    }

    private fun sendExtraScan(barcode: String){
        val mScanDocDataBody = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.CID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "EMPNO" to sharedPreference.getValueString(Constant.EMP_NO)!!,
            "DOCNUMBER" to sharedPreference.getValueString(Constant.DOCUMENT)!!,
            "DOCTYPE" to docType,
            "BARCODENO" to barcode,

            )
        viewModel.sendExtraScan(mScanDocDataBody)
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    private fun getData(item: String,cNote: String,is_match:Boolean): VehicleData {
        val stockData = VehicleData()
        stockData.bar_code = item
        stockData.cNote = cNote
        stockData.isMatch = is_match

        return stockData
    }

    private fun checkListSize(){
        if(vehicleData.size>=50){
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Upload your data")
                .setContentText("Please submit scan data and rescan again")
                .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener { sweetAlert ->
                    sweetAlert.dismiss()
                    showDialog(false)
                    getJSon(vehicleData)
                })
                .show()

        }else{
            if (isCamera) startScanning()
        }
    }

    fun getJSon(scanCode: List<VehicleData>) {

        var dataRequest = ArrayList<VehicleLoadRequest.Data>()
        for (value in scanCode) {
            dataRequest.add(VehicleLoadRequest.Data(value.bar_code?.trim()!!,value.cNote?.trim()!!))
        }

        var vecicleloadRequst = VehicleLoadRequest(sharedPreference.getValueString(Constant.CID)!!,
            sharedPreference.getValueString(Constant.BID)!!,
            sharedPreference.getValueString(Constant.DOCUMENT)!!,
            sharedPreference.getValueString(Constant.DOCTYPE)!!,
            sharedPreference.getValueString(Constant.EMP_NO)!!,
            dataRequest)
        var jsonData = Gson().toJson(vecicleloadRequst)
        Log.d("jsonData",jsonData)
        viewModel.uploadNewVehicleScan(vecicleloadRequst)
    }

    fun getDocumentNo(): String {
       /* var documentno = ""
        when (docType) {
            "PRS" ->{
                documentno =
                    "${binding.documentNo1.text.toString()}${binding.documentNo2.text.toString()}-${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            }
            "MFIN" -> {
                "${binding.documentNo1.text.toString()}${binding.documentNo2.text.toString()}-${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            }
            "MFOUT" -> {
                documentno =
                    "${binding.documentNo1.text.toString()}${binding.documentNo2.text.toString()}-${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            }
            "DRS", "DRS" ->{
                documentno =
                    "${binding.documentNo1.text.toString()}${binding.documentNo2.text.toString()}-${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            }
            else -> {
                documentno =
                    "${binding.documentNo1.text.toString()}${binding.documentNo2.text.toString()}-${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            }
        }*/
        return "${binding.documentNo1.text.toString()}${binding.documentNo2.text.toString()}-${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
    }

    private fun setRecylatView(data: MutableList<VehicleListData>) {
        var count = 0
        data.forEach{it->
            if(it.isScan == true)  count++
        }
        binding.scan.text = "$count"
        data.sortBy { it.isScan==true }
        if (data.isNotEmpty()) {
            Log.d(TAG, data.size.toString())
            binding.constraintLayout.visibility = View.VISIBLE
            binding.submit.visibility = View.VISIBLE
            (binding.stockListRecyclerview.adapter as VechileLoadAdapter).setItems(
                data as List<VehicleListData>,this
            )
        } else {
            binding.constraintLayout.visibility = View.GONE
            binding.submit.visibility = View.GONE
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts tts.setLanguage(new Locale("hi"));
            val result = tts!!.setLanguage(Locale("hi", "IN"))
            tts!!.setPitch(1.0f) // saw from internet
            tts!!.setSpeechRate(0.8f)

            val voices = tts!!.voices
            for (voice in voices) {
                Log.v(TAG, voice.name)
                if (voice.name == "hi-in-x-cfn#female_2-local") {
                    tts!!.voice = voice
                }
            }
            //tts!!.voice = Voice(Raw)// f denotes float, it actually type casts 0.5 to float
            //tts!!.setLanguage(Locale.US);


            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                //buttonSpeak!!.isEnabled = true
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }


}