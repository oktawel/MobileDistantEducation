package com.example.distanteducation

import android.content.Intent
import android.os.Build
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
    private lateinit var editButton: Button

    private lateinit var type: String
    private var Id: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = UserSession.token
        if (token == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        type = intent.getStringExtra("type")!!


        when (type) {
            "lecturer" -> {
                setContentView(R.layout.activity_info_lecturer)
                initializeFields()
                tSurname = findViewById(R.id.containerSurname)
                tLogin = findViewById(R.id.containerLogin)
                tPassword = findViewById(R.id.containerPassword)
                val lecturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("Lecturer", Lecturer::class.java)
                } else {
                    intent.getParcelableExtra("Lecturer")
                }
                loadLecturerDetails(lecturer!!)
                Id = lecturer.id
            }
            "student" -> {
                setContentView(R.layout.activity_info_student)
                initializeFields()
                tSurname = findViewById(R.id.containerSurname)
                tGroup = findViewById(R.id.containerGroup)
                tBDate = findViewById(R.id.containerBDate)
                tLogin = findViewById(R.id.containerLogin)
                tPassword = findViewById(R.id.containerPassword)
                val student = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("Student", Student::class.java)
                } else {
                    intent.getParcelableExtra("Student")
                }
                loadStudentDetails(student!!)
                Id = student.id
            }
            "group" -> {
                setContentView(R.layout.activity_info_group)
                initializeFields()
                val group = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("Group", Group::class.java)
                } else {
                    intent.getParcelableExtra("Group")
                }
                loadGroupDetails(group!!)
                Id = group.id
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

    private fun initializeFields() {
        editButton = findViewById(R.id.edit_button)
        tTittle = findViewById(R.id.title)
        tName = findViewById(R.id.containerName)
    }

    override fun onResume() {
        super.onResume()
        when (type) {
            "lecturer" -> loadLecturerDetails(Id)
            "student" -> loadStudentDetails(Id)
            "group" -> loadGroupDetails(Id)
        }
    }

    private fun loadGroupDetails(group: Group) {
        tTittle.text = "Информация о группе"
        displayGrouprInfo(group)
    }

    private fun loadLecturerDetails(lecturer: Lecturer) {
        tTittle.text = "Информация о лекторе"
        displayLecturerInfo(lecturer)
    }

    private fun loadStudentDetails(student: Student) {
        tTittle.text = "Информация о студенте"
        displayStudentInfo(student)
    }

    private fun displayGrouprInfo(group: Group) {
        tName.text = group.name

        editButton.setOnClickListener{
            startEditGroup(group)
        }
    }
    private fun displayStudentInfo(student: Student) {
        tName.text = student.name
        tSurname.text = student.surname
        tGroup.text = student.group
        tBDate.text = student.birthDate
        tLogin.text = student.userLogin
        tPassword.text = student.userPassword

        editButton.setOnClickListener{
            startEditStudent(student)
        }
    }
    private fun displayLecturerInfo(lecturer: Lecturer) {

        tName.text = lecturer.name
        tSurname.text = lecturer.surname
        tLogin.text = lecturer.userLogin
        tPassword.text = lecturer.userPassword

        editButton.setOnClickListener{
            startEditLecturer(lecturer)
        }
    }


    private fun startEditLecturer(lecturer: Lecturer){
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("Lecturer", lecturer)
        startActivity(intent)
    }

    private fun startEditGroup(group: Group){
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("Group", group)

        startActivity(intent)
    }

    private fun startEditStudent(student: Student){
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("Student", student)
        startActivity(intent)
    }



    private fun loadGroupDetails(Id: Long) {
        val apiService = RetrofitClient.apiService
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
}
