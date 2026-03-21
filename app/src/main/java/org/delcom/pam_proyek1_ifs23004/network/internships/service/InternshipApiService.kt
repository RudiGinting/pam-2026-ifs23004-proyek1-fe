package org.delcom.pam_proyek1_ifs23004.network.internships.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23004.network.data.ResponseMessage
import org.delcom.pam_proyek1_ifs23004.network.internships.data.*
import retrofit2.http.*

interface InternshipApiService {

    // ==========================================
    // AUTH ENDPOINTS
    // ==========================================

    @POST("auth/register")
    suspend fun postRegister(
        @Body request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?>

    @POST("auth/login")
    suspend fun postLogin(
        @Body request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?>

    @POST("auth/logout")
    suspend fun postLogout(
        @Body request: RequestAuthLogout
    ): ResponseMessage<String?>

    @POST("auth/refresh-token")
    suspend fun postRefreshToken(
        @Body request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?>

    // ==========================================
    // USERS ENDPOINTS
    // ==========================================

    @GET("users/me")
    suspend fun getUserMe(
        @Header("Authorization") authToken: String
    ): ResponseMessage<ResponseUser?>

    @PUT("users/me")
    suspend fun putUserMe(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChange
    ): ResponseMessage<String?>

    @PUT("users/me/password")
    suspend fun putUserMePassword(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChangePassword
    ): ResponseMessage<String?>

    @Multipart
    @PUT("users/me/photo")
    suspend fun putUserMePhoto(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    // ==========================================
    // INTERNSHIPS ENDPOINTS (Lowongan Magang)
    // ==========================================

    @GET("internships")
    suspend fun getInternships(
        @Header("Authorization") authToken: String,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 10,
        @Query("category") category: String? = null,
        @Query("location") location: String? = null
    ): ResponseMessage<ResponseInternships?>

    @GET("internships/{id}")
    suspend fun getInternshipById(
        @Header("Authorization") authToken: String,
        @Path("id") internshipId: String
    ): ResponseMessage<ResponseInternship?>

    @POST("internships")
    suspend fun postInternship(
        @Header("Authorization") authToken: String,
        @Body request: RequestInternship
    ): ResponseMessage<ResponseInternshipAdd?>

    @PUT("internships/{id}")
    suspend fun putInternship(
        @Header("Authorization") authToken: String,
        @Path("id") internshipId: String,
        @Body request: RequestInternship
    ): ResponseMessage<String?>

    @Multipart
    @PUT("internships/{id}/cover")
    suspend fun putInternshipCover(
        @Header("Authorization") authToken: String,
        @Path("id") internshipId: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    @DELETE("internships/{id}")
    suspend fun deleteInternship(
        @Header("Authorization") authToken: String,
        @Path("id") internshipId: String
    ): ResponseMessage<String?>

    // ==========================================
    // APPLICATIONS ENDPOINTS (Lamaran Magang)
    // ==========================================

    @GET("applications/my")
    suspend fun getMyApplications(
        @Header("Authorization") authToken: String,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 10
    ): ResponseMessage<ResponseApplications?>

    @GET("applications/internship/{internshipId}")
    suspend fun getApplicationsByInternship(
        @Header("Authorization") authToken: String,
        @Path("internshipId") internshipId: String
    ): ResponseMessage<ResponseApplications?>

    @POST("applications")
    suspend fun postApplication(
        @Header("Authorization") authToken: String,
        @Body request: RequestApplication
    ): ResponseMessage<ResponseApplicationAdd?>

    @PUT("applications/{id}/status")
    suspend fun putApplicationStatus(
        @Header("Authorization") authToken: String,
        @Path("id") applicationId: String,
        @Body request: RequestApplicationStatus
    ): ResponseMessage<String?>

    @Multipart
    @PUT("applications/{id}/cv")
    suspend fun putApplicationCV(
        @Header("Authorization") authToken: String,
        @Path("id") applicationId: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    @DELETE("applications/{id}")
    suspend fun deleteApplication(
        @Header("Authorization") authToken: String,
        @Path("id") applicationId: String
    ): ResponseMessage<String?>
}