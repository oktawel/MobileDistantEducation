package com.example.distanteducation.serverConection

import com.example.distanteducation.serverConection.UserResponse

object UserSession {
    var token: String? = null
    var user: UserResponse? = null
    var studentGroup: String? = null

    // Проверка, вошел ли пользователь
    fun isLoggedIn(): Boolean {
        return token != null && user != null
    }
    // Очистка сессии
    fun clearSession() {
        token = null
        user = null
        studentGroup = null
    }
}

