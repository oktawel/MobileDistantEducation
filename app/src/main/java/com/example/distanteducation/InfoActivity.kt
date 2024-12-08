package com.example.distanteducation

import com.example.distanteducation.serverConection.Student

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.transition.Visibility
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Lecturer
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoActivity : AppCompatActivity() {
    private lateinit var tTittle: TextView
    private lateinit var tName: TextView
    private lateinit var tSurname: TextView
    private lateinit var tGroup: TextView
    private lateinit var tBDate: TextView
    private lateinit var tLogin: TextView
    private lateinit var tPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val token = UserSession.token
        if (token == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tTittle = findViewById(R.id.title)
        tName = findViewById(R.id.containerName)
        tSurname = findViewById(R.id.containerSurname)
        tGroup = findViewById(R.id.containerGroup)
        tBDate = findViewById(R.id.containerBDate)
        tLogin = findViewById(R.id.containerLogin)
        tPassword = findViewById(R.id.containerPassword)

        val editButton = findViewById<Button>(R.id.edit_button)

        val Id = intent.getLongExtra("Id", 0L)
        val type = intent.getStringExtra("type")

        val userName: TextView = findViewById(R.id.user_name)

        userName.text = UserSession.user!!.name

        val btnLogout: ImageView = findViewById(R.id.btn_exit_acc)
        btnLogout.setOnClickListener {
            LogoutHelper.performLogout(this)
        }
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }


        if (Id == 0L) {
            Toast.makeText(this, "ID не найден!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        when (type) {
            "lecturer" -> {
                loadLecturerDetails(Id)
            }
            "student" -> {
                loadStudentDetails(Id)
            }
            "group" -> {

            }
        }

    }

    private fun loadLecturerDetails(Id: Long) {
        val apiService = RetrofitClient.apiService
        tTittle.text = "Информация о лекторе"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getLecturerDetails(
                    token = "Bearer ${UserSession.token}",
                    lecurertId = Id
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val lecturer = response.body()!!
                        displayLecturerInfo(lecturer)
                    } else {
                        Toast.makeText(this@InfoActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InfoActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadStudentDetails(studentId: Long) {
        val apiService = RetrofitClient.apiService
        tTittle.text = "Информация о студенте"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getStudentDetails(
                    token = "Bearer ${UserSession.token}",
                    studentId = studentId
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val student = response.body()!!
                        displayStudentInfo(student)
                    } else {
                        Toast.makeText(this@InfoActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InfoActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayStudentInfo(student: Student) {
        tName.text = student.name
        tSurname.text = student.surname
        tGroup.text = student.group
        tBDate.text = student.birthDate
        tLogin.text = student.userLogin
        tPassword.text = student.userPassword
    }
    private fun displayLecturerInfo(lecturer: Lecturer) {

        tName.text = lecturer.name
        tSurname.text = lecturer.surname
        tGroup.visibility = View.INVISIBLE
        tBDate.visibility = View.INVISIBLE
        tLogin.text = lecturer.userLogin
        tPassword.text = lecturer.userPassword
    }
}
