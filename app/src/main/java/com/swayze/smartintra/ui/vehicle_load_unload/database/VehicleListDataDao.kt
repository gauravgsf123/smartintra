package com.swayze.smartintra.ui.vehicle_load_unload.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface VehicleListDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(lec: VehicleListData):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(list: List<VehicleListData>): List<Long>

    @Update
    suspend fun update(lec: VehicleListData)

    @Query("DELETE FROM VehicleListData")
    suspend fun deleteAll()

    @Query("SELECT * FROM VehicleListData")
    fun getAllData(): LiveData<List<VehicleListData>>

}