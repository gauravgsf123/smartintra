package com.swayze.smartintra.ui.login

import com.swayze.smartintra.network.RetrofitInstance

class LoginRepository {
    suspend fun login(body: Map<String, String>) = RetrofitInstance.apiService?.login(body)
}