package com.example.distanteducation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.OptionRequest
import com.example.distanteducation.serverConection.QuestionRequest
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.TestRequest
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTest : AppCompatActivity() {

    private lateinit var btnAdd: ImageButton
    private lateinit var btnCreate: Button
    private lateinit var inputName: EditText
    private lateinit var inputDescription: EditText

    private lateinit var containerList: LinearLayout

    private lateinit var addQuestionLauncher: ActivityResultLauncher<Intent>

    private var courseId: Long = 0
    private var addFormQuestionList: MutableList<QuestionRequest> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_test)

        inputName = findViewById(R.id.inputName)
        inputDescription = findViewById(R.id.inputDescription)
        btnAdd = findViewById(R.id.btnAdd)
        btnCreate = findViewById(R.id.btnCreate)
        containerList = findViewById(R.id.containerList)


        initialization()

        addQuestionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val question = result.data?.getParcelableExtra<QuestionRequest>("Question")
                if (question != null) {
                    addFormQuestionList.add(question)
                    loadContentQuestions()
                    hideKeyboard()
                }
            }
        }

        btnAdd.setOnClickListener {
            addQuestion()
        }

        btnCreate.setOnClickListener {
            addTest()
        }

        courseId = intent.getLongExtra("courseId", 0)

        val userName: TextView = findViewById(R.id.user_name)
        userName.text = UserSession.user!!.name + " " + UserSession.user!!.surname

        val btnLogout: ImageView = findViewById(R.id.btn_exit_acc)
        btnLogout.setOnClickListener {
            LogoutHelper.performLogout(this)
        }
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initialization() {
        addFormQuestionList = mutableListOf()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun addQuestion() {
        val intent = Intent(this, AddQuestion::class.java)
        addQuestionLauncher.launch(intent)
    }

    private fun loadContentQuestions() {
        containerList.removeAllViews()
        addFormQuestionList!!.forEach {
            addListView(containerList, it)
        }
    }

    private fun addListView(container: LinearLayout, question: QuestionRequest) {
        val layout = createLayout()
        val textView = createTextField(question)
        val btnDelete = createBtnDelete(question)
        layout.addView(textView)
        layout.addView(btnDelete)
        container.addView(layout)
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

    private fun createTextField(question: QuestionRequest): TextView {
        val textView = TextView(this).apply {
            text = "Вопрос: ${question.text} (${question.cost})"
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 15, 0, 8) }
        }
        return textView
    }

    private fun createBtnDelete(question: QuestionRequest): CardView {
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
                addFormQuestionList.remove(question)
                loadContentQuestions()
            }
        }
        return deleteButton
    }

    private fun addTest() {
        val name = inputName.text.toString().trim()
        val description = inputDescription.text.toString().trim()

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = TestRequest(
            name = name,
            description = description,
            open = false,
            subjectId = courseId,
            addFormQuestionList = addFormQuestionList
        )

        val apiService = RetrofitClient.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.addTest(
                    token = "Bearer ${UserSession.token}",
                    testRequest = request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@AddTest,
                            "Тест успешно добавлен",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Закрыть активность
                    } else {
                        Toast.makeText(
                            this@AddTest,
                            "Ошибка создания теста",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddTest,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
