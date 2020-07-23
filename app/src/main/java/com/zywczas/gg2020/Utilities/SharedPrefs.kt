package com.zywczas.gg2020.Utilities

import android.content.Context
import android.content.SharedPreferences
import com.android.volley.toolbox.Volley

class SharedPrefs (context: Context) {
    //todo sprawdzic czy wszystkie komentarze usuniete

    val PREF_FILENAME = "prefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_FILENAME, 0)    //calls values from/to saved data in mobile phone

    private val IS_LOGGED_IN = "isLoggedIn"
    private val AUTH_TOKEN = "authToken"
    private val USER_EMAIL = "userEmail"

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(IS_LOGGED_IN, false)                                      //custom getters and setters
        set(value) = prefs.edit().putBoolean(IS_LOGGED_IN, value).apply()                           //data stored in <K, V> pairs in SharedPreferences

    var authToken: String?
        get() = prefs.getString(AUTH_TOKEN, "")
        set(value) = prefs.edit().putString(AUTH_TOKEN, value).apply()

    var userEmail: String?
        get() = prefs.getString(USER_EMAIL, "")
        set(value) = prefs.edit().putString(USER_EMAIL, value).apply()

    val requestQueue = Volley.newRequestQueue(context)
}