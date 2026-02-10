package com.swayze.smartintra.ui.vehicle_load_unload.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicleListData")
data class VehicleListData(@PrimaryKey(autoGenerate = true)
                           var Id: Long = 0,
                           var Response    : String? = null,
                           var Destination : String? = null,
                           var CNoteNo     : String? = null,
                           var BarCodeNo   : String? = null,
                           var ChgWeight   : String? = null,
                           var isScan   : Boolean? = false)
