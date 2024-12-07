package com.example.distanteducation

import com.example.distanteducation.serverConection.Student

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val container = findViewById<LinearLayout>(R.id.student_info_container)
        val editButton = findViewById<Button>(R.id.edit_button)

        val Id = intent.getLongExtra("Id", 0L)

        if (Id == 0L) {
            Toast.makeText(this, "ID не найден!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val type = intent.getStringExtra("type")
        when (type) {
            "lecturer" -> {

            }
            "student" -> {
                loadStudentDetails(Id.toLong(), container)
            }
            "group" -> {

            }
        }

    }

    private fun loadStudentDetails(studentId: Long, container: LinearLayout) {
        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getStudentDetails(
                    token = "Bearer ${UserSession.token}",
                    studentId = studentId
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val student = response.body()!!
                        displayStudentInfo(student, container)
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

    private fun displayStudentInfo(student: Student, container: LinearLayout) {
        container.removeAllViews()

        container.addView(createTextView("Имя студента: ${student.name}"))
        container.addView(createTextView("Фамилия студента: ${student.surname}"))
        container.addView(createTextView("Группа: ${student.group}"))
        container.addView(createTextView("Дата рождения: ${student.birthDate}"))
        container.addView(createTextView("Логин: ${student.userLogin}"))
        container.addView(createTextView("Пароль: ${student.userPassword}"))
    }

    private fun createTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@InfoActivity, R.color.black))
            setPadding(10, 10, 10, 10)
        }
    }
}
