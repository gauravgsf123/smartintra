package com.swayze.smartintra.ui.attendance

import com.swayze.smartintra.network.RetrofitInstance
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AttendanceRepository {
    suspend fun empAttendance(filePart: MultipartBody.Part,
                                  cid: RequestBody?,
                                  bid: RequestBody?,
                                  empNo: RequestBody?,
                                  imeiNo: RequestBody?,
                                  atttype: RequestBody?): List<AttendanceResponse>? = RetrofitInstance.apiService?.empAttendance(filePart,cid,bid,empNo,imeiNo,atttype)

    suspend fun getDateTime(): List<GetDateTimeResponse>? = RetrofitInstance.apiService?.getDateTime()
}