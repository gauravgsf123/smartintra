package com.swayze.smartintra.ui.vehicle_load_unload

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleData
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleListData
import com.swayze.smartintra.ui.vehicle_load_unload.model.DocTypeListResponseModel
import com.swayze.smartintra.ui.vehicle_load_unload.model.ScanDocDataResponseModel
import com.swayze.smartintra.ui.vehicle_load_unload.model.ScanDocTotalResponseModel
import com.swayze.smartintra.ui.vehicle_load_unload.model.SendExtraScanResponseModel
import com.swayze.smartintra.ui.vehicle_load_unload.model.VehicleLoadRequest
import com.swayze.smartintra.ui.vehicle_load_unload.model.VehicleResponseModel
import kotlinx.coroutines.launch
import java.lang.Exception

class VehicleLoadUnloadModel(private val app: Application) :ViewModel() {
    @SuppressLint("StaticFieldLeak")
    private var mContext: Context = app.applicationContext
    private val repository = VehicleLoadUnloadRepository(mContext)


    var docTypeListResponse : MutableLiveData<List<DocTypeListResponseModel>?> = MutableLiveData()
    var scanDocTotalResponse : MutableLiveData<List<ScanDocTotalResponseModel>?> = MutableLiveData()
    var uploadVehicleScanResponse : MutableLiveData<List<ScanDocDataResponseModel>?> = MutableLiveData()
    var vehicleResponseModel : MutableLiveData<List<VehicleListData>?> = MutableLiveData()
    var sendExtraScanResponse : MutableLiveData<List<SendExtraScanResponseModel>?> = MutableLiveData()

    //val vehicleAllListData: MutableLiveData<List<VehicleListData>> = MutableLiveData()
    val vehicleAllListData: LiveData<List<VehicleListData>> = repository.getAllVehicleListData()
    val vehicleAllData: LiveData<List<VehicleData>> = repository.getAllVehicleData()

    fun getDocTypeList(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = repository.getDocTypeList(body)
                docTypeListResponse.value = response
            }catch (e: Exception){}
        }
    }

    fun scanDocTotal(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = repository.scanDocTotal(body)
                scanDocTotalResponse.value = response
            }catch (e: Exception){}
        }
    }

    fun sendExtraScan(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = repository.sendExtraScan(body)
                sendExtraScanResponse.value = response
            }catch (e: Exception){}
        }
    }

    fun getVehicleDataList(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = repository.getVehicleDataList(body)
                vehicleResponseModel.value = response
            }catch (e: Exception){}
        }
    }

    fun uploadNewVehicleScan(vecicleloadRequst: VehicleLoadRequest) {
        viewModelScope.launch(){
            try {
                val response = repository.uploadNewVehicleScan(vecicleloadRequst)
                uploadVehicleScanResponse.value = response
            }catch (e: Exception){}
        }
    }

    fun insertVehicle(vehicle: VehicleData) {
        viewModelScope.launch {
            repository.insertVehicle(vehicle)
        }
    }
    fun updateVehicle(vehicle: VehicleData) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
        }
    }
    fun insertVehicleList(vehicleList: VehicleListData) {
        viewModelScope.launch {
            val id = repository.insertVehicleList(vehicleList)
        }
    }

    fun insertVehicleListBulk(list: List<VehicleListData>) {
        viewModelScope.launch {
            val resultIds = repository.insertVehicleListBulk(list)
            Log.d("BulkInsert", "Inserted rows = ${resultIds.size}")
        }
    }
    fun updateVehicleList(vehicleList: VehicleListData) {
        viewModelScope.launch {
            repository.updateVehicleList(vehicleList)
        }
    }

    fun deleteAllVehicleListData() {
        viewModelScope.launch {
            repository.deleteAllVehicleListData()
        }
    }

    fun deleteAllVehicleData() {
        viewModelScope.launch {
            repository.deleteAllVehicleData()
        }
    }

    /*fun getAllVehicleListData() {
        viewModelScope.launch {
            vehicleAllListData.value = repository.getAllVehicleListData().value
        }
    }*/
    /*fun delete(vehicleData: VehicleData) {
        viewModelScope.launch {
            vehicleDataDao.delete(vehicleData)
        }
    }*/



}