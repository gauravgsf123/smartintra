package com.swayze.smartintra.ui.trip_sheet_printing

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swayze.smartintra.R
import com.swayze.smartintra.network.Resource
import com.swayze.smartintra.util.Utils
import kotlinx.coroutines.launch
import retrofit2.Response

class TripSheetViewModel(app: Application) :ViewModel() {
    private var mContext: Context = app.applicationContext
    private val repository = TripSheetRepository()
    private var mTripSheetResponse = MutableLiveData<Resource<List<TripSheetResponse>>>()
    var tripSheetResponse: LiveData<Resource<List<TripSheetResponse>>> = mTripSheetResponse
    fun getTripSheet(request: Map<String, String>) {
        if (Utils.hasInternetConnection(mContext)) {
            mTripSheetResponse.postValue(Resource.Loading())
            viewModelScope.launch {
                val response = repository.getTripSheet(request)
                mTripSheetResponse.value = response?.let { handleTripSheetResponse(it) }
            }
        } else mTripSheetResponse.value =
            Resource.Error(mContext.resources.getString(R.string.no_internet))
    }

    private fun handleTripSheetResponse(response: Response<List<TripSheetResponse>>): Resource<List<TripSheetResponse>> {
        response.body()?.let {
            return when (response.code()) {
                200 -> {
                    if(it.isNotEmpty()){
                        return Resource.Success("Success",it)
                    } else Resource.Error("No Data")
                }
                else -> Resource.Error(mContext.resources.getString(R.string.some_thing_went_wrong))
            }
        }
        return Resource.Error(response.message())
    }
}