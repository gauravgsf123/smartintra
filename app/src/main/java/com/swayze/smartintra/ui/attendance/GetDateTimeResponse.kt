package com.swayze.smartintra.ui.attendance

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GetDateTimeResponse(@Expose @SerializedName("currTime") var currTime:String)