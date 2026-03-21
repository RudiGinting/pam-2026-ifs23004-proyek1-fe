package org.delcom.pam_proyek1_ifs23004.network.internships.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23004.network.data.ResponseMessage
import org.delcom.pam_proyek1_ifs23004.network.internships.data.*

interface IInternshipRepository {
    // ==========================================
    // Auth
    // ==========================================
    suspend fun postRegister(request: RequestAuthRegister): ResponseMessage<ResponseAuthRegister?>
    suspend fun postLogin(request: RequestAuthLogin): ResponseMessage<ResponseAuthLogin?>
    suspend fun postLogout(request: RequestAuthLogout): ResponseMessage<String?>
    suspend fun postRefreshToken(request: RequestAuthRefreshToken): ResponseMessage<ResponseAuthLogin?>

    // ==========================================
    // Users
    // ==========================================
    suspend fun getUserMe(authToken: String): ResponseMessage<ResponseUser?>
    suspend fun putUserMe(authToken: String, request: RequestUserChange): ResponseMessage<String?>
    suspend fun putUserMePassword(authToken: String, request: RequestUserChangePassword): ResponseMessage<String?>
    suspend fun putUserMePhoto(authToken: String, file: MultipartBody.Part): ResponseMessage<String?>

    // ==========================================
    // Internships
    // ==========================================
    suspend fun getInternships(
        authToken: String,
        search: String? = null,
        page: Int = 1,
        perPage: Int = 10,
        category: String? = null,
        location: String? = null
    ): ResponseMessage<ResponseInternships?>

    suspend fun getInternshipById(
        authToken: String,
        internshipId: String
    ): ResponseMessage<ResponseInternship?>

    suspend fun postInternship(
        authToken: String,
        request: RequestInternship
    ): ResponseMessage<ResponseInternshipAdd?>

    suspend fun putInternship(
        authToken: String,
        internshipId: String,
        request: RequestInternship
    ): ResponseMessage<String?>

    suspend fun putInternshipCover(
        authToken: String,
        internshipId: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?>

    suspend fun deleteInternship(
        authToken: String,
        internshipId: String
    ): ResponseMessage<String?>

    // ==========================================
    // Applications
    // ==========================================
    suspend fun getMyApplications(
        authToken: String,
        page: Int = 1,
        perPage: Int = 10
    ): ResponseMessage<ResponseApplications?>

    suspend fun getApplicationsByInternship(
        authToken: String,
        internshipId: String
    ): ResponseMessage<ResponseApplications?>

    suspend fun postApplication(
        authToken: String,
        request: RequestApplication
    ): ResponseMessage<ResponseApplicationAdd?>

    suspend fun putApplicationStatus(
        authToken: String,
        applicationId: String,
        status: String
    ): ResponseMessage<String?>

    suspend fun putApplicationCV(
        authToken: String,
        applicationId: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?>

    suspend fun deleteApplication(
        authToken: String,
        applicationId: String
    ): ResponseMessage<String?>
}