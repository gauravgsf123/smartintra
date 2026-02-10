package com.swayze.smartintra.ui.pod_upload

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_BASE
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.swayze.smartintra.R
import com.swayze.smartintra.app.BaseActivity
import com.swayze.smartintra.app.Constant
import com.swayze.smartintra.app.ManagePermissions
import com.swayze.smartintra.databinding.ActivityPodUploadBinding
import com.swayze.smartintra.network.ViewModalFactory
import com.swayze.smartintra.ui.vehicle_load_unload.VehicleLoadUnloadModel
import com.swayze.smartintra.util.Utils
import com.swayze.smartintra.util.qr_code_scanner.QRcodeScanningActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.get
import kotlin.toString

class PodUploadActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityPodUploadBinding
    private lateinit var podDelayReasonResponse: PODDelayReasonResponse
    private var pickupType:String = ""
    private var deliveryType:String = ""
    private var reason:String = ""
    private var rejectReason:String=""
    private lateinit var requestNo:String

    private lateinit var managePermissions: ManagePermissions
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_CAMERA_CAPTURE = 1002
    private val REQUEST_GALLERY_CAPTURE = 1003

    private val REQUEST_DOCUMENT_SCAN = 1004
    private val REQUEST_GALLERY_CAPTURE_DOCUMENT_ONE = 1005
    private val REQUEST_GALLERY_CAPTURE_DOCUMENT_TWO = 1006
    private var compressedImage: File? = null
    private var mCurrentPhotoPath: String? = null
    private val permissionsRequestCode = 123
    private var path:String?=null
    private var documentOnePath:String?=null
    private var documentTwoPath:String?=null
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )
    var mediaPath: String = "/storage/emulated/0/Android/data/com.mpcl/files/Pictures/"

    private var minDate = Utils.getDate("dd/MM/yyyy")
    private var isDelay = false
    private lateinit var podDateLimitResponse: PodDateLimitResponse
    private var barCode = ""
    private val documentScannerOptions = GmsDocumentScannerOptions.Builder()
        .setResultFormats(RESULT_FORMAT_JPEG)
        .setScannerMode(SCANNER_MODE_BASE)
        .build()
    private val documentScanner = GmsDocumentScanning.getClient(documentScannerOptions)

    private lateinit var barcodeScanner: BarcodeScanner
    private var otherFiles: Int? = 0

    private lateinit var viewModel: PodUploadViewModel
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPodUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS  // Detect all barcode formats
            )
            .build()

        barcodeScanner = BarcodeScanning.getClient(options)
        viewModel = ViewModelProvider(
            this,
            ViewModalFactory(application)
        )[PodUploadViewModel::class.java]
        binding.topBar.ivHome.setOnClickListener { onBackPressed() }
        binding.topBar.appBarTitle.text = resources.getString(R.string.pod_upload)

        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        val pickupStatusOption = resources.getStringArray(R.array.pod_status_type)
        val pickupStatusOptionAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, pickupStatusOption)
        binding.type.setAdapter(pickupStatusOptionAdapter)
        binding.type.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (!binding.type.isPopupShowing) {
                    binding.type.showDropDown()
                }
            }
            false
        }

        var body = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!
        )
        //pickupViewModel.pickupTypeReason(body)
        //viewModel.delayReason(body)
        showDialog()

        binding.type.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            rejectReason = ""
            //deliveryType = ""
            binding.cNoteNumber.setText("")
            // binding.deliveryType.setText("", false)
            binding.reason.setText("", false)

            when (parent.getItemAtPosition(position)) {
                "Delivered" -> {
                    binding.cNoteNumber.setText("")
                    binding.groupUndelivered.visibility = View.GONE
                    binding.groupRTO.visibility = View.GONE
                    binding.groupImage.visibility = View.VISIBLE
                    pickupType = parent.getItemAtPosition(position) as String

                    //binding.tvCalender.text = podDateLimitResponse.DrsDate!!

                    if (!isDelay) {
                        binding.groupDeliveredDelay.visibility = View.GONE
                        binding.groupDelivered.visibility = View.VISIBLE
                    } else {
                        binding.groupDelivered.visibility = View.GONE
                        binding.groupDeliveredDelay.visibility = View.VISIBLE

                        binding.reason.setAdapter(null)
                        binding.reason.setText("", false)

                        // Create a list of DelayReasonItem objects from podDelayReasonResponse
                        /*val delayReasonItems = podDelayReasonResponse.DELINTERNAL.map { item ->
                            val parts = item.toString()
                            val remid = extractRemid(parts) ?: ""
                            val remarks = extractRemarks(parts) ?: ""
                            DelayReasonItemPod(remid, remarks)
                        }

                        val pickupNotDoneReasonAdapter = ArrayAdapter(
                            this,
                            R.layout.drop_down_list_item,
                            delayReasonItems.map { "${it.remarks} " }
                        )
                        binding.reason.setAdapter(pickupNotDoneReasonAdapter)
                        binding.reason.setOnClickListener {
                            binding.reason.showDropDown()
                        }
                        var selectedRemark: String = ""
                        binding.reason.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                            val selectedItem = delayReasonItems[position]
                            selectedRemark = selectedItem.remarks
                            val selectedRemid = selectedItem.remid
                            // Use selectedRemid as needed
                            rejectReason = selectedRemid ?: ""
                        }*/

                    }

                }
                "Undelivered" -> {
                    binding.cNoteNumber.setText("")
                    binding.groupDelivered.visibility = View.GONE
                    binding.groupRTO.visibility = View.GONE
                    binding.groupImage.visibility = View.GONE
                    binding.groupUndelivered.visibility = View.VISIBLE
                    pickupType = parent.getItemAtPosition(position) as String

                    // Clear the adapter when Undelivered is selected
                    binding.reason.setAdapter(null)
                    binding.reason.setText("", false)

                    // Create a list of DelayReasonItem objects from podDelayReasonResponse
                    /*val delayReasonItems = podDelayReasonResponse.DELINTERNAL.map { item ->
                        val parts = item.toString()
                        val remid = extractRemid(parts) ?: ""
                        val remarks = extractRemarks(parts) ?: ""
                        DelayReasonItemPod(remid, remarks)
                    }

                    val pickupNotDoneReasonAdapter = ArrayAdapter(
                        this,
                        R.layout.drop_down_list_item,
                        delayReasonItems.map { "${it.remarks} " }
                    )
                    binding.reason.setAdapter(pickupNotDoneReasonAdapter)

                    var selectedRemark: String = ""
                    binding.reason.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                        val selectedItem = delayReasonItems[position]
                        selectedRemark = selectedItem.remarks
                        val selectedRemid = selectedItem.remid
                        // Use selectedRemid as needed
                        rejectReason = selectedRemid ?: ""
                    }*/

                }



                "R.T.O" -> {
                    binding.groupDelivered.visibility = View.GONE
                    binding.groupUndelivered.visibility = View.GONE
                    binding.groupImage.visibility = View.GONE
                    binding.groupRTO.visibility = View.VISIBLE
                    // deliveryType = ""
                    pickupType = parent.getItemAtPosition(position) as String
                    rejectReason = ""
                }
                else -> pickupType = ""
            }
        }



        binding.reason.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (!binding.reason.isPopupShowing) {
                    binding.reason.showDropDown()
                }
            }
            false
        }
        binding.reason.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                rejectReason = (parent.getItemAtPosition(position).toString())
            }
        binding.imgBarCode.setOnClickListener(this)
        binding.btnPhoto.setOnClickListener(this)
        binding.btnGallery.setOnClickListener(this)
        binding.tvDocOne.setOnClickListener(this)
        binding.tvDocTwo.setOnClickListener(this)
        binding.save.setOnClickListener(this)
        binding.tvCalender.setOnClickListener(this)

        binding.etBarCode.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            val inputMethod: InputMethodManager =
                v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethod != null) {
                inputMethod.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        binding.etBarCode.setOnFocusChangeListener {
                view, b ->
            val inputMethod: InputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if(b) inputMethod.hideSoftInputFromWindow(view.windowToken, 0)
        }

        setObserver()
    }

    fun extractRemid(input: String): String? {
        val remidRegex = Regex("remid\\s*=\\s*(\\S+)")
        val remidMatch = remidRegex.find(input)
        val remids = remidMatch?.groups?.get(1)?.value
        return  remids?.removeSuffix(",")
    }



    fun extractRemarks(input: String): String? {
        val remarksRegex = Regex("remarks\\s*=\\s*([^,]+(?:,\\s*[^,]+)*)")
        val remarksMatch = remarksRegex.find(input)
        val remarks = remarksMatch?.groups?.get(1)?.value
        return remarks?.removeSuffix(")")
    }

    override fun onPostResume() {
        super.onPostResume()

        if(sharedPreference.getValueString("result")?.isNotEmpty() == true){
            var str = sharedPreference.getValueString("result")
            binding.etBarCode.setText(str)
            Log.d("etBarCode","$str")
            getLimitDate(str!!.trim())
            sharedPreference.removeValue("result")
        }
    }

    private fun setObserver() {

    }

    private fun reset(){
        binding.groupDelivered.visibility = View.GONE
        binding.groupRTO.visibility = View.GONE
        binding.groupUndelivered.visibility = View.GONE
        binding.groupImage.visibility = View.GONE
        binding.type.visibility = View.GONE
        binding.save.isEnabled = false
        binding.tvCalender.text = ""
        binding.cNoteNumber.setText("")
        //binding.deliveryType.setText("",false)
        binding.reason.setText("",false)
        binding.type.setText(resources.getString(R.string.delivered),false)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.imgBarCode -> {
                selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
                startScanning()
            }
            R.id.btnPhoto -> {
//                val b = managePermissions.checkPermissions()
//                if (b) {
//                    takePicture()
//                }
                startDocumentScanning()
            }
            R.id.btnGallery->{
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, REQUEST_GALLERY_CAPTURE)
                }
            }
            R.id.tvDocOne->{
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, REQUEST_GALLERY_CAPTURE_DOCUMENT_ONE)
                }
            }
            R.id.tvDocTwo->{
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, REQUEST_GALLERY_CAPTURE_DOCUMENT_TWO)
                }
            }
