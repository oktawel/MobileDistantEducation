package com.example.distanteducation

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Group
import com.example.distanteducation.serverConection.GroupRequestUpdate
import com.example.distanteducation.serverConection.LecturerRequestUpdate
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.StudentRequest
import com.example.distanteducation.serverConection.StudentRequestUpdate
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.log

class EditActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editSurname: EditText
    private lateinit var editBirthDate: EditText
    private lateinit var spinnerGroup: Spinner
    private lateinit var editLogin: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnEdit: Button
    private lateinit var type: String

    private var groupsMap: Map<String, Long> = emptyMap()

    private var Id: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        type = intent.getStringExtra("type") ?: return
        Id = intent.getLongExtra("Id", 0)

        when (type) {
            "lecturer" -> {
                setContentView(R.layout.activity_update_lecturer)
                editName = findViewById(R.id.editName)
                editSurname = findViewById(R.id.editSurname)
                editLogin = findViewById(R.id.editLogin)
                editPassword = findViewById(R.id.editPassword)
                btnEdit = findViewById(R.id.btnEdit)
                loadLecturerData(Id)
                btnEdit.setOnClickListener {
                    updateLecturer()
                }
            }
            "student" -> {
                setContentView(R.layout.activity_update_student)
                editName = findViewById(R.id.editName)
                editSurname = findViewById(R.id.editSurname)
                editBirthDate = findViewById(R.id.editBirthDate)
                spinnerGroup = findViewById(R.id.editSpinnerGroup)
                editLogin = findViewById(R.id.editLogin)
                editPassword = findViewById(R.id.editPassword)
                btnEdit = findViewById(R.id.btnEdit)
                loadStudentData(Id)
                loadGroups()
                btnEdit.setOnClickListener {
                    updateStudent()
                }
                editBirthDate.setOnClickListener {
                    showDatePicker(editBirthDate.text.toString())
                }
            }
            "group" -> {
                setContentView(R.layout.activity_update_group)
                editName = findViewById(R.id.editName)
                btnEdit = findViewById(R.id.btnEdit)
                loadGroupData(Id)
                btnEdit.setOnClickListener {
                    updateGroup()
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

    private fun loadLecturerData(lecturerId: Long) {
        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getLecturerDetails(
                    token = "Bearer ${UserSession.token}",
                    lecurertId = lecturerId
                )

                if (response.isSuccessful && response.body() != null) {
                    val lecturer = response.body()!!

                    withContext(Dispatchers.Main) {
                        editName.setText(lecturer.name)
                        editSurname.setText(lecturer.surname)
                        editLogin.setText(lecturer.userLogin)
                        editPassword.setText(lecturer.userPassword)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditActivity, "Ошибка получения данных", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadStudentData(studentId: Long) {
        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getStudentDetails(
                    token = "Bearer ${UserSession.token}",
                    studentId = studentId
                )

                if (response.isSuccessful && response.body() != null) {
                    val student = response.body()!!

                    withContext(Dispatchers.Main) {
                        editName.setText(student.name)
                        editSurname.setText(student.surname)
                        editBirthDate.setText(student.birthDate)
                        editLogin.setText(student.userLogin)
                        editPassword.setText(student.userPassword)

                        val groupName = student.group

                        if (groupsMap.containsKey(groupName)) {
                            val groupIndex = groupsMap.keys.indexOf(groupName)
                            spinnerGroup.setSelection(groupIndex)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditActivity, "Ошибка получения данных студента", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadGroupData(groupId: Long) {
        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getGroupDetails(
                    token = "Bearer ${UserSession.token}",
                    grouptId = groupId
                )

                if (response.isSuccessful && response.body() != null) {
                    val group = response.body()!!

                    withContext(Dispatchers.Main) {
                        editName.setText(group.name)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditActivity, "Ошибка получения данных", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
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

                        groupsMap = groups.associate { it.name to it.id }

                        val adapter = ArrayAdapter(
                            this@EditActivity,
                            android.R.layout.simple_spinner_item,
                            groupsMap.keys.toList()
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerGroup.adapter = adapter
                    } else {
                        Toast.makeText(this@EditActivity, "Ошибка загрузки групп", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateStudent() {
        val name = editName.text.toString().trim()
        val surname = editSurname.text.toString().trim()
        val birthDate = editBirthDate.text.toString().trim()
        val login = editLogin.text.toString().trim()
        val password = editPassword.text.toString().trim()

        val selectedGroupName = spinnerGroup.selectedItem as String
        val groupId = groupsMap[selectedGroupName]

        if (name.isEmpty() || surname.isEmpty() || birthDate.isEmpty() || login.isEmpty() || password.isEmpty() || groupId == null) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = StudentRequestUpdate(
            id = Id,
            login = login,
            password = password,
            name = name,
            surname = surname,
            birthDate = birthDate,
            groupId = groupId
        )

        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.updateStudent(
                    token = "Bearer ${UserSession.token}",
                    studentRequest = request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditActivity, "Студент обновлен успешно", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Log.d("WorkWithServer", request.toString())
                        Log.d("WorkWithServer", response.toString())
                        Log.d("WorkWithServer", response.errorBody().toString())
                        Toast.makeText(this@EditActivity, "Ошибка обновления студента", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateLecturer() {
        val name = editName.text.toString().trim()
        val surname = editSurname.text.toString().trim()
        val login = editLogin.text.toString().trim()
        val password = editPassword.text.toString().trim()

        if (name.isEmpty() || surname.isEmpty() || login.isEmpty() || password.isEmpty() ) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = LecturerRequestUpdate(
            id = Id,
            login = login,
            password = password,
            name = name,
            surname = surname,
        )

        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.updateLecturer(
                    token = "Bearer ${UserSession.token}",
                    lecturerRequest = request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditActivity, "Лектор обновлен успешно", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditActivity, "Ошибка обновления лектора", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateGroup() {
        val name = editName.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = GroupRequestUpdate(
            id = Id,
            name = name,
        )

        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.updateGroup(
                    token = "Bearer ${UserSession.token}",
                    groupRequest = request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditActivity, "Группа обновлена успешно", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditActivity, "Ошибка обновления группы", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun showDatePicker(inputDate: String) {
        var year = 2024
        var month = 11
        var day = 2

        if (inputDate.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(inputDate)

                if (date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date

                    year = calendar.get(Calendar.YEAR)
                    month = calendar.get(Calendar.MONTH)
                    day = calendar.get(Calendar.DAY_OF_MONTH)

                }
            } catch (e: Exception) {
                Toast.makeText(this, "Неверный формат даты", Toast.LENGTH_SHORT).show()
            }
        }

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                editBirthDate.setText(formattedDate)
            },
            year, month, day
        )
        datePicker.show()
    }

}