package com.example.distanteducation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.functions.QuestionsAdapter
import com.example.distanteducation.functions.QuestionsAdapter.Companion.TYPE_MULTIPLE_CHOICE
import com.example.distanteducation.serverConection.OptionAnswer
import com.example.distanteducation.serverConection.QuestionAnswer
import com.example.distanteducation.serverConection.QuestionExecute
import com.example.distanteducation.serverConection.QuestionType
import com.example.distanteducation.serverConection.QuestionType.*
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.TestAnswer
import com.example.distanteducation.serverConection.TestExecute
import com.example.distanteducation.serverConection.UserSession
import com.example.distanteducation.serverConection.UserSession.token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class ExecuteTest : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var submitButton: Button
    private lateinit var tittle: TextView
    private lateinit var answers: MutableMap<Long, MutableList<OptionAnswer>>

    private var testId: Long = 0
    private lateinit var test: TestExecute

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_execute_test)
        tittle = findViewById(R.id.tvTitle)
        recyclerView = findViewById(R.id.questionsRecyclerView)
        submitButton = findViewById(R.id.submitButton)
        answers = mutableMapOf()

        // Установка временного пустого адаптера
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = QuestionsAdapter(emptyList()) { _, _ -> }

        testId = intent.getLongExtra("Id", 0)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.openTest(
                    token = token!!,
                    testId = testId
                )
                if (response.isSuccessful) {
                    test = response.body()!!
                    withContext(Dispatchers.Main) {
                        load()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ExecuteTest,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ExecuteTest,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val userName: TextView = findViewById(R.id.user_name)

        userName.text = UserSession.user!!.name +" "+ UserSession.user!!.surname

        val btnLogout: ImageView = findViewById(R.id.btn_exit_acc)
        btnLogout.setOnClickListener {
            LogoutHelper.performLogout(this)
        }
    }

    private fun load() {
        tittle.text = test.name
        // Установка адаптера с реальными данными
        setupRecyclerView(test.questions)

        // Обработка нажатия кнопки отправки
        submitButton.setOnClickListener {
            if (validateAnswers(test.questions)) {
                val studentId = UserSession.user!!.id // Получаем ID студента из сессии
                val testAnswer = createTestAnswer(test.id, studentId!!)
                submitResults(testAnswer)
            } else {
                Toast.makeText(this, "Пожалуйста, ответьте на все вопросы", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupRecyclerView(questions: List<QuestionExecute>) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = QuestionsAdapter(questions) { questionId, optionAnswer ->
            if (!answers.containsKey(questionId)) {
                answers[questionId] = mutableListOf()
            }

            answers[questionId]?.apply {
                // Проверяем тип вопроса
                val question = questions.firstOrNull { it.id == questionId }
                if (question?.typeQuestionID == TYPE_MULTIPLE_CHOICE.toLong()) {
                    // Для вопросов с несколькими ответами обновляем только соответствующий вариант
                    if (optionAnswer.optionId != null) {
                        removeIf { it.optionId == optionAnswer.optionId } // Убираем, если ответ уже есть
                        if (optionAnswer.optionId != null) add(optionAnswer) // Добавляем только выбранные ответы
                    }
                } else {
                    // Для вопросов с одним ответом заменяем предыдущий ответ
                    if (optionAnswer.optionId != null) {
                        removeIf { it.optionId != null } // Удаляем предыдущий выбор
                    }
                    Log.d("question", optionAnswer.toString())
                    if (optionAnswer.textAnswer != null) {
                        removeIf { it.textAnswer != null } // Удаляем старые текстовые ответы
                    }
                    add(optionAnswer)
                }
            }

        }

    }

    private fun validateAnswers(questions: List<QuestionExecute>): Boolean {
        return questions.all { question ->
            val answerList = answers[question.id]
            if (question.options.isNullOrEmpty()) {
                // Для вопросов без вариантов ответа проверяем текст
                answerList?.any { it.textAnswer != null } ?: false
            } else {
                // Для вопросов с вариантами выбора
                answerList?.any { it.optionId != null } ?: false
            }
        }
    }

    private fun createTestAnswer(testId: Long, studentId: Long): TestAnswer {
        val answerQuestions = answers.map { (questionId, options) ->
            QuestionAnswer(
                questionId = questionId,
                answerOptions = options
            )
        }

        return TestAnswer(
            testId = testId,
            studentId = studentId,
            answerQuestions = answerQuestions
        )
    }

    private fun submitResults(testAnswer: TestAnswer) {
        val apiService = RetrofitClient.apiService
        Log.d("question", testAnswer.toString())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.answerTest("Bearer ${UserSession.token}", testAnswer)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ExecuteTest, "Результаты сохранены", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ExecuteTest, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExecuteTest, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


