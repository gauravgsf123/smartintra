package com.mpcl.app

import android.app.Application

class MyApp:Application() {
    private lateinit var app: MyApp

    companion object {

        lateinit var application: Application

        fun getInstance(): Application {
            return application
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        application = this


    }

}