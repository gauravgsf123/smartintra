package com.swayze.smartintra.network

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.swayze.smartintra.ui.login.LoginViewModel
import com.swayze.smartintra.ui.trip_sheet_printing.TripSheetViewModel

/**
 * Created by Gaurav on 08,Sep,2024
 */
class ViewModalFactory(private val app: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(app) as T
        }else if (modelClass.isAssignableFrom(TripSheetViewModel::class.java)) {
            return TripSheetViewModel(app) as T
        }
        /*else if (modelClass.isAssignableFrom(TrackingViewModel::class.java)) {
            return TrackingViewModel() as T
        }*/



        throw IllegalArgumentException("Unknown class name")
    }
}
