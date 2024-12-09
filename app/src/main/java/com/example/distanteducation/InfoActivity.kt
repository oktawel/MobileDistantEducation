package com.example.distanteducation

import com.example.distanteducation.serverConection.Student

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Group
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

        val token = UserSession.token
        if (token == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val Id = intent.getLongExtra("Id", 0L)
        val type = intent.getStringExtra("type")


        when (type) {
            "lecturer" -> {
                setContentView(R.layout.activity_info_lecturer)
                tTittle = findViewById(R.id.title)
                tName = findViewById(R.id.containerName)
                tSurname = findViewById(R.id.containerSurname)
                tLogin = findViewById(R.id.containerLogin)
                tPassword = findViewById(R.id.containerPassword)
                loadLecturerDetails(Id)
            }
            "student" -> {
                setContentView(R.layout.activity_info_student)
                tTittle = findViewById(R.id.title)
                tName = findViewById(R.id.containerName)
                tSurname = findViewById(R.id.containerSurname)
                tGroup = findViewById(R.id.containerGroup)
                tBDate = findViewById(R.id.containerBDate)
                tLogin = findViewById(R.id.containerLogin)
                tPassword = findViewById(R.id.containerPassword)
                loadStudentDetails(Id)
            }
            "group" -> {
                setContentView(R.layout.activity_info_group)
                tTittle = findViewById(R.id.title)
                tName = findViewById(R.id.containerName)
                loadGroupDetails(Id)
            }
        }
        val editButton = findViewById<Button>(R.id.edit_button)
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

    }

    private fun loadGroupDetails(Id: Long) {
        val apiService = RetrofitClient.apiService
        tTittle.text = "Информация о группе"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getGroupDetails(
                    token = "Bearer ${UserSession.token}",
                    grouptId = Id
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val group = response.body()!!
                        displayGrouprInfo(group)
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

    private fun displayGrouprInfo(group: Group) {
        tName.text = group.name
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
        tLogin.text = lecturer.userLogin
        tPassword.text = lecturer.userPassword
    }
}
