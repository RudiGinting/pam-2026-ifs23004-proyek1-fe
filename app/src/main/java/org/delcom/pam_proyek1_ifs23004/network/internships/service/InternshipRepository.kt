package org.delcom.pam_proyek1_ifs23004.network.internships.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23004.helper.SuspendHelper
import org.delcom.pam_proyek1_ifs23004.network.data.ResponseMessage
import org.delcom.pam_proyek1_ifs23004.network.internships.data.*

class InternshipRepository(
    private val apiService: InternshipApiService
) : IInternshipRepository {

    // ==========================================
    // Auth
    // ==========================================
    override suspend fun postRegister(request: RequestAuthRegister): ResponseMessage<ResponseAuthRegister?> =
        SuspendHelper.safeApiCall { apiService.postRegister(request) }

    override suspend fun postLogin(request: RequestAuthLogin): ResponseMessage<ResponseAuthLogin?> =
        SuspendHelper.safeApiCall { apiService.postLogin(request) }

    override suspend fun postLogout(request: RequestAuthLogout): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.postLogout(request) }

    override suspend fun postRefreshToken(request: RequestAuthRefreshToken): ResponseMessage<ResponseAuthLogin?> =
        SuspendHelper.safeApiCall { apiService.postRefreshToken(request) }

    // ==========================================
    // Users
    // ==========================================
    override suspend fun getUserMe(authToken: String): ResponseMessage<ResponseUser?> =
        SuspendHelper.safeApiCall { apiService.getUserMe("Bearer $authToken") }

    override suspend fun putUserMe(authToken: String, request: RequestUserChange): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putUserMe("Bearer $authToken", request) }

    override suspend fun putUserMePassword(authToken: String, request: RequestUserChangePassword): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putUserMePassword("Bearer $authToken", request) }

    override suspend fun putUserMePhoto(authToken: String, file: MultipartBody.Part): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putUserMePhoto("Bearer $authToken", file) }

    // ==========================================
    // Internships
    // ==========================================
    override suspend fun getInternships(
        authToken: String,
        search: String?,
        page: Int,
        perPage: Int,
        category: String?,
        location: String?
    ): ResponseMessage<ResponseInternships?> =
        SuspendHelper.safeApiCall {
            apiService.getInternships("Bearer $authToken", search, page, perPage, category, location)
        }

    override suspend fun getInternshipById(authToken: String, internshipId: String): ResponseMessage<ResponseInternship?> =
        SuspendHelper.safeApiCall { apiService.getInternshipById("Bearer $authToken", internshipId) }

    override suspend fun postInternship(authToken: String, request: RequestInternship): ResponseMessage<ResponseInternshipAdd?> =
        SuspendHelper.safeApiCall { apiService.postInternship("Bearer $authToken", request) }

    override suspend fun putInternship(authToken: String, internshipId: String, request: RequestInternship): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putInternship("Bearer $authToken", internshipId, request) }

    override suspend fun putInternshipCover(authToken: String, internshipId: String, file: MultipartBody.Part): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putInternshipCover("Bearer $authToken", internshipId, file) }

    override suspend fun deleteInternship(authToken: String, internshipId: String): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.deleteInternship("Bearer $authToken", internshipId) }

    // ==========================================
    // Applications
    // ==========================================
    override suspend fun getMyApplications(authToken: String, page: Int, perPage: Int): ResponseMessage<ResponseApplications?> =
        SuspendHelper.safeApiCall { apiService.getMyApplications("Bearer $authToken", page, perPage) }

    override suspend fun getApplicationsByInternship(authToken: String, internshipId: String): ResponseMessage<ResponseApplications?> =
        SuspendHelper.safeApiCall { apiService.getApplicationsByInternship("Bearer $authToken", internshipId) }

    override suspend fun postApplication(authToken: String, request: RequestApplication): ResponseMessage<ResponseApplicationAdd?> =
        SuspendHelper.safeApiCall { apiService.postApplication("Bearer $authToken", request) }

    override suspend fun putApplicationStatus(authToken: String, applicationId: String, status: String): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putApplicationStatus("Bearer $authToken", applicationId, RequestApplicationStatus(status)) }

    override suspend fun putApplicationCV(authToken: String, applicationId: String, file: MultipartBody.Part): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putApplicationCV("Bearer $authToken", applicationId, file) }

    override suspend fun deleteApplication(authToken: String, applicationId: String): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.deleteApplication("Bearer $authToken", applicationId) }
}