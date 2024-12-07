package com.example.distanteducation.serverConection

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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
}
