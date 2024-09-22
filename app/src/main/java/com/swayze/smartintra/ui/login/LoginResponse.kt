package com.swayze.smartintra.ui.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(@SerializedName("EMPNO"   ) var EMPNO   : String? = null,
                         @SerializedName("BID"     ) var BID     : String? = null,
                         @SerializedName("BCITY"   ) var BCITY   : String? = null,
                         @SerializedName("EMPNAME" ) var EMPNAME : String? = null
)
