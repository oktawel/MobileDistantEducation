package com.example.distanteducation

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.CourseRequest
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCourse : AppCompatActivity() {

    private lateinit var btnAdd: Button
    private lateinit var inputName: EditText
    private lateinit var inputDescription: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_course)
        inputName = findViewById(R.id.inputName)
        inputDescription = findViewById(R.id.inputDescription)
        btnAdd = findViewById(R.id.btnAdd)

        btnAdd.setOnClickListener {
            addCourse()
        }

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

    }

    private fun addCourse() {
        val name = inputName.text.toString().trim()
        val description = inputDescription.text.toString().trim()

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CourseRequest(
            name = name,
            description = description,
            lecturerId = UserSession.LecturerId!!
        )

        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.addCourse(
                    token = "Bearer ${UserSession.token}",
                    courseRequest = request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@AddCourse,
                            "Курс успешно создан",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Закрыть активность
                    } else {
                        Toast.makeText(
                            this@AddCourse,
                            "Ошибка создания курса",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddCourse,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }



}
