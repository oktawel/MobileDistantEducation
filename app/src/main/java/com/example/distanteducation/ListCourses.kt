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
import com.example.distanteducation.serverConection.Course
import com.example.distanteducation.serverConection.Group
import com.example.distanteducation.serverConection.Lecturer
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.Student
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListCourses : AppCompatActivity() {
    private lateinit var btnAdd: Button
    private lateinit var container: LinearLayout
    private lateinit var token: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_courses)

        btnAdd = findViewById(R.id.btnAdd)

        token = UserSession.token ?: return

        val userName: TextView = findViewById(R.id.user_name)

        userName.text = UserSession.user!!.name +" "+ UserSession.user!!.surname

        val btnLogout: ImageView = findViewById(R.id.btn_exit_acc)
        btnLogout.setOnClickListener {
            LogoutHelper.performLogout(this)
        }

        loadData()
    }


    override fun onResume() {
        super.onResume()
        loadData()
    }


    private fun loadData() {
        container = findViewById<LinearLayout>(R.id.containerList)
        loadCourses()
    }

    private fun startAdd(){
        val intent = Intent(this, AddActivity::class.java)
        startActivity(intent)
    }

    private fun startEdit(id: Long){
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("Id", id)
        startActivity(intent)
    }

    private fun loadCourses() {
        btnAdd.text = "Добавить курс"
        btnAdd.setOnClickListener {
            startAdd()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getAllCourses("Bearer $token")
                if (response.isSuccessful) {
                    val courses = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (course in courses) {
                            addListCourseView(course)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ListCourses,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ListCourses,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }




    private fun addListCourseView(course: Course) {
        if (UserSession.user!!.role == "Lector"){
            val courseLayout = createLayout(course.id)

            val textView = createTextField(course.name)
            courseLayout.addView(textView)

            val groupButton = createBtnGroups(course.id)
            val editButton = createBtnEdit(course.id)
            val deleteButton = createBtnDelete(course.id)

            courseLayout.addView(groupButton)
            courseLayout.addView(editButton)
            courseLayout.addView(deleteButton)

            container.addView(courseLayout)
        }
        else if (course.groups.any { it.name == UserSession.studentGroup }){

            val courseLayout = createLayout(course.id)

            val textView = createTextField(course.name)
            courseLayout.addView(textView)

            container.addView(courseLayout)
        }
    }


    private fun createLayout(id:Long): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = resources.getDrawable(R.drawable.rounded_border_courses, theme)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 0, 30)
            }
            setPadding(32, 16, 32, 16)
        }
        layout.setOnClickListener{
//            val intent = Intent(this, InfoActivity::class.java)
//            intent.putExtra("Id", id)
//            startActivity(intent)
        }
        return layout
    }

    private fun createTextField(name:String): TextView {
        val textView = TextView(this).apply {
            text = "${name}"
            textSize = 25f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f).apply { setMargins(0, 15, 0, 8) }
        }
        return textView
    }

    private fun createBtnGroups(id:Long): CardView {
        val editButton = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 75f
            cardElevation = 10f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.field_text))
            addView(ImageView(context).apply {
                setImageResource(R.drawable.baseline_groups_24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setColorFilter(ContextCompat.getColor(context, R.color.button))
            })

            setOnClickListener {
//                startEdit(id)
            }
        }
        return editButton
    }

    private fun createBtnEdit(id:Long): CardView {
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
                startEdit(id)
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
                //deleteCourse(id)
            }
        }
        return deleteButton
    }



    private fun deleteCourse(courseId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Удаление курса")
            .setMessage("Вы уверены, что хотите удалить курс?")
            .setPositiveButton("Да") { _, _ ->
                val apiService = RetrofitClient.apiService

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = apiService.deleteLecturer(
                            token = "Bearer ${UserSession.token}",
                            lecturerId = courseId
                        )
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ListCourses,
                                    "Лектор успешно удалён",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadCourses()
                            } else {
                                Toast.makeText(
                                    this@ListCourses,
                                    "Ошибка удаления лектора",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ListCourses,
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

