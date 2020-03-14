package com.example.gg2020.Services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.gg2020.Controller.App
import com.example.gg2020.Utilities.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    fun registerUser (email: String, password: String, complete: (Boolean) -> Unit) {     //nasza funkcja tworzaca nowego uzytkownika potrzebuje email i password, ale Volley potrzebuje jeszcze Context, a complete dajemy zeby wiedziec co robic w dwoch przypadkach true i false

        val jsonBody = JSONObject()                                                                 //API na serwerze przyjmuje tylko JSON objects
        jsonBody.put("email", email)                                                         //do JSON body dodajemy key i value
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()                                                //web request musi to podac jako byte array, wiec teraz zamieniamy na string a pozniej na byte array

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener{ response ->   //StringRequest - klasa z pakietu Volley, object = anonymou inner class
            complete(true)
        }, Response.ErrorListener {error ->
            Log.d("ERROR", "cannot register user: $error")
            complete(false)
        }) {                                                                                        //now we specify body content type
            override fun getBodyContentType(): String {                                             //2 metody wymagane przez API, pobrane z JSONRequest
                return "application/json; charset=utf-8"                                            //podajemy body content type
            }

            override fun getBody(): ByteArray {                                                     //podajemy body content
                return requestBody.toByteArray()
            }
        }
        App.prefs.requestQueue.add(registerRequest)                                        //web request stworzony wiec teraz dodajemy go do kolejki
    }


    fun loginUser (email: String, password: String, complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener {response ->
            //this is where we parse json object
            try {
                App.prefs.userEmail = response.getString("user")
                App.prefs.authToken = response.getString("token")                               //ze zwroconego obiektu json pobieramy wartosc dla key "token", wartosc jest typu String
                App.prefs.isLoggedIn = true
                complete(true)
            } catch (e: JSONException){
                Log.d("JSON", "EXC: ${e.localizedMessage}")
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "cannot log in user: $error")
            complete(false)
        }){                                                                                        //now we specify body content type
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        App.prefs.requestQueue.add(loginRequest)
    }

    fun createUser (name: String, email: String, avatarName: String, avatarColor: String, complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("name", name)                                                           //w tym kroku wysylamy dane nowego uzytkownika do API
        jsonBody.put("email", email)                                                         //kolejnosc keys musi byc taka sama jak w API
        jsonBody.put("avatarName", avatarName)
        jsonBody.put("avatarColor", avatarColor)
        val requestBody = jsonBody.toString()

        val createUserRequest = object : JsonObjectRequest (Method.POST, URL_CREATE_USER, null, Response.Listener { response ->

            try {                                                                                   //pobieranie danych ze zwroconego obiektu JSON
                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatarColor = response.getString("avatarColor")
                UserDataService.avatarName = response.getString("avatarName")
                UserDataService.id = response.getString("_id")
                complete(true)

            } catch (e: JSONException){
                Log.d("JSON", "EXC: ${e.localizedMessage}")
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "cannot add user: $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {                                             //3 metody wymagane przez API, pobrane z JSONRequest
                return "application/json; charset=utf-8"                                            //podajemy body content type
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {                                 //ta funkcja wysyla header do API w postaci Mapy<Key, Value>
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")                                   //nazewnictwo z header'a naszej API
                return headers
            }
        }
        App.prefs.requestQueue.add(createUserRequest)
    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit){
        val findUserRequest = object : JsonObjectRequest(Method.GET,
            "$URL_GET_USER${App.prefs.userEmail}",null,
            Response.Listener { response ->
            try {
                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatarColor = response.getString("avatarColor")
                UserDataService.avatarName = response.getString("avatarName")
                UserDataService.id = response.getString("_id")

                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "EXC: ${e.localizedMessage}")
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "cannot find user: $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {                                             //2 metody wymagane przez API, pobrane z JSONRequest
                return "application/json; charset=utf-8"                                            //podajemy body content type
            }

            override fun getHeaders(): MutableMap<String, String> {                                 //ta funkcja wysyla header do API w postaci Mapy<Key, Value>
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")                                   //nazewnictwo z header'a naszej API
                return headers
            }
        }
        App.prefs.requestQueue.add(findUserRequest)
    }




}
