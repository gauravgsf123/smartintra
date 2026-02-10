package com.swayze.smartintra.ui.vehicle_load_unload.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "vehicleData")
data class VehicleData(@PrimaryKey(autoGenerate = true)
                        var Id: Long = 0,
                        var bar_code: String? = null,
                        var cNote: String? = null,
                        var isMatch: Boolean? = false)
