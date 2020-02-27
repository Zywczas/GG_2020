package com.example.gg2020

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import kotlinx.android.synthetic.main.activity_create_user.*

class CreateUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        showHideBtn.setOnClickListener{
            if (showHideBtn.text.toString().equals("Show")){
                createPasswordText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                showHideBtn.text = "Hide"
            } else{
                createPasswordText.transformationMethod = PasswordTransformationMethod.getInstance()
                showHideBtn.text = "Show"
            }
        }
    }

    fun createUserAvatar (view: View) {

    }

    fun generateColorClicked(view: View) {

    }

    fun createUserClicked (view: View) {

    }
}
