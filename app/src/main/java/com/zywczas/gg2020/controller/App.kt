package com.zywczas.gg2020.controller

import android.app.Application
import com.zywczas.gg2020.utilities.SharedPrefs

class App : Application() {

    companion object {
        lateinit var prefs: SharedPrefs
    }

    override fun onCreate() {
        super.onCreate()
        prefs = SharedPrefs(applicationContext)
    }
}