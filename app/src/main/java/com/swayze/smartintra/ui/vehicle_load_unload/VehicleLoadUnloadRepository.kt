package com.swayze.smartintra.ui.vehicle_load_unload

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.swayze.smartintra.app.AppDatabase
import com.swayze.smartintra.network.RetrofitInstance
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleData
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleListData
import com.swayze.smartintra.ui.vehicle_load_unload.model.VehicleLoadRequest
import kotlinx.coroutines.launch
import kotlin.text.insert

class VehicleLoadUnloadRepository(mContext: Context) {

    private var db: AppDatabase = AppDatabase.getInstance(mContext.applicationContext)
    private var vehicleDataDao = db.vehicleDataDao()
    private var vehicleListDataDao = db.vehicleListDataDao()
    //private var vehicleAllListData: LiveData<List<VehicleListData>> = vehicleListDataDao.getAllData()

    suspend fun getDocTypeList(body:Map<String,String>) = RetrofitInstance.apiService?.getDocTypeList(body)
    suspend fun scanDocTotal(body:Map<String,String>) = RetrofitInstance.apiService?.scanDocTotal(body)

    suspend fun getVehicleDataList(body:Map<String,String>)= RetrofitInstance.apiService?.getVehicleDataList(body)

    suspend fun uploadNewVehicleScan(vecicleloadRequst: VehicleLoadRequest) = RetrofitInstance.apiService?.uploadNewVehicleScan(vecicleloadRequst)

    suspend fun sendExtraScan(body:Map<String,String>) = RetrofitInstance.apiService?.sendExtraScan(body)

    suspend fun insertVehicle(vehicle: VehicleData) {
            vehicleDataDao.insert(vehicle)
    }
    suspend fun updateVehicle(vehicle: VehicleData) {
            vehicleDataDao.update(vehicle)
    }
    suspend fun insertVehicleList(vehicleList: VehicleListData): Long {
            return vehicleListDataDao.insert(vehicleList)
    }

    suspend fun insertVehicleListBulk(list: List<VehicleListData>): List<Long> {
        return vehicleListDataDao.insertAll(list)
    }
    suspend fun updateVehicleList(vehicleList: VehicleListData) {
            vehicleListDataDao.update(vehicleList)
    }

    fun getAllVehicleListData():LiveData<List<VehicleListData>> {
            return vehicleListDataDao.getAllData()
    }

    fun getAllVehicleData():LiveData<List<VehicleData>> {
            return vehicleDataDao.getAllVehicleData()
    }

    suspend fun deleteAllVehicleListData() {
        vehicleListDataDao.deleteAll()
    }

    suspend fun deleteAllVehicleData() {
        vehicleDataDao.deleteAll()
    }
}