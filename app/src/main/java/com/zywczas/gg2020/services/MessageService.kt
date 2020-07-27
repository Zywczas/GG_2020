package com.zywczas.gg2020.services

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.zywczas.gg2020.controller.App
import com.zywczas.gg2020.models.Channel
import com.zywczas.gg2020.models.Message
import com.zywczas.gg2020.utilities.URL_GET_CHANNELS
import com.zywczas.gg2020.utilities.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {
    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels (complete: (Boolean) -> Unit){
        val channelsRequest = object : JsonArrayRequest (Method.GET, URL_GET_CHANNELS, null, Response.Listener {response ->
            try {
                for (x in 0 until response.length()){
                    val channel = response.getJSONObject(x)
                    val channelName = channel.getString("name")
                    val channelId = channel.getString("_id")
                    val newChannel = Channel(channelName, channelId)
                    this.channels.add(newChannel)
                }
                complete(true)

            } catch (e: JSONException){
                Log.d("JSON", "EXC: ${e.localizedMessage}")
            }

        }, Response.ErrorListener {
            Log.d("ERROR", "Could not retrieve channels")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
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
                    val id = message.getString("_id")
                    val userName = message.getString("userName")
                    val userAvatar = message.getString("userAvatar")
                    val userAvatarColor = message.getString("userAvatarColor")
                    val timeStamp = message.getString("timeStamp")

                    val newMessage = Message(messageBody, userName, userAvatar, userAvatarColor, id, timeStamp)
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
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
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