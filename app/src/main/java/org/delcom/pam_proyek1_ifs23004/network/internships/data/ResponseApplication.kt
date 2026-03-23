package org.delcom.pam_proyek1_ifs23004.network.internships.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseApplications(
    val applications: List<ResponseApplicationData>
)

@Serializable
data class ResponseApplication(
    val application: ResponseApplicationData
)

@Serializable
data class ResponseApplicationData(
    val id: String = "",
    val internshipId: String,
    val studentId: String,
    val motivation: String,
    val cvUrl: String? = null,
    val status: String,
    val appliedAt: String,
    val updatedAt: String,
    val internshipTitle: String = "",
    val companyName: String = ""
)

@Serializable
data class ResponseApplicationAdd(
    val applicationId: String
)