package com.example.distanteducation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Course
import com.example.distanteducation.serverConection.CourseRequest
import com.example.distanteducation.serverConection.OptionRequest
import com.example.distanteducation.serverConection.QuestionRequest
import com.example.distanteducation.serverConection.QuestionType
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.Test
import com.example.distanteducation.serverConection.TestRequest
import com.example.distanteducation.serverConection.UserSession
import com.google.android.material.internal.ViewUtils.hideKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddQuestion : AppCompatActivity() {

    private lateinit var btnAdd: ImageButton
    private lateinit var btnCreate: Button

    private lateinit var inputName: EditText
    private lateinit var inputPoint: EditText
    private lateinit var inputFreeAnswer: EditText
    private lateinit var radioGroup: RadioGroup

    private lateinit var inputTextAnswers: TextView

    private lateinit var spinnerType: Spinner

    private lateinit var freeAnswerContainer: LinearLayout
    private lateinit var optionsContainer: ScrollView
    private lateinit var TFAnswerContainer: LinearLayout
    private lateinit var container: LinearLayout

    private lateinit var addOptionLauncher: ActivityResultLauncher<Intent>

    private var addFormOptionList: MutableList<OptionRequest>? = null

    private var flagForOne: Boolean = true

    private val questionTypes = listOf(
        QuestionType(1, "Свободный ответ"),
        QuestionType(2, "Один ответ"),
        QuestionType(3, "Несколько ответов"),
        QuestionType(4, "True/False")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_question)
        inputName = findViewById(R.id.inputName)
        inputPoint = findViewById(R.id.inputPoints)
        spinnerType = findViewById(R.id.questionType)
        btnAdd = findViewById(R.id.btnAddOption)
        btnCreate = findViewById(R.id.btnCreate)
        inputFreeAnswer = findViewById(R.id.inputFreeAnswer)
        radioGroup = findViewById<RadioGroup>(R.id.radioGroupTrueFalse)
        freeAnswerContainer = findViewById(R.id.freeAnswerContainer)
        optionsContainer = findViewById(R.id.optionsContainer)
        inputTextAnswers = findViewById(R.id.textAnswers)
        TFAnswerContainer = findViewById(R.id.TFAnswerContainer)
        container = findViewById(R.id.optionsList)

        btnAdd.setOnClickListener {
            addOption()
        }

        btnCreate.setOnClickListener {
            addQuestion()
        }

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

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            questionTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        initialization()
                        flagForOne = true
                        inputTextAnswers.text = "Введите правильный ответ к вопросу"
                        freeAnswerContainer.visibility = View.VISIBLE
                        optionsContainer.visibility = View.GONE
                        TFAnswerContainer.visibility = View.GONE
                    }

                    1 ->{
                        initialization()
                        inputTextAnswers.text = "Ответы к вопросу"
                        freeAnswerContainer.visibility = View.GONE
                        optionsContainer.visibility = View.VISIBLE
                        TFAnswerContainer.visibility = View.GONE
                        flagForOne = true
                    }
                    2-> {
                        initialization()
                        inputTextAnswers.text = "Ответы к вопросу"
                        freeAnswerContainer.visibility = View.GONE
                        optionsContainer.visibility = View.VISIBLE
                        TFAnswerContainer.visibility = View.GONE
                        flagForOne = true
                    }
                    3 ->{
                        initialization()
                        inputTextAnswers.text = "Выберите правильный ответ к вопросу"
                        freeAnswerContainer.visibility = View.GONE
                        optionsContainer.visibility = View.GONE
                        TFAnswerContainer.visibility = View.VISIBLE
                        flagForOne = true
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        addOptionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d("AddQuestion", result.toString())
            if (result.resultCode == RESULT_OK) {
                val option = result.data?.getParcelableExtra<OptionRequest>("Option")
                Log.d("AddQuestion", option.toString())
                if (option != null) {
                    if (questionTypes[spinnerType.selectedItemPosition].id == 2 && option.correct == true) {
                        flagForOne = false
                    }
                    addFormOptionList!!.add(option)
                    loadContentOptions()
                    hideKeyboard()
                }
            }
        }

        btnAdd.setOnClickListener {
            addOption()
        }
    }

    private fun initialization() {
        addFormOptionList = mutableListOf()
        container.removeAllViews()
        addFormOptionList!!.clear()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun addOption() {
        val intent = Intent(this, AddOption::class.java)
        intent.putExtra("Flag", flagForOne)
        addOptionLauncher.launch(intent)
    }
    private fun loadContentOptions() {
        container.removeAllViews()
        addFormOptionList!!.forEach {
            addListView(container, it)
        }
    }

    private fun addListView(container: LinearLayout, option: OptionRequest) {
            val layout = createLayout()
            val textView = createTextField(option)
            val btnDelete = createBtnDelete(option)
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

    private fun createTextField(option: OptionRequest): TextView {
        val textView = TextView(this).apply {
            if (option.correct == true){
                text = "Вариант ответа: ${option.text} (В)"
            }
            else{
                text = "Вариант ответа: ${option.text} (Н)"
            }
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 15, 0, 8) }
        }
        return textView
    }

    private fun createBtnDelete(option: OptionRequest): CardView {
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
                addFormOptionList!!.remove(option)
                if (option.correct == true){
                    flagForOne = true
                }
                loadContentOptions()
            }
        }
        return deleteButton
    }

    private fun addQuestion() {
        val text = inputName.text.toString().trim()
        val type = questionTypes[spinnerType.selectedItemPosition].id
        val cost = inputPoint.text?.toString()?.toIntOrNull()

        if (type == 1){
            val option = OptionRequest(
                text = inputFreeAnswer.text.toString(),
                correct = null
            )
            addFormOptionList!!.add(option)
        }
        else if(type == 4){
            val selectedId = radioGroup.checkedRadioButtonId

            if (selectedId != -1) {
                val selectedRadioButton = findViewById<RadioButton>(selectedId)
                val selectedText = selectedRadioButton.text.toString()

                if (selectedText == "Верно"){
                    val option1 = OptionRequest(
                        text = "Верно",
                        correct = true
                    )
                    val option2 = OptionRequest(
                        text = "Неверно",
                        correct = false
                    )
                    addFormOptionList!!.add(option1)
                    addFormOptionList!!.add(option2)
                }else if (selectedText == "Неверно"){
                    val option1 = OptionRequest(
                        text = "Верно",
                        correct = false
                    )
                    val option2 = OptionRequest(
                        text = "Неверно",
                        correct = true
                    )
                    addFormOptionList!!.add(option1)
                    addFormOptionList!!.add(option2)
                }

            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return
            }

        }

        if (text.isEmpty() || cost == null) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val question = QuestionRequest(
            text = text,
            typeId = type.toLong(),
            cost = cost,
            addFormOptionList = addFormOptionList!!
        )

        val resultIntent = Intent().apply {
            putExtra("Question", question)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
