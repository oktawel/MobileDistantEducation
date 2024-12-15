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
import com.example.distanteducation.serverConection.Group
import com.example.distanteducation.serverConection.GroupRequestUpdate
import com.example.distanteducation.serverConection.LecturerRequestUpdate
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.StudentRequestUpdate
import com.example.distanteducation.serverConection.Test
import com.example.distanteducation.serverConection.TestRequestUpdate
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditTest : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editDescription: EditText
    private lateinit var btnEdit: Button
    private lateinit var toggleBoolean: ToggleButton
    private lateinit var course: Spinner

    private var courseList: List<Course> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_test)

        editName = findViewById(R.id.editName)
        editDescription = findViewById(R.id.editDescription)
        btnEdit = findViewById(R.id.btnEdit)
        toggleBoolean = findViewById(R.id.toggleBoolean)
        course = findViewById(R.id.course)

        val test = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("Test", Test::class.java)
        } else {
            intent.getParcelableExtra("Test")
        }

        loadTestData(test!!)

        btnEdit.setOnClickListener {
            updateCourse(test)
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


    private fun loadTestData(test: Test) {
        editName.setText(test.name)
        editDescription.setText(test.description)
        toggleBoolean.isChecked = test.open
        loadCourses(test.subjectId)
    }

    private fun loadCourses(selectedCourseId: Long? = null) {
        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getAllCourses("Bearer ${UserSession.token}")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val courses = response.body()!!

                        courseList = courses

                        val courseNames = courseList.map { it.name }

                        val adapter = ArrayAdapter(
                            this@EditTest,
                            android.R.layout.simple_spinner_item,
                            courseNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        course.adapter = adapter

                        selectedCourseId?.let { id ->
                            val courseIndex = courseList.indexOfFirst { it.id == id }
                            if (courseIndex != -1) {
                                course.setSelection(courseIndex)
                            }
                        }
                    } else {
                        Toast.makeText(this@EditTest, "Ошибка загрузки курсов", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditTest, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun  updateCourse(test: Test) {
        val name = editName.text.toString().trim()
        val description = editDescription.text.toString().trim()
        val open = toggleBoolean.isChecked
        val selectedCourseName = course.selectedItem as String
        val subjectId = courseList.find { it.name == selectedCourseName }?.id

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = TestRequestUpdate(
            id = test.id,
            name = name,
            description = description,
            open = open,
            subjectId = subjectId!!
        )

        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.updateTest(
                    token = "Bearer ${UserSession.token}",
                    testRequestUpdate = request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditTest, "Тест успешно обновлен", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditTest, "Ошибка обновления теста", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditTest, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
