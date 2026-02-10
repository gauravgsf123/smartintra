package com.swayze.smartintra.ui.pod_upload

import com.google.gson.annotations.SerializedName

data class PODDelayReasonResponse(
    @SerializedName("DELINTERNAL") var DELINTERNAL: List<DelayReasonItem> = emptyList(),
    @SerializedName("UNDEXTERNAL") var UNDEXTERNAL: List<DelayReasonItem> = emptyList(),
    @SerializedName("UNDINTERNAL") var UNDINTERNAL: List<DelayReasonItem> = emptyList(),
    @SerializedName("DELEXTERNAL") var DELEXTERNAL: List<DelayReasonItem> = emptyList()
)

data class DelayReasonItem(
    @SerializedName("remid") var remid: String,
    @SerializedName("remarks") var remarks: String
)
