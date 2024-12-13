package com.example.distanteducation

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Course
import com.example.distanteducation.serverConection.CourseRequestUpdate
import com.example.distanteducation.serverConection.GroupRequestUpdate
import com.example.distanteducation.serverConection.LecturerRequestUpdate
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.StudentRequestUpdate
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditCourseActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editDescription: EditText
    private lateinit var btnEdit: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_course)

        editName = findViewById(R.id.editName)
        editDescription = findViewById(R.id.editDescription)
        btnEdit = findViewById(R.id.btnEdit)

        val course = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("Course", Course::class.java)
        } else {
            intent.getParcelableExtra("Course")
        }

        loadCourseData(course!!)

        btnEdit.setOnClickListener {
            updateCourse(course)
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


    private fun loadCourseData(course: Course) {
        editName.setText(course.name)
        editDescription.setText(course.description)
    }


    private fun  updateCourse(course: Course) {
        val name = editName.text.toString().trim()
        val description = editDescription.text.toString().trim()

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CourseRequestUpdate(
            id = course.id,
            name = name,
            description = description,
            lecturerId = UserSession.LecturerId!!
        )

        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.updateCourse(
                    token = "Bearer ${UserSession.token}",
                    courseRequest = request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditCourseActivity, "Курс успешно обновлен", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditCourseActivity, "Ошибка обновления курса", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditCourseActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
