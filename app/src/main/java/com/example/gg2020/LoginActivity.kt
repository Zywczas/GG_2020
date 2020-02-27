package com.example.gg2020

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import com.example.gg2020.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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

    }

    fun loginCreateUserBtnClicked (view: View){
        val createUserActivityIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserActivityIntent)
    }
}
