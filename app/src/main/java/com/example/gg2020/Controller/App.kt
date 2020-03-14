package com.example.gg2020.Controller

import android.app.Application
import com.example.gg2020.Utilities.SharedPrefs

class App : Application() {

    companion object {                                                                              //singleton inside specific class
        lateinit var prefs: SharedPrefs
    }

    override fun onCreate() {
        super.onCreate()
        prefs = SharedPrefs(applicationContext)                                                     //initialization of SharedPrefs onCreate to make it available to everyone at the start of the application
    }
}