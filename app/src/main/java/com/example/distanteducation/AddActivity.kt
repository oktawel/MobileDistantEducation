package com.example.distanteducation

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Group
import com.example.distanteducation.serverConection.GroupRequest
import com.example.distanteducation.serverConection.LecturerRequest
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.StudentRequest
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddActivity : AppCompatActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputSurname: EditText
    private lateinit var inputBirthDate: EditText
    private lateinit var spinnerGroup: Spinner
    private lateinit var btnAdd: Button
    private lateinit var type: String

    private var groupsMap: Map<String, Long> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        type = intent.getStringExtra("type") ?: return

        when (type) {
            "lecturer" -> {
                setContentView(R.layout.activity_add_lecturer)
                inputName = findViewById(R.id.inputName)
                inputSurname = findViewById(R.id.inputSurname)
                btnAdd = findViewById(R.id.btnAdd)
                btnAdd.setOnClickListener {
                    addLecturer()
                }
            }
            "student" -> {
                setContentView(R.layout.activity_add_student)
                inputName = findViewById(R.id.inputName)
                inputSurname = findViewById(R.id.inputSurname)
                inputBirthDate = findViewById(R.id.inputBirthDate)
                spinnerGroup = findViewById(R.id.spinnerGroup)
                btnAdd = findViewById(R.id.btnAdd)
                setupDatePicker()
                loadGroups()
                btnAdd.setOnClickListener {
                    addStudent()
                }
            }
            "group" -> {
                setContentView(R.layout.activity_add_group)
                inputName = findViewById(R.id.inputName)
                btnAdd = findViewById(R.id.btnAdd)
                btnAdd.setOnClickListener {
                    addGroup()
                }
            }
        }

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

    }

    private fun loadGroups() {
        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getGroups("Bearer ${UserSession.token}")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val groups = response.body()!!

                        // Сопоставляем имена групп с их ID
                        groupsMap = groups.associate { it.name to it.id }

                        // Создаём список, добавив в начало строку "Выберите группу"
                        val groupsWithDefault = listOf(getString(R.string.select_group)) + groupsMap.keys.toList()

                        // Устанавливаем адаптер для Spinner
                        val adapter = ArrayAdapter(
                            this@AddActivity,
                            android.R.layout.simple_spinner_item,
                            groupsWithDefault
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerGroup.adapter = adapter
                    } else {
                        Toast.makeText(
                            this@AddActivity,
                            "Ошибка загрузки групп",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addGroup() {
        val name = inputName.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = GroupRequest(
            name = name
        )

        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.addGroup(
                    token = "Bearer ${UserSession.token}",
                    groupRequest = request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@AddActivity,
                            "Группа добавлен успешно",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddActivity,
                            "Ошибка добавления группы",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addLecturer() {
        val name = inputName.text.toString().trim()
        val surname = inputSurname.text.toString().trim()

        if (name.isEmpty() || surname.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = LecturerRequest(
            name = name,
            surname = surname
        )

        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.addLecturer(
                    token = "Bearer ${UserSession.token}",
                    lecturerRequest = request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@AddActivity,
                            "Лектор добавлен успешно",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddActivity,
                            "Ошибка добавления лектора",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addStudent() {
        val name = inputName.text.toString().trim()
        val surname = inputSurname.text.toString().trim()
        val birthDate = inputBirthDate.text.toString().trim()

        // Получаем выбранное имя группы из Spinner
        val selectedGroupName = spinnerGroup.selectedItem as String
        val groupId = groupsMap[selectedGroupName]

        if (name.isEmpty() || surname.isEmpty() || birthDate.isEmpty() || groupId == null) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = StudentRequest(
            name = name,
            surname = surname,
            birthDate = birthDate,
            groupId = groupId
        )

        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.addStudent(
                    token = "Bearer ${UserSession.token}",
                    studentRequest = request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@AddActivity,
                            "Студент добавлен успешно",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Закрыть активность
                    } else {
                        Toast.makeText(
                            this@AddActivity,
                            "Ошибка добавления студента",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupDatePicker() {
        inputBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Открываем DatePickerDialog
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Преобразование даты в формат yyyy-MM-dd
                val formattedDate = String.format(
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1, // Месяцы начинаются с 0
                    selectedDay
                )
                inputBirthDate.setText(formattedDate)
            }, year, month, day).show()
        }
    }
}
