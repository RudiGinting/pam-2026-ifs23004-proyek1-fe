package org.delcom.pam_proyek1_ifs23004.network.internships.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestInternship(
    val companyName: String,
    val companyEmail: String,
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val duration: String,
    val requirement: String,
    val benefit: String? = null,
    val deadline: String,
    val submissionDate: String
)