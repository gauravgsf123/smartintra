package com.swayze.smartintra.ui.login

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

class LoginViewModel(private val app: Application) :ViewModel() {
    private var mContext: Context = app.applicationContext
    private val repository = LoginRepository()
    private var mLoginResponse = MutableLiveData<Resource<LoginResponse>>()
    var loginResponse: LiveData<Resource<LoginResponse>> = mLoginResponse
    fun login(request: Map<String, String>) {
        if (Utils.hasInternetConnection(mContext)) {
            mLoginResponse.postValue(Resource.Loading())
            viewModelScope.launch {
                val response = repository.login(request)
                mLoginResponse.value = response?.let { handleLoginResponse(it) }
            }
        } else mLoginResponse.value =
            Resource.Error(app.resources.getString(R.string.no_internet))
    }

    private fun handleLoginResponse(response: Response<List<LoginResponse>>): Resource<LoginResponse> {
        response.body()?.let {
            return when (response.code()) {
                200 -> {
                    if(it.isNotEmpty()){
                        return Resource.Success("Success",it[0])
                    } else Resource.Error("Login Failed")
                }
                else -> Resource.Error(mContext.resources.getString(R.string.some_thing_went_wrong))
            }
        }
        return Resource.Error(response.message())
    }
}