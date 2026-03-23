package org.delcom.pam_proyek1_ifs23004.network.internships.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseInternships(
    val internships: List<ResponseInternshipData>
)

@Serializable
data class ResponseInternship(
    val internship: ResponseInternshipData
)

@Serializable
data class ResponseInternshipData(
    val id: String = "",
    val companyName: String = "",
    val companyEmail: String = "",
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val duration: String,
    val requirement: String,
    val benefit: String? = null,
    val deadline: String,
    val status: String = "Open",
    val applicantsCount: Int = 0,
    val submissionDate: String = "",
    val cover: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class ResponseInternshipAdd(
    val internshipId: String
)