package com.swayze.smartintra.ui.vehicle_load_unload.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface VehicleDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(lec: VehicleData):Long

    @Update
    suspend fun update(lec: VehicleData)

    @Query("DELETE FROM VehicleData")
    suspend fun deleteAll()

    @Query("SELECT * FROM VehicleData")
    fun getAllVehicleData(): LiveData<List<VehicleData>>

}