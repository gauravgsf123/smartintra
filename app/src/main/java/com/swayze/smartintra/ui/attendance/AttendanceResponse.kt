package com.swayze.smartintra.ui.attendance

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AttendanceResponse(@Expose @SerializedName("Response") var Response:String?,
                              @Expose @SerializedName("marktime") var marktime:String?,
                              @Expose @SerializedName("marktype") var marktype:String?)
