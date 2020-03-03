package com.example.gg2020.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.gg2020.Utilities.URL_LOGIN
import com.example.gg2020.Utilities.URL_REGISTER
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    var isLoggedIn = false
    var userEmail = ""
    var authToken = ""

    fun registerUser (context: Context, email: String, password: String, complete: (Boolean) -> Unit) {     //nasza funkcja tworzaca nowego uzytkownika potrzebuje email i password, ale Volley potrzebuje jeszcze Context, a complete dajemy zeby wiedziec co robic w dwoch przypadkach true i false

        val jsonBody = JSONObject()                                                                 //API na serwerze przyjmuje tylko JSON objects
        jsonBody.put("email", email)                                                         //do JSON body dodajemy key i value
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()                                                //web request musi to podac jako byte array, wiec teraz zamieniamy na string a pozniej na byte array

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener{ response ->   //StringRequest - klasa z pakietu Volley
            complete(true)
        }, Response.ErrorListener {error ->
            Log.d("ERROR", "nie mozna stworzyc uzytkownika: $error")
            complete(false)
        }) {                                                                                        //now we specify body content type
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        Volley.newRequestQueue(context).add(registerRequest)                                        //web request stworzony wiec teraz dodajemy go do kolejki
    }

    fun loginUser (context: Context, email: String, password: String, complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener {response ->
            //this is where we parse json object
            try {
                userEmail = response.getString("user")
                authToken = response.getString("token")                               //ze zwroconego obiektu json chcemy pobrac wartosc dla key "token", wartosc jest typu String
                isLoggedIn = true
                complete(true)
            } catch (e: JSONException){
                Log.d("JSON", "EXC: ${e.localizedMessage}")
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "nie mozna zalogowac uzytkownika: $error")
            complete(false)
        }){                                                                                        //now we specify body content type
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(loginRequest)
    }




}
