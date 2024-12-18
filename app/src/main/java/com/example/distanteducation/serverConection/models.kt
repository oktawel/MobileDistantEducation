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
@Parcelize
data class Lecturer(
    val id: Long,
    val name: String,
    val surname: String,
    val userLogin: String,
    val userPassword: String
): Parcelable
@Parcelize
data class Student(
    val id: Long,
    val name: String,
    val surname: String,
    val group: String,
    val birthDate: String,
    val userLogin: String,
    val userPassword: String
): Parcelable
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

@Parcelize
data class Test (
    val id: Long,
    val name: String,
    val description: String,
    val open: Boolean,
    val mark: Float,
    val subjectId: Long
): Parcelable

data class TestRequest(
    val name: String,
    val description: String,
    val open: Boolean,
    val subjectId: Long,
    val addFormQuestionList: List<QuestionRequest>
)

data class TestRequestUpdate(
    val id: Long,
    val name: String,
    val description: String,
    val open: Boolean,
    val subjectId: Long
)

@Parcelize
data class QuestionRequest(
    val text: String,
    val typeId: Long,
    val cost: Int,
    val addFormOptionList: List<OptionRequest>
): Parcelable
@Parcelize
data class OptionRequest(
    val text: String,
    val correct: Boolean?
): Parcelable

data class QuestionType(val id: Int, val name: String) {
    override fun toString(): String {
        return name
    }
}
data class TestExecute(
    val id: Long,
    val name: String,
    val description: String,
    val open: Boolean,
    val questions: List<QuestionExecute>,
    val subjectId: Long
)

data class QuestionExecute(
    val id: Long,
    val text: String,
    val testId: Long,
    val typeQuestionID: Long,
    val cost: Int,
    val options: List<OptionExecute>?
)

data class OptionExecute(
    val id: Long,
    val questionId: Long,
    val text: String,
    val correct: Boolean
)

data class TestAnswer(
    val testId: Long,
    val studentId: Long,
    val answerQuestions: List<QuestionAnswer>,
)

data class QuestionAnswer(
    val questionId: Long,
    val answerOptions: List<OptionAnswer>
)

data class OptionAnswer(
    val optionId: Long?,
    val textAnswer: String?,
)

data class MarkStudent(
    val id: Long?,
    val name: String?,
    val surname: String?,
    val group: String?,
    val mark: Float,
)