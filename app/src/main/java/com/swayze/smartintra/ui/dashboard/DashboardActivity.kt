package com.swayze.smartintra.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.FileProvider
import androidx.core.os.postDelayed
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.swayze.smartintra.app.BaseActivity
import com.swayze.smartintra.R
import com.swayze.smartintra.app.Constant
import com.swayze.smartintra.app.ManagePermissions
import com.swayze.smartintra.databinding.ActivityDashboardBinding
import com.swayze.smartintra.ui.bluetooth_device.SetupDeviceActivity
import com.swayze.smartintra.ui.login.LoginActivity
import com.swayze.smartintra.ui.pod_upload.PodUploadActivity
import com.swayze.smartintra.ui.trip_sheet_printing.TripSheetPrintingActivity
import com.swayze.smartintra.ui.vehicle_load_unload.VehicleLoadUnloadActivity
import com.swayze.smartintra.util.Utils

import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.os.Handler
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import com.swayze.smartintra.network.ViewModalFactory
import com.swayze.smartintra.ui.attendance.AttendanceViewModel
import com.swayze.smartintra.ui.pod_upload.PodUploadViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody


class DashboardActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private var checkIn = true
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var managePermissions: ManagePermissions
    private var compressedImage: File? = null
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )
    var mediaPath: String = "/storage/emulated/0/Android/data/com.swayze.smartintra/files/Pictures/"
    private val REQUEST_CAMERA_CAPTURE = 1001
    private var mCurrentPhotoPath:String?=null
    private var path: String? = null
    var date: Date? = null
    var simpleDateFormat: SimpleDateFormat? = null

    private var timerHandler: Handler? = null
    private var timerRunnable: Runnable? = null
    private lateinit var viewModel: AttendanceViewModel
    private var today: String = Utils.getDate("dd/MM/yyyy")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.navView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, binding.main, binding.toolbar, R.string.open_nav, R.string.close_nav)
        binding.main.addDrawerListener(toggle)
        toggle.syncState()
        viewModel = ViewModelProvider(this, ViewModalFactory(application))[AttendanceViewModel::class.java]

        /*if(sharedPreference.getValueBoolean(Constant.CHECK_IN,false)!=null){
            checkIn = sharedPreference.getValueBoolean(Constant.CHECK_IN,false)
        }else{
             sharedPreference.save(Constant.CHECK_IN,true)
        }*/
        //val savedCheckIn = sharedPreference.getValueBoolean(Constant.CHECK_IN, true)
        //sharedPreference.save(Constant.CHECK_IN, savedCheckIn)
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)

        binding.tvDayAndDate.text = Utils.getDayAndDate()
        binding.cvVehicleLoadUnload.setOnClickListener(this)
        binding.cvPodUpload.setOnClickListener(this)
        binding.ivLogout.setOnClickListener(this)
        binding.ivAttendance.setOnClickListener(this)


        setObservers()

    }

    override fun onResume() {
        super.onResume()
        viewModel.getDateTime()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    @SuppressLint("DefaultLocale")
    private fun setData() {
        if(isNewDate()){
            sharedPreference.save(Constant.CURRENT_DATE, Utils.getDate("dd/MM/yyyy"))
            sharedPreference.save(Constant.CHECK_IN, false)
            sharedPreference.save(Constant.CHECK_OUT, false)
            binding.tvCheckInTime.text = "--:--:--"
            binding.tvCheckOutTime.text = "--:--:--"
            binding.tvTimeHour.text = String.format("%02d", 0)
            binding.tvTimeMinute.text = String.format("%02d", 0)
            binding.tvTimeSecond.text = String.format("%02d", 0)
            binding.ivAttendance.visibility = View.VISIBLE
        }else{
            val isCheckInDone = sharedPreference.getValueBoolean(Constant.CHECK_IN, false)
            val isCheckOutDone = sharedPreference.getValueBoolean(Constant.CHECK_OUT, false)

            if(isCheckInDone){
                val checkInTime = sharedPreference.getValueLong(Constant.CHECK_IN_TIME,0L)
                val elapsed = System.currentTimeMillis() - checkInTime
                updateTimerUI(elapsed)
            }

            if(isCheckInDone && isCheckOutDone){
                val checkOutTime = sharedPreference.getValueLong(Constant.CHECK_OUT_TIME,0L)
                val checkInTime = sharedPreference.getValueLong(Constant.CHECK_IN_TIME,0L)
                val elapsed = checkOutTime - checkInTime
                updateTimerUI(elapsed)
                binding.ivAttendance.visibility = View.INVISIBLE
            }else binding.ivAttendance.visibility = View.VISIBLE

            binding.tvCheckInTime.text = if(isCheckInDone) Utils.getDate(sharedPreference.getValueLong(Constant.CHECK_IN_TIME,0L), "HH:mm:ss") else "--:--:--"
            binding.tvCheckOutTime.text = if(isCheckOutDone) Utils.getDate(sharedPreference.getValueLong(Constant.CHECK_OUT_TIME,0L), "HH:mm:ss") else "--:--:--"
        }
        resumeTimerIfNeeded()
    }

    private fun setObservers() {
        viewModel.empAttendanceResponse.observe(this, Observer {
            hideDialog()
            val attendance = it ?: return@Observer
            Log.d("respose",Gson().toJson(attendance))
            if(attendance[0].Response.equals("Success")){
                if(attendance[0].marktype.equals("IN", ignoreCase = true)){
                    sharedPreference.save(Constant.CHECK_IN, true)
                    sharedPreference.save(Constant.CHECK_IN_TIME, Utils.getTimeInMillis(attendance[0].marktime!!,"dd/MM/yyyy HH:mm:ss"))
                    binding.tvCheckInTime.text = attendance[0].marktime?.substringAfter(" ")
                    startTimer()

                }else{
                    sharedPreference.save(Constant.CHECK_OUT, true)
                    binding.ivAttendance.visibility = View.INVISIBLE
                    sharedPreference.save(Constant.CHECK_OUT_TIME, Utils.getTimeInMillis(attendance[0].marktime!!,"dd/MM/yyyy HH:mm:ss"))
                    binding.tvCheckOutTime.text = attendance[0].marktime?.substringAfter(" ")
                    stopTimer()
                }
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getString(R.string.success))
                    .setContentText(getString(R.string.your_attendance_done))
                    .setConfirmClickListener { sDialog -> // reuse previous dialog instance
                        sDialog.dismiss()
                    }
                    .show()
            }else{
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error))
                    .setContentText(attendance[0].Response)
                    .setConfirmClickListener { sDialog -> // reuse previous dialog instance
                        sDialog.dismiss()
                    }
                    .show()
            }
        })

        viewModel.getDateTimeResponse.observe(this, Observer {
            hideDialog()
            val attendance = it ?: return@Observer
            today = attendance[0].currTime.substringBefore(" ")
            binding.tvDayAndDate.text = Utils.getDayFromDate(attendance[0].currTime,"dd/MM/yyyy HH:mm:ss")
            Log.d("respose",Gson().toJson(attendance))
            setData()
        })
    }

    override fun onNavigationItemSelected(menu: MenuItem): Boolean {
        when(menu.itemId) {
            R.id.trip_sheet_printing->{
                startActivity(Intent(this, TripSheetPrintingActivity::class.java))
            }
            R.id.nav_setup_device->{
                startActivity(Intent(this, SetupDeviceActivity::class.java))
            }
            R.id.nav_logout -> {
                sharedPreference.clearSharedPreference()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        binding.main.closeDrawer(GravityCompat.START)
        return true
    }

    private fun checkCurrentDate(): Boolean {
        simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        date = Calendar.getInstance().time
        val currentDate = simpleDateFormat?.format(date)
        val savedCurrentDate = sharedPreference.getValueString(Constant.CURRENT_DATE)
        Log.d("current_date",currentDate + savedCurrentDate)
        currentDate?.let { sharedPreference.save(Constant.CURRENT_DATE,it) }
        return currentDate!=savedCurrentDate


    }

    private fun isNewDate(): Boolean {
        /*val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val today = sdf.format(Date())*/
        val savedDate = sharedPreference.getValueString(Constant.CURRENT_DATE)

        return today != savedDate
    }

    fun markAttendance() {
        //val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        //val today = sdf.format(Date())

        val savedDate = sharedPreference.getValueString(Constant.CURRENT_DATE)
        var isCheckInDone = sharedPreference.getValueBoolean(Constant.CHECK_IN, false)
        var isCheckOutDone = sharedPreference.getValueBoolean(Constant.CHECK_OUT, false)

        // 🔁 New day → reset attendance
        if (today != savedDate) {
            sharedPreference.save(Constant.CURRENT_DATE, today)
            sharedPreference.save(Constant.CHECK_IN, false)
            sharedPreference.save(Constant.CHECK_OUT, false)
            isCheckInDone = false
            isCheckOutDone = false
        }



        when {
            !isCheckInDone -> {
                //onCheckIn()
                markSalesAttendance("IN")
            }

            !isCheckOutDone -> {
                markSalesAttendance("OUT")
                //onCheckOut()
            }

            else -> {
                showToast("Attendance already completed for today")
            }
        }
    }

    private fun onCheckIn() {
        sharedPreference.save(Constant.CHECK_IN, true)
        markSalesAttendance("IN")

    }

    private fun onCheckOut() {
        sharedPreference.save(Constant.CHECK_OUT, true)
        binding.tvCheckOutTime.text = Utils.getDate("HH:mm:ss")
        //showToast("Check-Out marked successfully")
        val checkInTime = System.currentTimeMillis()
        sharedPreference.save(Constant.CHECK_OUT_TIME, checkInTime)
        binding.ivAttendance.visibility = View.INVISIBLE
        markSalesAttendance("OUT")
        //resumeTimerIfNeeded()
        stopTimer()
    }



    private fun startTimer() {
        stopTimer()

        timerHandler = Handler(Looper.getMainLooper())

        timerRunnable = object : Runnable {
            override fun run() {

                val isCheckoutDone = sharedPreference.getValueBoolean(Constant.CHECK_OUT, false)

                // 🛑 STOP immediately if checkout done
                if (isCheckoutDone) {
                    stopTimer()
                    return
                }

                val checkInTime = sharedPreference.getValueLong(Constant.CHECK_IN_TIME,0L)
                val elapsed = System.currentTimeMillis() - checkInTime

                updateTimerUI(elapsed)

                timerHandler?.postDelayed(this, 1000)
            }
        }

        timerHandler?.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerRunnable?.let {
            timerHandler?.removeCallbacks(it)
        }
        timerHandler = null
        timerRunnable = null
    }

    private fun resumeTimerIfNeeded() {
        val isCheckIn = sharedPreference.getValueBoolean(Constant.CHECK_IN, false)
        val isCheckOut = sharedPreference.getValueBoolean(Constant.CHECK_OUT, false)

        if (isCheckIn && !isCheckOut) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    private fun updateTimerUI(elapsedMillis: Long) {
        val seconds = (elapsedMillis / 1000) % 60
        val minutes = (elapsedMillis / (1000 * 60)) % 60
        val hours = (elapsedMillis / (1000 * 60 * 60))
        binding.tvTimeHour.text = String.format("%02d", hours)
        binding.tvTimeMinute.text = String.format("%02d", minutes)
        binding.tvTimeSecond.text = String.format("%02d", seconds)
       // val time = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        //binding.tvTimer.text = time
    }

    private fun markSalesAttendance(attenType: String) {
        showDialog()
        val file = File(path)
        Log.d(TAG,"image_name : ${file.name}")
        val filePart = MultipartBody.Part.createFormData(
            "dataFile",
            file.name,
            RequestBody.create("image/*".toMediaTypeOrNull(), file)
        )
        viewModel.empAttendance(
            filePart,
            sharedPreference.getValueString(Constant.CID)?.let { getPart(it) },
            sharedPreference.getValueString(Constant.BID)?.let { getPart(it) },
            sharedPreference.getValueString(Constant.EMP_NO)?.let { getPart(it) },
            getDeviceIMEIId(this)?.let { getPart(it)},
            getPart(attenType)
        )
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            binding.ivAttendance.id->{
                if (!managePermissions.checkPermissions()) return
                takePicture()

            }
            binding.cvVehicleLoadUnload.id->{
                val intent = Intent(this, VehicleLoadUnloadActivity::class.java)
                startActivity(intent)
            }
            binding.cvPodUpload.id->{
                val intent = Intent(this, PodUploadActivity::class.java)
                startActivity(intent)
            }
            binding.ivLogout.id->{
                sharedPreference.clearSharedPreference()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun takePicture() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()
        Log.e("actual file path", file.path)
        val uri: Uri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, REQUEST_CAMERA_CAPTURE)

    }

    private fun createFile(): File {
        // Create an image file name
        Log.d(
            "temp_file",
            "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${Utils.getDate("ddMMyyyyHHmmss")}_"
        )
        //val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${Utils.getDate("dddMMyyyyHHmmss")}", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            try {
                when (requestCode) {
                    REQUEST_CAMERA_CAPTURE -> {
                        // Existing camera capture logic
                        if (mCurrentPhotoPath != null) {
                            mCurrentPhotoPath?.let {
                                val auxFile = File(it)
                                customCompressImage(auxFile)
                            }
                        } else {
                            showToast("File Empty! Try Again")
                            Log.e("file_path", "File Empty! Try Again")
                        }
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun customCompressImage(actualImage: File) {
        actualImage.let { imageFile ->
            lifecycleScope.launch {
                try {
                    // Get original image dimensions before compression
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(imageFile.absolutePath, options)
                    val originalWidth = options.outWidth
                    val originalHeight = options.outHeight

                    // Calculate better resolution that maintains aspect ratio
                    val maxDimension = 1280 // Higher resolution than before
                    val scaledWidth: Int
                    val scaledHeight: Int

                    if (originalWidth > originalHeight) {
                        scaledWidth = maxDimension
                        scaledHeight =
                            (originalHeight * maxDimension / originalWidth.toFloat()).toInt()
                    } else {
                        scaledHeight = maxDimension
                        scaledWidth =
                            (originalWidth * maxDimension / originalHeight.toFloat()).toInt()
                    }

                    val fileName =
                        "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${Utils.getDate("d/MM/yyyyHH:mm")}_compressed.jpg"
                    val destination = File(imageFile.parent, fileName)

                    compressedImage = Compressor.compress(this@DashboardActivity, imageFile) {
                        resolution(scaledWidth, scaledHeight)
                        destination(destination)
                        quality(80) // Higher quality setting
                        format(Bitmap.CompressFormat.JPEG)
                        // Remove the size constraint or make it larger
                        size(1_048_576) // 1 MB instead of 180KB
                    }
                    setCompressedImage()

                } catch (e: Exception) {
                    Log.e("ImageCompression", "Error compressing image: ${e.message}", e)
                    Toast.makeText(
                        this@DashboardActivity,
                        "Failed to compress image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun setCompressedImage() {
            compressedImage?.let {
                try {


                    // Use BitmapFactory options to prevent OOM errors with large images
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 1 // No downsampling when displaying
                    }

                    val bitmap = BitmapFactory.decodeFile(it.path, options)

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val to = File(
                        storageDir,
                        "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${Utils.getDate("dd/MM/yyyyHH:mm")}.jpg"
                    )

                    // Copy the compressed image to the final location
                    it.copyTo(to, overwrite = true)

                    path = to.absolutePath
                    val fileSizeInBytes = to.length()
                    val fileSizeInKB = fileSizeInBytes / 1024
                    val fileSizeInMB = fileSizeInKB / 1024.0

                    Log.d("image_compression", "Compressed image size: $fileSizeInBytes bytes")
                    Log.d("image_compression", "Compressed image size: $fileSizeInKB KB")
                    Log.d(
                        "image_compression",
                        "Compressed image size: ${String.format("%.2f", fileSizeInMB)} MB"
                    )
                    Log.d("final_path", path ?: "Path is null")
                    markAttendance()
                } catch (e: Exception) {
                    Log.e("ImageDisplay", "Error displaying image: ${e.message}", e)
                }
            } ?: run {
                Log.e("ImageDisplay", "Compressed image is null")
            }
        }

}