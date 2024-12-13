package com.example.distanteducation

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.transition.Visibility
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Course
import com.example.distanteducation.serverConection.Group
import com.example.distanteducation.serverConection.Lecturer
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.Student
import com.example.distanteducation.serverConection.Test
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoTestActivity : AppCompatActivity() {
    private lateinit var btnComplete: Button
    private lateinit var tTittle: TextView
    private lateinit var tDescription: TextView
    private lateinit var tMark: TextView

    private lateinit var token: String
    private lateinit var infoTest: Test


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_test)

        btnComplete = findViewById(R.id.btnComplete)

        token = UserSession.token ?: return

        val userName: TextView = findViewById(R.id.user_name)

        userName.text = UserSession.user!!.name +" "+ UserSession.user!!.surname

        val btnLogout: ImageView = findViewById(R.id.btn_exit_acc)
        btnLogout.setOnClickListener {
            LogoutHelper.performLogout(this)
        }
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        val test = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("Test", Test::class.java)
        } else {
            intent.getParcelableExtra("Test")
        }
        infoTest = test!!

        tTittle = findViewById(R.id.title)
        tDescription = findViewById(R.id.containerDescription)
        tMark = findViewById(R.id.containerMark)

        tTittle.text = infoTest.name
        tDescription.text = infoTest.description

        val resultMark = "%.2f".format(test.mark)
        if (test.mark == 0.toFloat()){
            tMark.text = "Оценка: Ещё не пройден"
            tMark.setTextColor(resources.getColor(R.color.red, theme))
        }
        else{
            tMark.text = "Оценка: ${resultMark}"
            tMark.setTextColor(resources.getColor(R.color.green, theme))
        }

        if (UserSession.user!!.role == "Lector" || test.mark != 0.toFloat() || !(test.open)){
            if (UserSession.user!!.role == "Lector"){
                tMark.visibility = View.GONE
            }
            btnComplete.visibility = View.GONE
        }

        btnComplete.setOnClickListener {
            startAdd()
        }



    }

    private fun startAdd(){
        val intent = Intent(this, AddCourse::class.java)
        startActivity(intent)
    }






}

