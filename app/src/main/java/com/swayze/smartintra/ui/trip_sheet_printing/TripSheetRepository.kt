package com.swayze.smartintra.ui.trip_sheet_printing

import com.swayze.smartintra.network.RetrofitInstance

class TripSheetRepository {
    suspend fun getTripSheet(body: Map<String, String>) = RetrofitInstance.apiService?.getTripSheet(body)
}