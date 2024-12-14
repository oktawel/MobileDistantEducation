package com.example.distanteducation

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Group
import com.example.distanteducation.serverConection.Lecturer
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.Student
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListsActivity : AppCompatActivity() {
    private lateinit var btnAdd: Button
    private lateinit var viewTittle: TextView
    private lateinit var type: String
    private lateinit var container: LinearLayout
    private lateinit var token: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lists)

        btnAdd = findViewById(R.id.btnAdd)
        viewTittle = findViewById(R.id.tvTitle)
        token = UserSession.token ?: return
        type = intent.getStringExtra("type") ?: return

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

        loadData()
    }


    override fun onResume() {
        super.onResume()
        loadData()
    }


    private fun loadData() {
        container = findViewById<LinearLayout>(R.id.containerList)

        when (type) {
            "lecturer" -> loadLecturers()
            "student" -> loadStudents()
            "group" -> loadGroups()
        }
    }

    private fun startAdd(){
        val intent = Intent(this, AddActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
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

    private fun loadLecturers() {
        viewTittle.text = "Справочник лекторов"
        btnAdd.text = "Добавить лектора"
        btnAdd.setOnClickListener {
            startAdd()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getLecturers("Bearer $token")
                if (response.isSuccessful) {
                    val lecturers = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (lecturer in lecturers) {
                            addListLecturersView(lecturer)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ListsActivity,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ListsActivity,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadStudents() {
        viewTittle.text = "Справочник студентов"
        btnAdd.text = "Добавить студента"
        btnAdd.setOnClickListener {
            startAdd()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getStudents("Bearer $token")
                if (response.isSuccessful) {
                    val students = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (student in students) {
                            addListStudentsView(student)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ListsActivity,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ListsActivity,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadGroups() {
        viewTittle.text = "Справочник групп"
        btnAdd.text = "Добавить группу"
        btnAdd.setOnClickListener {
            startAdd()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getGroups("Bearer $token")
                if (response.isSuccessful) {
                    val groups = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (group in groups) {
                            addListGroupsView(group)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ListsActivity,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ListsActivity,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun addListLecturersView(lecturer: Lecturer) {
        val lecturerLayout = createLayoutLecturer(lecturer)

        val textView = createTextField(lecturer.id, lecturer.name, lecturer.surname)
        val editButton = createBtnEditLecturer(lecturer)
        val deleteButton = createBtnDelete(lecturer.id)

        lecturerLayout.addView(textView)
        lecturerLayout.addView(editButton)
        lecturerLayout.addView(deleteButton)

        container.addView(lecturerLayout)
    }

    private fun addListStudentsView(student: Student) {
        val lecturerLayout = createLayoutStudent(student)

        val textView = createTextField(student.id, student.name, student.surname)
        val editButton = createBtnEditStudent(student)
        val deleteButton = createBtnDelete(student.id)

        lecturerLayout.addView(textView)
        lecturerLayout.addView(editButton)
        lecturerLayout.addView(deleteButton)

        container.addView(lecturerLayout)
    }

    private fun addListGroupsView(group: Group) {
        val lecturerLayout = createLayoutGroup(group)

        val textView = createTextField(group.id, group.name, "")
        val editButton = createBtnEditGroup(group)
        val deleteButton = createBtnDelete(group.id)

        lecturerLayout.addView(textView)
        lecturerLayout.addView(editButton)
        lecturerLayout.addView(deleteButton)

        container.addView(lecturerLayout)
    }



    private fun createLayoutStudent(student: Student): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 0, 8)
            }
            setPadding(16, 16, 16, 16)
        }
        layout.setOnClickListener{
            val intent = Intent(this, InfoActivity::class.java)
            intent.putExtra("type", type)
            intent.putExtra("Student", student)
            startActivity(intent)
        }
        return layout
    }

    private fun createLayoutLecturer(lecturer: Lecturer): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 0, 8)
            }
            setPadding(16, 16, 16, 16)
        }
        layout.setOnClickListener{
            val intent = Intent(this, InfoActivity::class.java)
            intent.putExtra("type", type)
            intent.putExtra("Lecturer", lecturer)
            startActivity(intent)
        }
        return layout
    }

    private fun createLayoutGroup(group: Group): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 0, 8)
            }
            setPadding(16, 16, 16, 16)
        }
        layout.setOnClickListener{
            val intent = Intent(this, InfoActivity::class.java)
            intent.putExtra("type", type)
            intent.putExtra("Group", group)
            startActivity(intent)
        }
        return layout
    }

    private fun createTextField(id:Long, name:String, surname:String,): TextView {
        val textView = TextView(this).apply {
            text = "${name} ${surname}"
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f).apply { setMargins(0, 35, 0, 8) }
        }
        return textView
    }

    private fun createBtnEditLecturer(lecturer: Lecturer): CardView {
        val editButton = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 75f
            cardElevation = 10f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.field_text))
            addView(ImageView(context).apply {
                setImageResource(R.drawable.baseline_edit_24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setColorFilter(ContextCompat.getColor(context, R.color.button))
            })

            setOnClickListener {
                startEditLecturer(lecturer)
            }
        }
        return editButton
    }

    private fun createBtnEditGroup(group: Group): CardView {
        val editButton = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 75f
            cardElevation = 10f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.field_text))
            addView(ImageView(context).apply {
                setImageResource(R.drawable.baseline_edit_24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setColorFilter(ContextCompat.getColor(context, R.color.button))
            })

            setOnClickListener {
                startEditGroup(group)
            }
        }
        return editButton
    }

    private fun createBtnEditStudent(student: Student): CardView {
        val editButton = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 75f
            cardElevation = 10f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.field_text))
            addView(ImageView(context).apply {
                setImageResource(R.drawable.baseline_edit_24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setColorFilter(ContextCompat.getColor(context, R.color.button))
            })

            setOnClickListener {
                startEditStudent(student)
            }
        }
        return editButton
    }

    private fun createBtnDelete(id:Long): CardView {
        val deleteButton = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 75f
            cardElevation = 10f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.redDelete))
            addView(ImageView(context).apply {
                setImageResource(R.drawable.baseline_delete_24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setColorFilter(ContextCompat.getColor(context, R.color.button))
            })
            setOnClickListener {
                when (type) {
                    "lecturer" -> deleteLecturer(id)
                    "student" -> deleteStudent(id)
                    "group" -> deleteGroup(id)
                }
            }
        }
        return deleteButton
    }



    private fun deleteStudent(studentId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Удаление студента")
            .setMessage("Вы уверены, что хотите удалить студента?")
            .setPositiveButton("Да") { _, _ ->
                val apiService = RetrofitClient.apiService

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = apiService.deleteStudent(
                            token = "Bearer ${UserSession.token}",
                            studentId = studentId
                        )
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Студент успешно удалён",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadStudents()
                            } else {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Ошибка удаления студента",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ListsActivity,
                                "Ошибка: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteLecturer(lecturerId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Удаление лектора")
            .setMessage("Вы уверены, что хотите удалить лектора?")
            .setPositiveButton("Да") { _, _ ->
                val apiService = RetrofitClient.apiService

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = apiService.deleteLecturer(
                            token = "Bearer ${UserSession.token}",
                            lecturerId = lecturerId
                        )
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Лектор успешно удалён",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadLecturers()
                            } else {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Ошибка удаления лектора",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ListsActivity,
                                "Ошибка: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteGroup(groupId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Удаление группы")
            .setMessage("Вы уверены, что хотите удалить группу?")
            .setPositiveButton("Да") { _, _ ->
                val apiService = RetrofitClient.apiService

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = apiService.deleteGroup(
                            token = "Bearer ${UserSession.token}",
                            groupId = groupId
                        )
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Группа успешно удалёна",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadGroups()
                            } else {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Ошибка удаления группы",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ListsActivity,
                                "Ошибка: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }


}

