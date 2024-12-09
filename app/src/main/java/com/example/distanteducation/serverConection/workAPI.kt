package com.example.distanteducation.serverConection

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("backend-1.0-SNAPSHOT/auth/authenticate")
    suspend fun login(@Body loginRequest: LoginRequest): Response<TokenResponse>

    @GET("backend-1.0-SNAPSHOT/auth/user")
    suspend fun getUserData(@Header("Authorization") token: String): Response<UserResponse>

    @GET("backend-1.0-SNAPSHOT/admin/lecturers")
    suspend fun getLecturers(@Header("Authorization") token: String): Response<List<Lecturer>>

    @GET("backend-1.0-SNAPSHOT/admin/students")
    suspend fun getStudents(@Header("Authorization") token: String): Response<List<Student>>

    @GET("backend-1.0-SNAPSHOT/admin/groups")
    suspend fun getGroups(@Header("Authorization") token: String): Response<List<Group>>

    @GET("backend-1.0-SNAPSHOT/admin/student/{id}")
    suspend fun getStudentDetails(
        @Header("Authorization") token: String,
        @Path("id") studentId: Long
    ): Response<Student>

    @GET("backend-1.0-SNAPSHOT/admin/lecturer/{id}")
    suspend fun getLecturerDetails(
        @Header("Authorization") token: String,
        @Path("id") lecurertId: Long
    ): Response<Lecturer>

    @GET("backend-1.0-SNAPSHOT/admin/group/{id}")
    suspend fun getGroupDetails(
        @Header("Authorization") token: String,
        @Path("id") grouptId: Long
    ): Response<Group>

    @POST("backend-1.0-SNAPSHOT/admin/student/add")
    suspend fun addStudent(
        @Header("Authorization") token: String,
        @Body studentRequest: StudentRequest
    ): Response<Void>

    @POST("backend-1.0-SNAPSHOT/admin/lecturer/add")
    suspend fun addLecturer(
        @Header("Authorization") token: String,
        @Body lecturerRequest: LecturerRequest
    ): Response<Void>

    @POST("backend-1.0-SNAPSHOT/admin/group/add")
    suspend fun addGroup(
        @Header("Authorization") token: String,
        @Body groupRequest: GroupRequest
    ): Response<Void>


    @DELETE("backend-1.0-SNAPSHOT/admin/student/delete/{id}")
    suspend fun deleteStudent(
        @Header("Authorization") token: String,
        @Path("id") studentId: Long
    ): Response<Unit>

    @DELETE("backend-1.0-SNAPSHOT/admin/lecturer/delete/{id}")
    suspend fun deleteLecturer(
        @Header("Authorization") token: String,
        @Path("id") lecturerId: Long
    ): Response<Unit>

    @DELETE("backend-1.0-SNAPSHOT/admin/group/delete/{id}")
    suspend fun deleteGroup(
        @Header("Authorization") token: String,
        @Path("id") groupId: Long
    ): Response<Unit>

    @POST("backend-1.0-SNAPSHOT/admin/student/update")
    suspend fun updateStudent(
        @Header("Authorization") token: String,
        @Body studentRequest: StudentRequestUpdate
    ): Response<Unit>

    @POST("backend-1.0-SNAPSHOT/admin/lecturer/update")
    suspend fun updateLecturer(
        @Header("Authorization") token: String,
        @Body lecturerRequest: LecturerRequestUpdate
    ): Response<Unit>

    @POST("backend-1.0-SNAPSHOT/admin/group/update")
    suspend fun updateGroup(
        @Header("Authorization") token: String,
        @Body groupRequest: GroupRequestUpdate
    ): Response<Unit>

}
