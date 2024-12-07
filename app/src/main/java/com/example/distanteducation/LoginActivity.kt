package com.example.distanteducation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.distanteducation.DB.UserDatabaseHelper
import com.example.distanteducation.functions.LoginHelper
import com.example.distanteducation.serverConection.ApiService
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.LoginRequest
import com.example.distanteducation.serverConection.TokenResponse
import com.example.distanteducation.serverConection.UserResponse
import com.example.distanteducation.serverConection.UserSession
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etLogin: TextInputEditText = findViewById(R.id.et_login)
        val etPassword: TextInputEditText = findViewById(R.id.et_password)
        val btnLogin: Button = findViewById(R.id.btn_login)

        val tlLogin: TextInputLayout = findViewById(R.id.text_input_login)
        val tlPassword: TextInputLayout = findViewById(R.id.text_input_password)

        val ert: TextView = findViewById(R.id.Errors)

        btnLogin.setOnClickListener {
            val login = etLogin.text.toString()
            val password = etPassword.text.toString()
            tlLogin.helperText = ""
            tlPassword.helperText = ""
            ert.text = ""

            if (login.isNotEmpty() && password.isNotEmpty()) {
                LoginHelper.loginGetToken(this, this, login, password)
            } else {
                if (login.isEmpty()) tlLogin.helperText = "Поле не заполнено"
                if (password.isEmpty()) tlPassword.helperText = "Поле не заполнено"
            }
        }

        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
    }

}
