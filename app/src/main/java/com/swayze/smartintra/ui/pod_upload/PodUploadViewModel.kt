package com.swayze.smartintra.ui.pod_upload

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swayze.smartintra.ui.login.LoginRepository
import com.swayze.smartintra.util.APIResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PodUploadViewModel(private val app: Application) :ViewModel() {
    private var mContext: Context = app.applicationContext
    private val repository = PodUploadRepository()
    val responseBarCode: MutableLiveData<List<APIResponse>?> = MutableLiveData()
    val responseLimitDate: MutableLiveData<List<PodDateLimitResponse>?> = MutableLiveData()
    var podDelayReasonResponse: MutableLiveData<List<PODDelayReasonResponse>?> = MutableLiveData()
    fun uploadAcCopyData(
        filePart: MultipartBody.Part,
        cid: RequestBody?,
        empNo: RequestBody?,
        mobile: RequestBody?,
        bid: RequestBody?,
        docNo: RequestBody?,
        deviceImei: RequestBody?
    ) {
        viewModelScope.launch {
            try {
                val response = repository.uploadAcCopyData(
                    filePart,
                    cid,
                    empNo,
                    mobile,
                    bid,
                    docNo,
                    deviceImei
                )
                responseBarCode.value = response

            } catch (e: Exception) {
                Log.d("main", "getPost: ${e.message}")
            }
        }
    }

    fun uploadPODCopyData(
        filePart: MultipartBody.Part,
        documentOneFilePart: MultipartBody.Part,
        documentTwoFilePart: MultipartBody.Part,
        cid: RequestBody?,
        empNo: RequestBody?,
        mobile: RequestBody?,
        bid: RequestBody?,
        docNo: RequestBody?,
        date: RequestBody?,
        deviceImei: RequestBody?,
        statusType: RequestBody?,
        reasonType: RequestBody?,
        reason: RequestBody?,
        cNoteNumber: RequestBody?
    ) {
        viewModelScope.launch {
            try {
                val response = repository.uploadPODCopyData(
                    filePart,
                    documentOneFilePart,
                    documentTwoFilePart,
                    cid,
                    empNo,
                    mobile,
                    bid,
                    docNo,
                    date,
                    deviceImei,
                    statusType,
                    reasonType,
                    reason,
                    cNoteNumber
                )
                responseBarCode.value = response

            } catch (e: Exception) {
                Log.d("main", "getPost: ${e.message}")
            }
        }
    }

    fun uploadPODCopyData(
        filePart: MultipartBody.Part,
        cid: RequestBody?,
        empNo: RequestBody?,
        mobile: RequestBody?,
        bid: RequestBody?,
        docNo: RequestBody?,
        date: RequestBody?,
        deviceImei: RequestBody?
    ) {
        viewModelScope.launch {
            try {
                val response = repository.uploadPODCopyData(
                    filePart,
                    cid,
                    empNo,
                    mobile,
                    bid,
                    docNo,
                    date,
                    deviceImei
                )
                responseBarCode.value = response

            } catch (e: Exception) {
                Log.d("main", "getPost: ${e.message}")
            }
        }
    }

    fun getLimitDate(body: Map<String, String>) {
        viewModelScope.launch {
            var response = repository.getLimitDate(body)
            responseLimitDate.value = response
        }
    }

    fun delayReason(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = repository.delayReason(body)
                podDelayReasonResponse.value = response
            } catch (e: java.lang.Exception) {
            }
        }
    }
}