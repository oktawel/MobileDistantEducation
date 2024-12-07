package com.example.distanteducation.DB

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "UserDatabase.db"
        const val DATABASE_VERSION = 2 // Увеличили версию для миграции
        const val TABLE_USERS = "Users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_FIRST_NAME = "name"
        const val COLUMN_LAST_NAME = "surname"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_FIRST_NAME TEXT NOT NULL,
                $COLUMN_LAST_NAME TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val addColumnsQuery1 = """
                ALTER TABLE $TABLE_USERS 
                ADD COLUMN $COLUMN_FIRST_NAME TEXT NOT NULL DEFAULT ''

            """.trimIndent()
            val addColumnsQuery2 = """

                ALTER TABLE $TABLE_USERS 
                ADD COLUMN $COLUMN_LAST_NAME TEXT NOT NULL DEFAULT '';
            """.trimIndent()
            db.execSQL(addColumnsQuery1)
            db.execSQL(addColumnsQuery2)
        }
    }

    fun insertUser(username: String, password: String, firstName: String, lastName: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_LAST_NAME, lastName)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun getAllUsers(): List<Map<String, String>> {
        val db = readableDatabase
        val cursor = db.query(TABLE_USERS, null, null, null, null, null, null)
        val users = mutableListOf<Map<String, String>>()

        with(cursor) {
            while (moveToNext()) {
                val user = mapOf(
                    COLUMN_USERNAME to getString(getColumnIndexOrThrow(COLUMN_USERNAME)),
                    COLUMN_PASSWORD to getString(getColumnIndexOrThrow(COLUMN_PASSWORD)),
                    COLUMN_FIRST_NAME to getString(getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                    COLUMN_LAST_NAME to getString(getColumnIndexOrThrow(COLUMN_LAST_NAME))
                )
                users.add(user)
            }
            close()
        }
        return users
    }

    fun deleteUser(username: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_USERS, "$COLUMN_USERNAME = ?", arrayOf(username))
    }

    fun getByUsername(username: String): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS, // Таблица
            arrayOf(COLUMN_ID, COLUMN_PASSWORD, COLUMN_FIRST_NAME, COLUMN_LAST_NAME), // Столбцы для выборки
            "$COLUMN_USERNAME = ?", // Условие WHERE
            arrayOf(username), // Значение для условия
            null, // Группировка
            null, // Условие HAVING
            null // Сортировка
        )

        var user: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            user = mapOf(
                COLUMN_ID to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString(),
                COLUMN_PASSWORD to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                COLUMN_FIRST_NAME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                COLUMN_LAST_NAME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME))
            )
        }
        cursor.close()
        return user
    }
}