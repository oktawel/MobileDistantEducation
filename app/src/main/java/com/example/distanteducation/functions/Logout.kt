package com.example.distanteducation.functions

import android.app.Activity
import android.content.Intent
import com.example.distanteducation.WelcomeActivity
import com.example.distanteducation.serverConection.UserSession

 object LogoutHelper {

    // Выполнение выхода из аккаунта
     fun performLogout(activity: Activity) {
        UserSession.clearSession() // Очистка сессии

        // Перенаправление на экран входа
        val intent = Intent(activity, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Очистка стека активностей
        activity.startActivity(intent)
        activity.finish()
    }
}