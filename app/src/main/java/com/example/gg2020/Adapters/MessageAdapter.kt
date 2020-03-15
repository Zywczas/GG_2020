package com.example.gg2020.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gg2020.Model.Message
import com.example.gg2020.R
import com.example.gg2020.Services.UserDataService
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageAdapter (val context: Context, val messages: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage = itemView.findViewById<ImageView>(R.id.messageUserImage)
        val timeStamp = itemView.findViewById<TextView>(R.id.timeStampLbl)
        val userName = itemView.findViewById<TextView>(R.id.messageUserNameLbl)
        val messageBody = itemView.findViewById<TextView>(R.id.messageBodyLbl)

        fun bindMessage (context: Context, message: Message) {
            val resourceId = context.resources.getIdentifier(message.userAvatar, "drawable", context.packageName)
            userImage.setImageResource(resourceId)
            userImage.setBackgroundColor(UserDataService.returnAvatarColor(message.userAvatarColor))
            userName.text = message.userName
            timeStamp.text = returnDateString(message.timeStamp)
            messageBody.text = message.message
        }
    }

    fun returnDateString(isoString: String) : String {
        // 2017-09-11T01:16:18.858Z - this is what API gives
        // Monday 4:35 PM - this is what we want

        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())   //format to parse String into date
        isoFormatter.timeZone = TimeZone.getTimeZone("UTC")                                            //UTC - Coordinated Universal Time (Standard)
        var convertedDate = Date()
        try {
            convertedDate = isoFormatter.parse(isoString)
        } catch (e: ParseException) {
            Log.d("PARSE", "Cannot parse String into date")
        }

        val outDateString = SimpleDateFormat("E, h:mm a", Locale.getDefault())
        return outDateString.format(convertedDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return messages.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindMessage(context, messages[position])
    }
}