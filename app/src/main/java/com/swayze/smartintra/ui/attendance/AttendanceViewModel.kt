package com.swayze.smartintra.ui.attendance

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swayze.smartintra.util.APIResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AttendanceViewModel(private val app: Application): ViewModel() {
    val repository = AttendanceRepository()
    val empAttendanceResponse: MutableLiveData<List<AttendanceResponse>?> = MutableLiveData()
    val getDateTimeResponse: MutableLiveData<List<GetDateTimeResponse>?> = MutableLiveData()

    fun empAttendance(
        filePart: MultipartBody.Part,
        cid: RequestBody?,
        bid: RequestBody?,
        empNo: RequestBody?,
        imeiNo: RequestBody?,
        atttype: RequestBody?
    ) {
        viewModelScope.launch {
            try {
                val response = repository.empAttendance(
                    filePart,cid,bid,empNo,imeiNo,atttype
                )
                empAttendanceResponse.value = response

            } catch (e: Exception) {
                Log.d("main", "getPost: ${e.message}")
            }
        }
    }

    fun getDateTime() {
        viewModelScope.launch {
            try {
                val response = repository.getDateTime()
                getDateTimeResponse.value = response

            } catch (e: Exception) {
                Log.d("main", "getPost: ${e.message}")
            }
        }
    }
}