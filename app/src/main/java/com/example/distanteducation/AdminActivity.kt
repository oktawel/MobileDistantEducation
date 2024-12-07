package com.example.distanteducation

import android.R.id.message
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.UserSession


class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        val userName: TextView = findViewById(R.id.user_name)

        userName.text = UserSession.user?.name ?: "NotLoaded"


        val btnLogout: ImageView = findViewById(R.id.btn_exit_acc)
        btnLogout.setOnClickListener {
            LogoutHelper.performLogout(this)
        }

        val btnLecturers: Button = findViewById(R.id.btn_lecturers)
        btnLecturers.setOnClickListener{
            val intent = Intent(this, ListsActivity::class.java)
            intent.putExtra("type", "lecturer")
            startActivity(intent)
        }
        val btnStudents: Button = findViewById(R.id.btn_students)
        btnStudents.setOnClickListener{
            val intent = Intent(this, ListsActivity::class.java)
            intent.putExtra("type", "student")
            startActivity(intent)
        }
        val btnGroups: Button = findViewById(R.id.btn_groups)
        btnGroups.setOnClickListener{
            val intent = Intent(this, ListsActivity::class.java)
            intent.putExtra("type", "group")
            startActivity(intent)
        }
    }
}