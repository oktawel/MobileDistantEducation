package com.example.distanteducation.serverConection

data class LoginRequest(val username: String, val password: String)

data class TokenResponse(val token: String)

data class UserResponse(
    val userId: Long,
    val login: String,
    val role: String,
    val id: Long?,
    var name: String?,
    var surname: String?
)

data class Lecturer(
    val id: Long,
    val name: String,
    val surname: String,
    val userLogin: String,
    val userPassword: String
)

data class Student(
    val id: Long,
    val name: String,
    val surname: String,
    val group: String,
    val birthDate: String,
    val userLogin: String,
    val userPassword: String
)

data class Group(
    val id: Long,
    val name: String,
)

data class StudentRequest(
    val name: String,
    val surname: String,
    val birthDate: String,
    val groupId: Long
)
data class LecturerRequest(
    val name: String,
    val surname: String,
)
data class GroupRequest(
    val name: String,
)