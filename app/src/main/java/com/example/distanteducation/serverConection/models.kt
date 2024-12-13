package com.example.distanteducation.serverConection

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
@Parcelize
data class Group(
    val id: Long,
    val name: String,
) : Parcelable

data class GroupWithCourses(
    val id: Long,
    val name: String,
    val listCourses: Course
)

data class StudentRequest(
    val name: String,
    val surname: String,
    val birthDate: String,
    val groupId: Long
)
data class LecturerRequest(
    val name: String,
    val surname: String
)
data class GroupRequest(
    val name: String
)

data class StudentRequestUpdate(
    val id: Long,
    val login: String,
    val password: String,
    val name: String,
    val surname: String,
    val birthDate: String,
    val groupId: Long
)
data class LecturerRequestUpdate(
    val id: Long,
    val login: String,
    val password: String,
    val name: String,
    val surname: String
)
data class GroupRequestUpdate(
    val id: Long,
    val name: String
)

@Parcelize
data class Course(
    val id: Long,
    val name: String,
    val description: String,
    val lecturerName: String,
    val lecturerSurname: String,
    val groups: List<Group>
) : Parcelable

data class CourseRequest(
    val name: String,
    val description: String,
    val lecturerId: Long
)
data class CourseRequestUpdate(
    val id: Long,
    val name: String,
    val description: String,
    val lecturerId: Long
)

data class Test (
    val id: Long,
    val name: String,
    val description: String,
    val open: Boolean,
    val mark: Float,
    val subjectId: Long
)