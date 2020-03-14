package com.example.gg2020.Controller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.gg2020.R
import com.example.gg2020.Services.AuthService
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginSpinner.visibility = View.INVISIBLE

        showHideBtn.setOnClickListener{
            if (showHideBtn.text.toString().equals("Show")){
                loginPasswordText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                showHideBtn.text = "Hide"
            } else{
                loginPasswordText.transformationMethod = PasswordTransformationMethod.getInstance()
                showHideBtn.text = "Show"
            }
        }


    }

    fun loginLoginBtnClicked (view: View){
        enableSpinner(true)
        hideKeyboard()
        val email = loginEmailText.text.toString()
        val password = loginPasswordText.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Make sure both email and password are filled in", Toast.LENGTH_LONG).show()
            enableSpinner(false)
            return
        }
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
                Toast.makeText(this,"Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
                enableSpinner(false)
            }
        }

    }

    fun loginCreateUserBtnClicked (view: View){
        val createUserActivityIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserActivityIntent)
        finish()                                                                                    //jak klikniemy guzik Stworz Uzytkownika to LoginActivity zupelnie jest usuwane, wiec po stworzeniu nowego uzytkownika powinno sie cofnac z ekranu 3 (Create user) bezposrednio do ekranu 1 (main activity)
    }

    fun errorToast(){
        Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun enableSpinner (enable: Boolean){                                                            //jezeli klikniemy guzik Create User to inne guziki sa wylaczone zeby,
        if (enable){                                                                                //uzytkownik nie klikal ciagle, oraz wlacza progress bar
            loginSpinner.visibility = View.VISIBLE
        } else {
            loginSpinner.visibility = View.INVISIBLE
        }
        loginLoginBtn.isEnabled = !enable
        loginCreateUserBtn.isEnabled = !enable
    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager  //bierzemy dostep do serwisu o nazwie INPUT..., czyli do klawiatury telefonu i odbieramy to jako klase InputMethodManager
        if (inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)                //podajemy token okna, ktore jest w danym momencie aktywne, czyli otwartej klawiatury
        }
    }
}
