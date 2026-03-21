package org.delcom.pam_proyek1_ifs23004.network.internships.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestUserChange (
    val name: String,
    val username: String,
    val about: String? = null
)

@Serializable
data class RequestUserChangePassword (
    val newPassword: String,
    val password: String
)