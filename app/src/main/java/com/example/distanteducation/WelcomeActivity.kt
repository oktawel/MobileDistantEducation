package com.example.distanteducation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.transition.Visibility
import com.example.distanteducation.DB.UserDatabaseHelper
import com.example.distanteducation.functions.LoginHelper

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val contentLayout = findViewById<LinearLayout>(R.id.ll_content)
        val titleContentLayout: TextView = findViewById(R.id.later_logins)

        val dbHelper = UserDatabaseHelper(this)

        // Загрузка данных из базы
        val users = dbHelper.getAllUsers()

        // Очистка контейнера перед добавлением данных
        contentLayout.removeAllViews()

        if (users.isEmpty()){
            titleContentLayout.visibility = View.INVISIBLE
        }
        else{
            titleContentLayout.visibility = View.VISIBLE
        }

        for (user in users) {
            val login = user["username"] ?: "N/A"
            val password = user["password"] ?: "N/A"
            val firstName = user["name"] ?: "N/A"
            val lastName = user["surname"] ?: "N/A"


            // Горизонтальный контейнер для записи
            val horizontalLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(20, 8, 20, 8)
                }
//                background = "@drawable/rounded_field"
                setBackgroundResource(R.drawable.login_buttons)
                setPadding(60, 30, 60, 30)

                setOnClickListener {
                    LoginHelper.loginGetToken(this@WelcomeActivity, this@WelcomeActivity, login, password)
                }
            }

            // Текст с логином
            val textView = TextView(this).apply {

                text = "Пользователь: $lastName $firstName"
                textSize = 20f
                setTextColor(ContextCompat.getColor(context, R.color.white))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 35, 0, 8) }
                gravity = Gravity.CENTER_VERTICAL
            }

            // Кнопка удаления
            val deleteButton = ImageButton(this).apply {
                val drawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_close_clear_cancel)
                drawable?.setTint(ContextCompat.getColor(context, R.color.black))

                setImageDrawable(drawable) // Устанавливаем измененный drawable
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                setBackgroundResource(android.R.color.transparent)
                setOnClickListener {
                    // Удаление записи из базы
                    val rowsDeleted = dbHelper.deleteUser(login)
                    if (rowsDeleted > 0) {
                        Toast.makeText(this@WelcomeActivity, "Пользователь $login удален", Toast.LENGTH_SHORT).show()
                        recreate() // Обновление списка
                    } else {
                        Toast.makeText(this@WelcomeActivity, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Добавляем элементы в горизонтальный контейнер
            horizontalLayout.addView(textView)
            horizontalLayout.addView(deleteButton)

            // Добавляем горизонтальный контейнер в основной контейнер
            contentLayout.addView(horizontalLayout)
        }


        // Обработка кнопки "Войти" на верхней панели
        val btnLoginTop: Button = findViewById(R.id.btn_login_top)
        btnLoginTop.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
}

