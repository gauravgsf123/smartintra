package com.swayze.smartintra.ui.vehicle_load_unload.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ScanDocDataResponseModel(@Expose @SerializedName("Response") var Response:String?,
                                    @Expose @SerializedName("Message") var Message:String?)