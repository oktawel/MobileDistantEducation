package com.example.distanteducation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.OptionRequest
import com.example.distanteducation.serverConection.QuestionRequest
import com.example.distanteducation.serverConection.QuestionType
import com.example.distanteducation.serverConection.UserSession

class AddOption : AppCompatActivity() {

    private lateinit var btnCreate: Button

    private lateinit var inputName: EditText
    private lateinit var toggleBoolean: ToggleButton

    private var flag: Boolean = true

    private lateinit var toggleBooleanConteiner: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_option)
        inputName = findViewById(R.id.inputName)
        btnCreate = findViewById(R.id.btnCreate)
        toggleBoolean = findViewById(R.id.toggleBoolean)
        toggleBooleanConteiner = findViewById(R.id.toggleBooleanConteiner)

        flag = intent.getBooleanExtra("Flag", true)

        btnCreate.setOnClickListener {
            addOption()
        }

        if (!flag){
            toggleBooleanConteiner.visibility = View.GONE
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
    }

    private fun addOption() {
        val text = inputName.text.toString().trim()
        val correct = toggleBoolean.isChecked

        if (text.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val option = OptionRequest(
            text = text,
            correct = correct
        )

        Log.d("AddOption", "Sending result back to AddQuestion")
        val resultIntent = Intent().apply {
            putExtra("Option", option)
        }
        Log.d("AddOption", option.toString())
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
