package com.swayze.smartintra.network

import android.content.Context
import com.google.gson.GsonBuilder
import com.swayze.smartintra.app.Constant
import com.swayze.smartintra.app.MyApp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private lateinit var retrofit: Retrofit

    private fun getRetrofitInstance(): Retrofit {
        val context: Context = MyApp.getInstance().applicationContext
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val authInterceptor = AuthInterceptor(context)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        if (!(::retrofit.isInitialized)) {
            retrofit = Retrofit.Builder()
                .baseUrl(Constant.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit
    }

    val apiService: Apis? = getRetrofitInstance().create(Apis::class.java)

}