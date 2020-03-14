package com.example.gg2020.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gg2020.Model.Channel
import com.example.gg2020.R
import com.example.gg2020.Services.AuthService
import com.example.gg2020.Services.MessageService
import com.example.gg2020.Services.UserDataService
import com.example.gg2020.Utilities.BROADCAST_USER_DATA_CHANGE
import com.example.gg2020.Utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)

    lateinit var channelAdapter : ArrayAdapter<Channel>
    private fun setupAdapter (){
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            MessageService.channels)
        channel_list.adapter = channelAdapter
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_tools,
                R.id.nav_share,
                R.id.nav_send
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        socket.connect()
        setupAdapter()

        if (App.prefs.isLoggedIn){
            AuthService.findUserByEmail(this){}
        } else {
            mainWelcomeText.text = "Open menu on the left to log in"
        }
    }

    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn){
                userNameNavHeader.text = UserDataService.name
                userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                loginBtnNavHeader.text = "Logout"
                mainWelcomeText.text = ""

                MessageService.getChannels(context){complete ->
                    if (complete) {
                        channelAdapter.notifyDataSetChanged()                                       //onCrate we have empty array of channels, but this fun tells our adapter about new data in onResume method
                    } else {

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(  //odbieramy dane z CreateUserActivity
            BROADCAST_USER_DATA_CHANGE))
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
    }

    fun loginBtnNavHeaderClicked(view: View){
        if (App.prefs.isLoggedIn){
            // log out
            UserDataService.logout()
            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginBtnNavHeader.text = "Login"
            mainWelcomeText.text = "Open menu on the left to log in"
        } else {
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }
    }

    fun addChannelBtnClicked (view: View){
        if (App.prefs.isLoggedIn){
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                .setPositiveButton("Add"){ dialog, which ->  
                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameText) //nie mozna odwolac sie bezposrednio do pol tekstowych w DialogView wiec trzeba je wyszukac po ID
                    val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescText)
                    val channelName = nameTextField.text.toString()
                    val channelDesc = descTextField.text.toString()

                    //create channel with name and description
                    socket.emit("newChannel", channelName, channelDesc)                       //kolejnosc wysylania parametrow jak w API - Event i dane
                    socket.on("channelCreated", onNewChannel)                                //odbiera dane z API, jezeli Event wykryty to Emmitter Listener odbiera informacje
                }
                .setNegativeButton("Cancel"){ dialog, which ->
                }
                .show()
        }
    }

    private val onNewChannel = Emitter.Listener {args ->                                            //przyjmuje z API array elementow typu ANY wiec musimy cast as String
        runOnUiThread {                                                                             //Emmiter Listener dziala na worker Thread zeby nie blokowac calego UI naszej aplikacji wiec tutaj kazemy mu przejsc na glowny thread japo tym jak juz pobierze wyniki (args), zeby nasze UI zostalo zauktualizowane
            val channelName = args[0] as String
            val channelDesctription = args[1] as String
            val channelId = args[2] as String

            val newChannel = Channel(channelName, channelDesctription, channelId)                   //we create new Channel object were we store data about it
            MessageService.channels.add(newChannel)                                                 //adding new channel to list of all channels
            channelAdapter.notifyDataSetChanged()                                                   //after creation of new channel and receiving it from API it also refreshes list automatically now

        }
    }

    fun sendMsgBtnClicked (view: View){
        hideKeyboard()
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager  //bierzemy dostep do serwisu o nazwie INPUT..., czyli do klawiatury telefonu i odbieramy to jako klase InputMethodManager
        if (inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)                //podajemy token okna, ktore jest w danym momencie aktywne, czyli otwartej klawiatury
        }
    }
}
