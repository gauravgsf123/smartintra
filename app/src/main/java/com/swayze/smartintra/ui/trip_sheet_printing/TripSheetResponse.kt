package com.swayze.smartintra.ui.trip_sheet_printing

import com.google.gson.annotations.SerializedName

data class TripSheetResponse(@SerializedName("Response"    ) var Response    : String? = null,
                             @SerializedName("Consignee"   ) var Consignee   : String? = null,
                             @SerializedName("Destination" ) var Destination : String? = null,
                             @SerializedName("Company"     ) var Company     : String? = null,
                             @SerializedName("FromCode"    ) var FromCode    : String? = null,
                             @SerializedName("TotalBox"    ) var TotalBox    : String? = null,
                             @SerializedName("CNoteNo"     ) var CNoteNo     : String? = null,
                             @SerializedName("ToCode"      ) var ToCode      : String? = null,
                             @SerializedName("BarCodeNo"   ) var BarCodeNo   : String? = null,
                             var printDone   : Boolean? = false
)