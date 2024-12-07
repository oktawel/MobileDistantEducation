package com.example.distanteducation

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lecturers)

        val container = findViewById<LinearLayout>(R.id.containerLecturers)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val viewTittle = findViewById<TextView>(R.id.tvTitle)

        val token = UserSession.token
        if (token == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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

        when (type) {
            "lecturer" -> {
                loadLecturers(type, container, token, btnAdd, viewTittle)
            }
            "student" -> {
                loadStudents(type, container, token, btnAdd, viewTittle)
            }
            "group" -> {
                loadGroups(type, container, token, btnAdd, viewTittle)
            }
        }



    }

    private fun loadLecturers(type: String, container: LinearLayout, token: String, btnAdd:Button, viewTittle:TextView) {
        viewTittle.text = "Справочник лекторов"
        btnAdd.text = "Добавить лектора"
        btnAdd.setOnClickListener {
            Toast.makeText(this, "Функция добавления лектора!", Toast.LENGTH_SHORT).show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getLecturers("Bearer $token")
                if (response.isSuccessful) {
                    val lecturers = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (lecturer in lecturers) {
                            addListLecturersView(type, container, lecturer)
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

    private fun loadStudents(type: String, container: LinearLayout, token: String, btnAdd:Button, viewTittle:TextView) {
        viewTittle.text = "Справочник студентов"
        btnAdd.text = "Добавить студента"
        btnAdd.setOnClickListener {
            Toast.makeText(this, "Функция добавления студента!", Toast.LENGTH_SHORT).show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getStudents("Bearer $token")
                if (response.isSuccessful) {
                    val students = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (student in students) {
                            addListStudentsView(type, container, student)
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


    private fun loadGroups(type: String, container: LinearLayout, token: String, btnAdd:Button, viewTittle:TextView) {
        viewTittle.text = "Справочник групп"
        btnAdd.text = "Добавить группу"
        btnAdd.setOnClickListener {
            Toast.makeText(this, "Функция добавления группы!", Toast.LENGTH_SHORT).show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getGroups("Bearer $token")
                if (response.isSuccessful) {
                    val groups = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (group in groups) {
                            addListGroupsView(type, container, group)
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


    private fun addListLecturersView(type: String, container: LinearLayout, lecturer: Lecturer) {
        val lecturerLayout = createLayout(lecturer.id, type)

        val textView = createTextField(lecturer.id, lecturer.name, lecturer.surname, type)
        val editButton = createBtnEdit(lecturer.id, type)
        val deleteButton = createBtnDelete(lecturer.id, type)

        lecturerLayout.addView(textView)
        lecturerLayout.addView(editButton)
        lecturerLayout.addView(deleteButton)

        container.addView(lecturerLayout)
    }

    private fun addListStudentsView(type: String, container: LinearLayout, student: Student) {
        val lecturerLayout = createLayout(student.id, type)

        val textView = createTextField(student.id, student.name, student.surname, type)
        val editButton = createBtnEdit(student.id, type)
        val deleteButton = createBtnDelete(student.id, type)

        lecturerLayout.addView(textView)
        lecturerLayout.addView(editButton)
        lecturerLayout.addView(deleteButton)

        container.addView(lecturerLayout)
    }

    private fun addListGroupsView(type: String, container: LinearLayout, group: Group) {
        val lecturerLayout = createLayout(group.id, type)

        val textView = createTextField(group.id, group.name, "", type)
        val editButton = createBtnEdit(group.id, type)
        val deleteButton = createBtnDelete(group.id, type)

        lecturerLayout.addView(textView)
        lecturerLayout.addView(editButton)
        lecturerLayout.addView(deleteButton)

        container.addView(lecturerLayout)
    }



    private fun createLayout(id:Long, type:String): LinearLayout {
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
            intent.putExtra("Id", id)
            intent.putExtra("type", type)
            startActivity(intent)
        }
        return layout
    }

    private fun createTextField(id:Long, name:String, surname:String, type:String): TextView {
        val textView = TextView(this).apply {
            text = "${name} ${surname}"
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f).apply { setMargins(0, 35, 0, 8) }
        }
        return textView
    }

    private fun createBtnEdit(id:Long, type:String): CardView {
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
                Toast.makeText(context, "Нажата кнопка редактирования", Toast.LENGTH_SHORT).show()
            }
        }
        return editButton
    }

    private fun createBtnDelete(id:Long, type:String): CardView {
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
                Toast.makeText(this@ListsActivity, "Нажата кнопка удаления", Toast.LENGTH_SHORT).show()
            }
        }
        return deleteButton
    }
}

