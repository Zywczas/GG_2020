package com.zywczas.gg2020.controller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.zywczas.gg2020.R
import com.zywczas.gg2020.services.AuthService
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupOnClickListeners()
    }

    private fun setupOnClickListeners(){
        showHideBtn.setOnClickListener{
            if (showHideBtn.text.toString() == "Show"){
                showPasswordInput()
            } else {
                hidePasswordInput()
            }
        }
    }

    private fun showPasswordInput(){
        loginPasswordText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        showHideBtn.text = getString(R.string.hide)
    }

    private fun hidePasswordInput(){
        loginPasswordText.transformationMethod = PasswordTransformationMethod.getInstance()
        showHideBtn.text = getString(R.string.show)
    }

    fun loginLoginBtnClicked (view: View){
        enableSpinner(true)
        hideKeyboard()
        val email = loginEmailText.text.toString()
        val password = loginPasswordText.text.toString()
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Make sure both email and password are filled in", Toast.LENGTH_LONG).show()
            enableSpinner(false)
        } else {
            loginNewUser(email, password)
        }
    }

    private fun loginNewUser(email: String, password: String){
        AuthService.loginUser(email, password){loginSuccess ->
            if (loginSuccess){
                AuthService.findUserByEmail(this){findSuccess ->
                    if (findSuccess){
                        enableSpinner(false)
                        finish()
                    } else {
                        errorToast()
                    }
                }
            } else {
                errorToast()
                enableSpinner(false)
            }
        }
    }

    private fun errorToast(){
        Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun loginCreateUserBtnClicked (view: View){
        switchToCreateUserActivity()
    }

    private fun switchToCreateUserActivity(){
        val createUserActivityIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserActivityIntent)
        finish()
    }

    private fun enableSpinner (enabled: Boolean){                                                            //jezeli klikniemy guzik Create User to inne guziki sa wylaczone zeby,
        if (enabled){                                                                                //uzytkownik nie klikal ciagle, oraz wlacza progress bar
            loginSpinner.visibility = View.VISIBLE
        } else {
            loginSpinner.visibility = View.INVISIBLE
        }
        loginLoginBtn.isEnabled = !enabled
        loginCreateUserBtn.isEnabled = !enabled
    }

    private fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager  //bierzemy dostep do serwisu o nazwie INPUT..., czyli do klawiatury telefonu i odbieramy to jako klase InputMethodManager
        if (inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)                //podajemy token okna, ktore jest w danym momencie aktywne, czyli otwartej klawiatury
        }
    }
}
