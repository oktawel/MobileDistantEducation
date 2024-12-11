package com.example.distanteducation.functions

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.distanteducation.AdminActivity
import com.example.distanteducation.DB.UserDatabaseHelper
import com.example.distanteducation.ListCourses
import com.example.distanteducation.WelcomeActivity
import com.example.distanteducation.serverConection.Course
import com.example.distanteducation.serverConection.LoginRequest
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.UserResponse
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.launch
import retrofit2.Response

object LoginHelper {

    // Теперь передаем activity и используем lifecycleScope в Activity
    fun loginGetToken(lifecycleOwner: LifecycleOwner, activity: Activity, login: String, password: String) {
        val loginRequest = LoginRequest(login, password)
        val apiService = RetrofitClient.apiService

        // Запускаем корутину для выполнения асинхронных запросов
        lifecycleOwner.lifecycleScope.launch {
            try {
                // Отправляем запрос на сервер для получения токена
                val tokenResponse = apiService.login(loginRequest)

                if (tokenResponse.isSuccessful && tokenResponse.body() != null) {
                    val token = tokenResponse.body()?.token
                    token?.let {
                        UserSession.token = it
                        fetchUserData(activity, it, login, password) // Передаем activity в функцию
                    }
                } else {
                    Toast.makeText(activity, "Неверное имя пользователя или пароль", Toast.LENGTH_SHORT).show()
                }
                Log.d("WorkWithServer", tokenResponse.toString())
            } catch (e: Exception) {
                Toast.makeText(activity, "Ошибка подключения: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Функция для получения данных пользователя
    private suspend fun fetchUserData(activity: Activity, token: String, login: String, password: String) {
        try {
            val apiService = RetrofitClient.apiService
            val userResponse: Response<UserResponse> = apiService.getUserData("Bearer $token")

            if (userResponse.isSuccessful && userResponse.body() != null) {
                val user = userResponse.body()!!

                when (user.role) {
                    "Admin" -> {
                        saveInDatabase(activity, login, password, "Admin", "Admin")
                        user.name = "Admin"
                        user.surname = "Admin"
                        UserSession.user = user

                        val intent = Intent(activity, AdminActivity::class.java)
                        activity.startActivity(intent)
                    }
                    "Lector" -> {
                        saveInDatabase(activity, login, password, user.name!!, user.surname!!)
                        UserSession.user = user

                        val intent = Intent(activity, ListCourses::class.java)
                        activity.startActivity(intent)
                    }
                    "Student" -> {
                        saveInDatabase(activity, login, password, user.name!!, user.surname!!)
                        UserSession.user = user

                        val response = apiService.getStudentDetails(
                            token = "Bearer ${UserSession.token}",
                            studentId = user.id!!
                        )

                        UserSession.studentGroup = response.body()!!.group

                        val intent = Intent(activity, ListCourses::class.java)
                        activity.startActivity(intent)
                    }
                    else -> {
                        // Это выполняется, если ни один из вариантов не подходит
                        println("Что-то пошло не так")
                    }
                }

            } else {
                Toast.makeText(activity, "Не удалось получить данные пользователя", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Ошибка при получении данных пользователя: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveInDatabase(activity: Activity, login: String, password: String, name: String, surname: String) {
        val dbHelper = UserDatabaseHelper(activity)

        if (dbHelper.getByUsername(login) == null) {
            dbHelper.insertUser(login, password, name, surname)
        }
    }
}

