package com.zywczas.gg2020.controller

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.zywczas.gg2020.adapters.MessageAdapter
import com.zywczas.gg2020.models.Channel
import com.zywczas.gg2020.models.Message
import com.zywczas.gg2020.R
import com.zywczas.gg2020.services.AuthService
import com.zywczas.gg2020.services.MessageService
import com.zywczas.gg2020.services.UserDataService
import com.zywczas.gg2020.utilities.BROADCAST_USER_DATA_CHANGE
import com.zywczas.gg2020.utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    private val socket = IO.socket(SOCKET_URL)
    private lateinit var channelAdapter : ArrayAdapter<Channel>
    private lateinit var messageAdapter : MessageAdapter
    private var selectedChannel: Channel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupDrawer()
        setupSocket()
        getUserIfLoggedIn()
        setupUserDataReceiver()
        setupAdapters()
        setupOnClickListeners()
    }

    private fun setupDrawer(){
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupSocket(){
        socket.connect()
        socket.on("channelCreated", onNewChannelListener())
        socket.on("messageCreated", onNewMessageListener())
    }

    private fun onNewChannelListener() : Emitter.Listener {
        return Emitter.Listener { details ->
            if (App.prefs.isLoggedIn) {
                runOnUiThread {
                    receiveChannel(details)
                }
            }
        }
    }

    private fun receiveChannel(details: Array<Any>){
        val channelName = details[0] as String
        val channelId = details[2] as String
        val newChannel = Channel(channelName, channelId)
        MessageService.channels.add(newChannel)
        channelAdapter.notifyDataSetChanged()
    }

    private fun onNewMessageListener() : Emitter.Listener {
        return Emitter.Listener { details ->
            if (App.prefs.isLoggedIn) {
                runOnUiThread {
                    receiveMessage(details)
                }
            }
        }
    }

    private fun receiveMessage(details: Array<Any>){
        val channelId = details[2] as String
        if (channelId == selectedChannel?.id) {
            val msgBody = details[0] as String
            val userName = details[3] as String
            val userAvatar = details[4] as String
            val userAvatarColor = details[5] as String
            val id = details[6] as String
            val timeStamp = details[7] as String
            val newMessage = Message(msgBody, userName, userAvatar, userAvatarColor,
                id, timeStamp)
            MessageService.messages.add(newMessage)
            messageAdapter.notifyDataSetChanged()
            messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    private fun getUserIfLoggedIn(){
        if (App.prefs.isLoggedIn){
            AuthService.findUserByEmail(this){}
        }
    }

    private fun setupUserDataReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
            IntentFilter(BROADCAST_USER_DATA_CHANGE))
    }

    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn){
                setupUserDetails()
                setupChannels()
            }
        }
    }

    private fun setupUserDetails(){
        userNameNavHeader.text = UserDataService.name
        userEmailNavHeader.text = UserDataService.email
        val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
        userImageNavHeader.setImageResource(resourceId)
        userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
        loginBtnNavHeader.text = getString(R.string.logout)
    }

    private fun setupChannels () {
        MessageService.getChannels{complete ->
            if (complete) {
                if (MessageService.channels.count() > 0) {
                    selectedChannel = MessageService.channels[0]
                    channelAdapter.notifyDataSetChanged()
                    updateWithChannel()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateWithChannel() {
        if (selectedChannel != null) {
            mainChannelName.text = "#${selectedChannel!!.name}"
            setupMessages()
        }
    }

    private fun setupMessages(){
        MessageService.getMessages(selectedChannel!!.id){ complete ->
            if (complete){
                messageAdapter.notifyDataSetChanged()
                if (messageAdapter.itemCount > 0){
                    scrollToLastMessage()
                }
            }
        }
    }

    private fun scrollToLastMessage(){
        messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
    }

    private fun setupAdapters (){
        setupChannelAdapter()
        setupMessageAdapter()
    }

    private fun setupChannelAdapter(){
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            MessageService.channels)
        channel_list.adapter = channelAdapter
    }

    private fun setupMessageAdapter(){
        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    private fun setupOnClickListeners(){
        channel_list.setOnItemClickListener { _, _, position, _ ->
            selectedChannel = MessageService.channels[position]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }
    }

    fun loginBtnNavHeaderClicked(view: View){
        if (App.prefs.isLoggedIn){
            logoutUser()
        } else {
            switchToLoginActivity()
        }
    }

    private fun logoutUser() {
        UserDataService.logout()
        channelAdapter.notifyDataSetChanged()
        messageAdapter.notifyDataSetChanged()
        userNameNavHeader.text = ""
        userEmailNavHeader.text = ""
        userImageNavHeader.setImageResource(R.drawable.profiledefault)
        userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
        loginBtnNavHeader.text = getString(R.string.login)
        mainChannelName.text = "Open menu on the left to log in"
    }

    private fun switchToLoginActivity(){
        val loginActivity = Intent(this, LoginActivity::class.java)
        startActivity(loginActivity)
    }

    fun addChannelBtnClicked (view: View){
        if (App.prefs.isLoggedIn){
            addNewChannel()
        }
    }

    private fun addNewChannel(){
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

        builder.setView(dialogView)
            .setPositiveButton("Add"){ _, _ ->
                val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescText)
                val channelName = nameTextField.text.toString()
                val channelDesc = descTextField.text.toString()
                socket.emit("newChannel", channelName, channelDesc)
            }
            .setNegativeButton("Cancel"){ _, _ ->
            }
            .show()
    }

    fun sendMsgBtnClicked (view: View){
        if (App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {
            hideKeyboard()
            sendMessage()
        }
    }

    private fun hideKeyboard(){
        val inputManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    private fun sendMessage(){
        val userId = UserDataService.id
        val channelId = selectedChannel!!.id
        socket.emit("newMessage", messageTextField.text.toString(), userId, channelId,
            UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor)
        messageTextField.text.clear()
    }

    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        super.onDestroy()
    }
}