//            R.id.ivDocumentScan -> {
//                startDocumentScanning()
//            }
            R.id.tvCalender->showCalender()
            R.id.save->{
                validateForm()
            }
        }
    }

    private fun startDocumentScanning() {
        documentScanner.getStartScanIntent(this)
            .addOnSuccessListener { intentSender ->
                startIntentSenderForResult(
                    intentSender,
                    REQUEST_DOCUMENT_SCAN,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error starting document scanner: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getLimitDate(docNumber: String) {
        val mScanDocDataBody = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "DOCNUMBER" to docNumber
        )
        showDialog()
        Log.d(TAG,mScanDocDataBody.toString())
        viewModel.getLimitDate(mScanDocDataBody)
    }


    private fun showCalender(){
        var mcurrentDate = Calendar.getInstance()
        val mYear: Int = mcurrentDate.get(Calendar.YEAR)
        val mMonth: Int = mcurrentDate.get(Calendar.MONTH)
        val mDay: Int = mcurrentDate.get(Calendar.DAY_OF_MONTH)
        var defaultDate = ""//podDateLimitResponse.DrsDate!!.toString().split(Regex("/"))
        var dd = defaultDate[0].toInt()
        var mm = defaultDate[1].toInt()
        var yy = defaultDate[2].toInt()
        val mDatePicker = DatePickerDialog(
            this@PodUploadActivity,
            { datepicker, selectedyear, selectedmonth, selectedday ->
                mcurrentDate.set(Calendar.YEAR, selectedyear)
                mcurrentDate.set(Calendar.MONTH, selectedmonth)
                mcurrentDate.set(
                    Calendar.DAY_OF_MONTH,
                    selectedday
                )
                val sdf = SimpleDateFormat(
                    resources.getString(
                        R.string.date_card_formate
                    ),
                    Locale.US
                )
                binding.tvCalender.text = sdf.format(
                    mcurrentDate.time
                )
                isDelay = Utils.checkDate(podDateLimitResponse.EdDate!!,binding.tvCalender.text.toString())
                if(!isDelay){
                    binding.groupDeliveredDelay.visibility = View.GONE
                    binding.groupDelivered.visibility = View.VISIBLE
                }else{
                    binding.groupDelivered.visibility = View.GONE
                    binding.groupDeliveredDelay.visibility = View.VISIBLE

                    binding.reason.setAdapter(null)
                    binding.reason.setText("", false)

                    // Create a list of DelayReasonItem objects from podDelayReasonResponse
                    val delayReasonItems = podDelayReasonResponse.DELINTERNAL.map { item ->
                        val parts = item.toString()
                        val remid = extractRemid(parts) ?: ""
                        val remarks = extractRemarks(parts) ?: ""
                        DelayReasonItemPod(remid, remarks)
                    }

                    val pickupNotDoneReasonAdapter = ArrayAdapter(
                        this,
                        R.layout.drop_down_list_item,
                        delayReasonItems.map { "${it.remarks} " }
                    )
                    binding.reason.setAdapter(pickupNotDoneReasonAdapter)

                    var selectedRemark: String = ""
                    binding.reason.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                        val selectedItem = delayReasonItems[position]
                        selectedRemark = selectedItem.remarks
                        val selectedRemid = selectedItem.remid
                        // Use selectedRemid as needed
                        rejectReason = selectedRemid ?: ""
                    }
                }

            }, mYear, mMonth, mDay
        )
        //mDatePicker.updateDate(Utils.getCurrentDate(""))
        mDatePicker.setTitle(
            resources.getString(
                R.string.alert_date_select
            )
        )

        mDatePicker.datePicker.maxDate = System.currentTimeMillis()
        if(podDateLimitResponse.Response!="Failed") {
            mDatePicker.datePicker.minDate = Utils.milliseconds(podDateLimitResponse.DrsDate!!)
            mDatePicker.updateDate(yy, mm-1, dd)
        }
        mDatePicker.show()
    }

    private fun validateForm() {
        when {
            binding.etBarCode.text?.isNotBlank() == false -> {
                showError(
                    getString(R.string.opps),
                    "Please Scan Code"
                )
            }
            pickupType.isNullOrEmpty() -> {
                showError(
                    resources.getString(R.string.opps),
                    "Please Select Pickup Type"
                )
            }
            // Remove the general path == null check from here
            pickupType==resources.getString(R.string.delivered)->{
                if(isDelay && rejectReason.isNullOrEmpty()){
                    showError(
                        getString(R.string.opps),
                        "Please Select Delay Reason"
                    )
                }else if(path==null){  // Only check for photo if it's a "Delivered" type
                    showError(
                        getString(R.string.opps),
                        "Please Take Photo"
                    )
                }else if(otherFiles==1) {
                    if (documentOnePath.isNullOrEmpty() && documentTwoPath.isNullOrEmpty()) {
                        showError(
                            getString(R.string.opps),
                            "Please select both document"
                        )
                    } else if(documentOnePath.isNullOrEmpty()){
                        showError(
                            getString(R.string.opps),
                            "Please select document one"
                        )
                    }else if(documentTwoPath.isNullOrEmpty()){
                        showError(
                            getString(R.string.opps),
                            "Please select document two"
                        )
                    }else updateLocation()
                } else updateLocation()
            }
            pickupType==resources.getString(R.string.undelivered)->{
                if(rejectReason.isNullOrEmpty()){
                    showError(
                        getString(R.string.opps),
                        "Please Select Delay Reason"
                    )
                }else if(otherFiles==1) {
                    if (documentOnePath.isNullOrEmpty() && documentTwoPath.isNullOrEmpty()) {
                        showError(
                            getString(R.string.opps),
                            "Please select both document"
                        )
                    } else if(documentOnePath.isNullOrEmpty()){
                        showError(
                            getString(R.string.opps),
                            "Please select document one"
                        )
                    }else if(documentTwoPath.isNullOrEmpty()){
                        showError(
                            getString(R.string.opps),
                            "Please select document two"
                        )
                    }else updateLocation()
                } else updateLocation()
            }
            pickupType==resources.getString(R.string.rto)->{
                if(binding.cNoteNumber.text?.isNullOrEmpty()==true){
                    showError(
                        getString(R.string.opps),
                        "Please Enter C-Note Number")
                }else if(otherFiles==1) {
                    if (documentOnePath.isNullOrEmpty() && documentTwoPath.isNullOrEmpty()) {
                        showError(
                            getString(R.string.opps),
                            "Please select both document"
                        )
                    } else if(documentOnePath.isNullOrEmpty()){
                        showError(
                            getString(R.string.opps),
                            "Please select document one"
                        )
                    }else if(documentTwoPath.isNullOrEmpty()){
                        showError(
                            getString(R.string.opps),
                            "Please select document two"
                        )
                    }else updateLocation()
                } else updateLocation()
            }
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

    private fun updateLocation() {

    }
}