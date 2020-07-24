package com.zywczas.gg2020.Services

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.zywczas.gg2020.Controller.App
import com.zywczas.gg2020.Model.Channel
import com.zywczas.gg2020.Model.Message
import com.zywczas.gg2020.Utilities.URL_GET_CHANNELS
import com.zywczas.gg2020.Utilities.URL_GET_MESSAGES
import org.json.JSONException

//storing messages and channels

object MessageService {
    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels (complete: (Boolean) -> Unit){                                                  //request from API to GET all created channels, returns an array of JSONObject's
        val channelsRequest = object : JsonArrayRequest (Method.GET, URL_GET_CHANNELS, null, Response.Listener {response ->
            try {
                for (x in 0 until response.length()){
                    val channel = response.getJSONObject(x)                                         //we pull JSON Objects from array 1 by 1
                    val channelName = channel.getString("name")                              //we pull info about each parameter of channel from server
                    val channelId = channel.getString("_id")
                    val newChannel = Channel(channelName, channelId)                   //in our app we create new Channel object with pulled info
                    this.channels.add(newChannel)                                                   //and add it to our list of channels
                }
                complete(true)

            } catch (e: JSONException){
                Log.d("JSON", "EXC: ${e.localizedMessage}")
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not retrieve channels")
            complete(false)
        }){
            override fun getBodyContentType(): String {                                             //2 metody wymagane przez API, pobrane z JSONRequest
                return "application/json; charset=utf-8"                                            //podajemy body content type
            }

            override fun getHeaders(): MutableMap<String, String> {                                 //ta funkcja wysyla header do API w postaci Mapy<Key, Value>
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")                       //nazewnictwo z header'a naszej API
                return headers
            }
        }
        App.prefs.requestQueue.add(channelsRequest)
    }

    fun getMessages (channelId: String, complete: (Boolean) -> Unit) {
        clearMessages()
        val url = "$URL_GET_MESSAGES$channelId"

        val messagesRequest = object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->
            try {
                for (x in 0 until response.length()){
                    val message = response.getJSONObject(x)
                    val messageBody = message.getString("messageBody")
                    val channelId = message.getString("channelId")
                    val id = message.getString("_id")
                    val userName = message.getString("userName")
                    val userAvatar = message.getString("userAvatar")
                    val userAvatarColor = message.getString("userAvatarColor")
                    val timeStamp = message.getString("timeStamp")

                    val newMessage = Message(messageBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
                    this.messages.add(newMessage)
                }
                complete(true)
            } catch (e: JSONException){
                Log.d("JSON", "EXC: ${e.localizedMessage}")
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not retrieve channels")
            complete(false)
        }) {
            override fun getBodyContentType(): String {                                             //2 metody wymagane przez API, pobrane z JSONRequest
                return "application/json; charset=utf-8"                                            //podajemy body content type
            }

            override fun getHeaders(): MutableMap<String, String> {                                 //ta funkcja wysyla header do API w postaci Mapy<Key, Value>
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")                                   //nazewnictwo z header'a naszej API
                return headers
            }
        }
        App.prefs.requestQueue.add(messagesRequest)
    }

    fun clearMessages(){
        messages.clear()
    }

    fun clearChannels() {
        channels.clear()
    }


}