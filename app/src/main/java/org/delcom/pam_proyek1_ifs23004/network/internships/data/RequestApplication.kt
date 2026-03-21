package org.delcom.pam_proyek1_ifs23004.network.internships.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestApplication(
    val internshipId: String,
    val motivation: String
)

@Serializable
data class RequestApplicationStatus(
    val status: String
)