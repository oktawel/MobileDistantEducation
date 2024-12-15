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
import com.example.distanteducation.serverConection.MarkStudent
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.Student
import com.example.distanteducation.serverConection.Test
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoTestActivity : AppCompatActivity() {
    private lateinit var btnComplete: Button
    private lateinit var tTittle: TextView
    private lateinit var tDescription: TextView
    private lateinit var tMark: TextView
    private lateinit var container: LinearLayout

    private lateinit var token: String
    private lateinit var infoTest: Test


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_test)

        btnComplete = findViewById(R.id.btnComplete)

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

        val test = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("Test", Test::class.java)
        } else {
            intent.getParcelableExtra("Test")
        }
        infoTest = test!!

        tTittle = findViewById(R.id.title)
        tDescription = findViewById(R.id.containerDescription)
        tMark = findViewById(R.id.containerMark)
        container = findViewById(R.id.containerList)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadMarks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getAllMarksStudents(
                    token = "Bearer $token",
                    testId = infoTest.id,
                )
                if (response.isSuccessful) {
                    val marks = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (mark in marks) {
                            addListResultsView(mark)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@InfoTestActivity,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    loadData()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@InfoTestActivity,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addListResultsView(markStudent: MarkStudent) {
        if (UserSession.user!!.role == "Lector"){
            val markLayout = createLayout()
            val textView = createTextField(markStudent)
            markLayout.addView(textView)
            container.addView(markLayout)
        }
    }

    private fun createLayout(): LinearLayout {
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

    private fun createTextField(markStudent: MarkStudent): TextView {
        val resultMark = "%.2f".format(markStudent.mark)
        val textView = TextView(this).apply {
            text = "${markStudent.group} ${markStudent.surname} ${markStudent.name} Оценка: ${resultMark} "
            textSize = 15f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 15, 0, 8) }
        }
        return textView
    }


    private fun loadData(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getTestById(
                    token = UserSession.token!!,
                    testId = infoTest.id,
                    studentId = UserSession.user!!.id!!
                )
                if (response.isSuccessful) {
                    val test = response.body()
                    withContext(Dispatchers.Main) {
                        infoTest = test!!
                        loadTestData()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@InfoTestActivity,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@InfoTestActivity,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadTestData(){
        tTittle.text = infoTest.name
        tDescription.text = infoTest.description

        val resultMark = "%.2f".format(infoTest.mark)
        if (infoTest.mark == 0.toFloat()){
            tMark.text = "Оценка: Ещё не пройден"
            tMark.setTextColor(resources.getColor(R.color.red, theme))
        }
        else{
            tMark.text = "Оценка: ${resultMark}"
            tMark.setTextColor(resources.getColor(R.color.green, theme))
        }

        if (UserSession.user!!.role == "Lector" || infoTest.mark != 0.toFloat() || !(infoTest.open)){
            if (UserSession.user!!.role == "Lector"){
                tMark.visibility = View.GONE
                loadMarks()
            }
            btnComplete.visibility = View.GONE
        }

        btnComplete.setOnClickListener {
            startExecute(infoTest.id)
        }
    }

    private fun startExecute(id: Long){
        val intent = Intent(this, ExecuteTest::class.java)
        intent.putExtra("Id", id)
        startActivity(intent)
    }






}

