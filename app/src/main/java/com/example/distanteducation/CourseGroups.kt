package com.example.distanteducation

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Course
import com.example.distanteducation.serverConection.Group
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.RetrofitClient.apiService
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CourseGroups: AppCompatActivity() {
    private lateinit var container: LinearLayout
    private lateinit var token: String
    private lateinit var connectedGroups: List<Group>
    private var courseId: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_groups)

        token = UserSession.token ?: return

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

        val course = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("Course", Course::class.java)
        } else {
            intent.getParcelableExtra("Course")
        }

        courseId = course!!.id
        connectedGroups = course.groups


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


    private fun loadCourses() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getGroups("Bearer $token")
                if (response.isSuccessful) {
                    val groups = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (group in groups) {
                            addListGroupView(group)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@CourseGroups,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CourseGroups,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addListGroupView(group: Group) {
        val groupLayout = createLayout(group.id)

        val textView = createTextField(group.name)
        groupLayout.addView(textView)

        val groupSwitch = createSwitchGroup(group.id)

        groupLayout.addView(groupSwitch)

        container.addView(groupLayout)

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

    private fun createSwitchGroup(id: Long): SwitchCompat {
        val switchButton = SwitchCompat(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f
            ).apply {
                setMargins(0, 15, 0, 8)
            }

            isChecked = connectedGroups.any { it.id == id }

            setOnCheckedChangeListener { _, isChecked ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (isChecked) {
                                val response = apiService.connectGroup(
                                    token = "Bearer ${UserSession.token}",
                                    courseId = courseId,
                                    groupId = id
                                )

                            } else {
                                val response = apiService.removeGroup(
                                    token = "Bearer ${UserSession.token}",
                                    courseId = courseId,
                                    groupId = id
                                )
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.d("groupWorkException", e.message.toString())
                            }
                        }
                    }
            }
        }
        return switchButton
    }










}

