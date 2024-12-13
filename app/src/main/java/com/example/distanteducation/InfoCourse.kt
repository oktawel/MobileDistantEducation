package com.example.distanteducation

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.transition.Visibility
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Course
import com.example.distanteducation.serverConection.Group
import com.example.distanteducation.serverConection.Lecturer
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.Student
import com.example.distanteducation.serverConection.Test
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoCourse : AppCompatActivity() {
    private lateinit var btnAdd: Button
    private lateinit var tTittle: TextView
    private lateinit var tDescription: TextView
    private lateinit var container: LinearLayout


    private lateinit var token: String
    private lateinit var infoCourse: Course


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_course)

        btnAdd = findViewById(R.id.btnAdd)

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
        infoCourse = course!!

        tTittle = findViewById(R.id.title)
        tDescription = findViewById(R.id.containerDescription)

        tTittle.text = course.name
        tDescription.text = course.description

        if (UserSession.user!!.role == "Lector"){
            btnAdd.visibility = View.VISIBLE
            btnAdd.text = "Добавить тест"
            btnAdd.setOnClickListener {
                startAdd()
            }
        }
        else{
            btnAdd.visibility = View.GONE
        }
        loadData()
    }


    override fun onResume() {
        super.onResume()
        loadData()
    }


    private fun loadData() {
        container = findViewById<LinearLayout>(R.id.containerList)
        loadTests()
    }

    private fun startAdd(){
        val intent = Intent(this, AddCourse::class.java)
        startActivity(intent)
    }

    private fun startEdit(course: Course){
        val intent = Intent(this, EditCourseActivity::class.java)
        intent.putExtra("Course", course)
        startActivity(intent)
    }

    private fun startUpdateGroups(course: Course){
        val intent = Intent(this, CourseGroups::class.java)
        intent.putExtra("Course", course)
        startActivity(intent)
    }

    private fun loadTests() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getAllTestsByCourse(
                    token = "Bearer $token",
                    courseId = infoCourse.id,
                    studentId = ((if (UserSession.user!!.role == "Lector") {
                        0
                    } else {
                        UserSession.user!!.id
                    })!!)
                )
                if (response.isSuccessful) {
                    val tests = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (test in tests) {
                            addListTestView(test)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@InfoCourse,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    loadData()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@InfoCourse,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }




    private fun addListTestView(test: Test) {
        if (UserSession.user!!.role == "Lector"){
            val testLayout = createLayout(test)

            val textView = createTextField(test.name)

            testLayout.addView(textView)

            val editButton = createBtnEdit(test)
            val deleteButton = createBtnDelete(test.id)


            testLayout.addView(editButton)
            testLayout.addView(deleteButton)

            container.addView(testLayout)
        }
        else{
            val testLayout = createLayout(test)

            val textView = createTextField(test.name)

            testLayout.addView(textView)

            val textViewMark = createMarkField(test.mark!!)

            testLayout.addView(textViewMark)

            container.addView(testLayout)
        }
    }


    private fun createLayout(test: Test): LinearLayout {
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
            val intent = Intent(this, InfoTestActivity::class.java)
            intent.putExtra("Test", test)
            startActivity(intent)
        }
        return layout
    }

    private fun createTextField(name:String): TextView {
        val textView = TextView(this).apply {
            text = "${name}"
            textSize = 25f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 15, 0, 8) }
        }
        return textView
    }

    private fun createMarkField(mark: Float): TextView {
        val textView = TextView(this).apply {
            val resultMark = "%.2f".format(mark)
            if (mark == 0.toFloat()){
                text = "Ещё не пройден"
                setTextColor(ContextCompat.getColor(context, R.color.red))
            }
            else{
                text = "Оценка: ${resultMark}"
                setTextColor(ContextCompat.getColor(context, R.color.green))
            }
            textSize = 20f
            gravity = Gravity.END

            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 15, 0, 8) }
        }
        return textView
    }

    private fun createBtnEdit(test: Test): CardView {
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
                //startEdit(course)
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
                        val response = apiService.deleteCourse(
                            token = "Bearer ${UserSession.token}",
                            courseId = courseId
                        )
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@InfoCourse,
                                    "Курс успешно удалён",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadTests()
                            } else {
                                Toast.makeText(
                                    this@InfoCourse,
                                    "Ошибка удаления курса",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@InfoCourse,
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

