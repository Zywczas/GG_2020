package com.zywczas.gg2020.controller

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.zywczas.gg2020.R
import com.zywczas.gg2020.services.AuthService
import com.zywczas.gg2020.utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    private var userAvatarName = "profileDefault"
    //RGB color values from 0 to 1 adopted from API adjusted to iOS
    private var avatarBackgroundColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        setupOnClickListeners()
    }

    private fun setupOnClickListeners() {
        showHideBtn.setOnClickListener{
            if (showHideBtn.text.toString() == "Show"){
                showPasswordInput()
            } else{
                hidePasswordInput()
            }
        }
    }

    private fun showPasswordInput(){
        createPasswordText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        showHideBtn.text = getString(R.string.hide)
    }

    private fun hidePasswordInput(){
        createPasswordText.transformationMethod = PasswordTransformationMethod.getInstance()
        showHideBtn.text = getString(R.string.show)
    }

    fun createAvatarImageClicked (view: View) {
        val random = Random()
        val color = random.nextInt(2)                                                   //creates random number between 0-2 but 2 is excluded, so it can be or 0 or 1
        val avatar = random.nextInt(28)                                                 //because we have 27 images to choose from
        userAvatarName =
            if (color == 0){
                "light$avatar"
            } else {
                "dark$avatar"
            }
        val resourceId = resources.getIdentifier(userAvatarName, "drawable", packageName)
        createAvatarImageView.setImageResource(resourceId)
    }

    fun generateColorClicked(view: View) {
        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)
        createAvatarImageView.setBackgroundColor(Color.rgb(r,g,b))
        val savedR = r.toDouble() / 255                                                     //konwertujemy wartosc 0-255 na wartosc 0-1, w takim formacie color bedzie
        val savedG = g.toDouble() /255                                                      //przekazany pozniej do zdjecia profilowego
        val savedB = b.toDouble() / 255
        avatarBackgroundColor = "[$savedR, $savedG, $savedB, 1]"
    }

    fun createUserClicked (view: View) {
        enableSpinner(true)                                                                  //funkcja zdefiniowana ponizej
        val userName = createUserNameText.text.toString()
        val userEmail = createEmailText.text.toString()                                      //musi byc toString bo inaczej daloby characters (CharSequence)
        val userPassword = createPasswordText.text.toString()
        if (userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this,"Make sure username, email and password are filled in.",Toast.LENGTH_LONG).show()
            enableSpinner(false)
        } else {
            createNewUser(userName, userEmail, userPassword)
        }
    }

    private fun enableSpinner (enable: Boolean){                                                            //jezeli klikniemy guzik Create User to inne guziki sa wylaczone zeby,
        if (enable){                                                                                //uzytkownik nie klikal ciagle, oraz wlacza prgress bar
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }
        createUserBtn.isEnabled = !enable
        createAvatarImageView.isEnabled = !enable
        backgroundColorBtn.isEnabled = !enable
    }

    private fun showError(){
        Toast.makeText(this, "Something went wrong. Please change email and try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    private fun createNewUser(name: String, email: String, password: String){
        AuthService.registerUser(email, password){registerSuccess ->          //definicja funkcji bierze lambde stad i daje przypisuje jej paramert Boolean a pozniej ja wykonuje
            if (registerSuccess){
                AuthService.loginUser(email, password){loginSuccess ->
                    if (loginSuccess){
                        AuthService.createUser(name, email, userAvatarName, avatarBackgroundColor){ createUserSuccess ->
                            if (createUserSuccess){
                                enableSpinner(false)
                                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange) //wysylamy tylko info, nie wartosci
                                finish()                                                            //wylacza ta activity i wraca do poprzedniej
                            } else {
                                showError()
                            }
                        }
                    } else {
                        showError()
                    }
                }
            } else {
                showError()
            }
        }
    }

}
