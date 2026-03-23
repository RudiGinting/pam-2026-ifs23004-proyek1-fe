package org.delcom.pam_proyek1_ifs23004.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23004.network.internships.data.*
import org.delcom.pam_proyek1_ifs23004.network.internships.service.IInternshipRepository
import javax.inject.Inject

// UI States
sealed interface ProfileUIState {
    data class Success(val data: ResponseUserData) : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    object Loading : ProfileUIState
}

sealed interface InternshipsUIState {
    data class Success(val data: List<ResponseInternshipData>) : InternshipsUIState
    data class Error(val message: String) : InternshipsUIState
    object Loading : InternshipsUIState
}

sealed interface InternshipUIState {
    data class Success(val data: ResponseInternshipData) : InternshipUIState
    data class Error(val message: String) : InternshipUIState
    object Loading : InternshipUIState
}

sealed interface ApplicationsUIState {
    data class Success(val data: List<ResponseApplicationData>) : ApplicationsUIState
    data class Error(val message: String) : ApplicationsUIState
    object Loading : ApplicationsUIState
}

sealed interface ApplicationUIState {
    data class Success(val data: ResponseApplicationData) : ApplicationUIState
    data class Error(val message: String) : ApplicationUIState
    object Loading : ApplicationUIState
}

sealed interface InternshipActionUIState {
    data class Success(val message: String) : InternshipActionUIState
    data class Error(val message: String) : InternshipActionUIState
    object Loading : InternshipActionUIState
}

data class UIStateInternship(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val internships: InternshipsUIState = InternshipsUIState.Loading,
    val internship: InternshipUIState = InternshipUIState.Loading,
    val myApplications: ApplicationsUIState = ApplicationsUIState.Loading,
    val application: ApplicationUIState = ApplicationUIState.Loading,
    var internshipAdd: InternshipActionUIState = InternshipActionUIState.Loading,
    var internshipChange: InternshipActionUIState = InternshipActionUIState.Loading,
    var internshipDelete: InternshipActionUIState = InternshipActionUIState.Loading,
    var internshipChangeCover: InternshipActionUIState = InternshipActionUIState.Loading,
    var applicationAdd: InternshipActionUIState = InternshipActionUIState.Loading,
    var applicationDelete: InternshipActionUIState = InternshipActionUIState.Loading,
    var profileChange: InternshipActionUIState = InternshipActionUIState.Loading,
    var profileChangePassword: InternshipActionUIState = InternshipActionUIState.Loading,
    var profileChangePhoto: InternshipActionUIState = InternshipActionUIState.Loading
)

@HiltViewModel
@Keep
class InternshipViewModel @Inject constructor(
    private val repository: IInternshipRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateInternship())
    val uiState = _uiState.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false
    private var currentCategory: String? = null
    private var currentLocation: String? = null
    private val currentInternshipsList = mutableListOf<ResponseInternshipData>()
    private var isFetching = false

    // ==========================================
    // Profile
    // ==========================================
    fun getProfile(authToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            val tmpState = runCatching {
                repository.getUserMe(authToken)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        ProfileUIState.Success(response.data!!.user)
                    } else {
                        ProfileUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    ProfileUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(profile = tmpState) }
        }
    }

    // ==========================================
    // Internships - PERBAIKAN: Hapus authToken dari parameter
    // ==========================================
    fun resetAndGetAllInternships(
        search: String? = null,
        category: String? = null,
        location: String? = null
    ) {
        isFetching = false
        currentPage = 1
        isLastPage = false
        currentCategory = category
        currentLocation = location
        currentInternshipsList.clear()
        getAllInternships(search, currentCategory, currentLocation)
    }

    fun getAllInternships(
        search: String? = null,
        category: String? = currentCategory,
        location: String? = currentLocation
    ) {
        if (isLastPage || isFetching) return

        isFetching = true

        if (currentPage == 1) {
            _uiState.update { it.copy(internships = InternshipsUIState.Loading) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val tmpState = runCatching {
                repository.getInternships(search, currentPage, 10, category, location)
            }.fold(
                onSuccess = { response ->
                    isFetching = false
                    if (response.status == "success") {
                        val newInternships = response.data?.internships ?: emptyList()
                        if (newInternships.size < 10) isLastPage = true

                        val uniqueInternships = newInternships.filter { newItem ->
                            currentInternshipsList.none { existing -> existing.id == newItem.id }
                        }

                        currentInternshipsList.addAll(uniqueInternships)
                        currentPage++
                        InternshipsUIState.Success(currentInternshipsList.toList())
                    } else {
                        InternshipsUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    isFetching = false
                    InternshipsUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { state -> state.copy(internships = tmpState) }
        }
    }

    fun getInternshipById(internshipId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(internship = InternshipUIState.Loading) }
            val tmpState = runCatching {
                repository.getInternshipById(internshipId)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipUIState.Success(response.data!!.internship)
                    } else {
                        InternshipUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(internship = tmpState) }
        }
    }

    fun postInternship(
        authToken: String,
        companyName: String,
        companyEmail: String,
        title: String,
        description: String,
        category: String,
        location: String,
        duration: String,
        requirement: String,
        benefit: String?,
        deadline: String,
        submissionDate: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(internshipAdd = InternshipActionUIState.Loading) }
            val tmpState = runCatching {
                repository.postInternship(
                    authToken = authToken,
                    RequestInternship(
                        companyName = companyName,
                        companyEmail = companyEmail,
                        title = title,
                        description = description,
                        category = category,
                        location = location,
                        duration = duration,
                        requirement = requirement,
                        benefit = benefit,
                        deadline = deadline,
                        submissionDate = submissionDate
                    )
                )
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipActionUIState.Success(response.message)
                    } else {
                        InternshipActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(internshipAdd = tmpState) }
        }
    }

    fun putInternship(
        authToken: String,
        internshipId: String,
        companyName: String,
        companyEmail: String,
        title: String,
        description: String,
        category: String,
        location: String,
        duration: String,
        requirement: String,
        benefit: String?,
        deadline: String,
        submissionDate: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(internshipChange = InternshipActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putInternship(
                    authToken = authToken,
                    internshipId = internshipId,
                    RequestInternship(
                        companyName = companyName,
                        companyEmail = companyEmail,
                        title = title,
                        description = description,
                        category = category,
                        location = location,
                        duration = duration,
                        requirement = requirement,
                        benefit = benefit,
                        deadline = deadline,
                        submissionDate = submissionDate
                    )
                )
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipActionUIState.Success(response.message)
                    } else {
                        InternshipActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(internshipChange = tmpState) }
        }
    }

    fun putInternshipCover(authToken: String, internshipId: String, file: MultipartBody.Part) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(internshipChangeCover = InternshipActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putInternshipCover(authToken, internshipId, file)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipActionUIState.Success(response.message)
                    } else {
                        InternshipActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(internshipChangeCover = tmpState) }
        }
    }

    fun deleteInternship(authToken: String, internshipId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(internshipDelete = InternshipActionUIState.Loading) }
            val tmpState = runCatching {
                repository.deleteInternship(authToken, internshipId)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipActionUIState.Success(response.message)
                    } else {
                        InternshipActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(internshipDelete = tmpState) }
        }
    }

    // ==========================================
    // Applications
    // ==========================================
    fun getMyApplications(authToken: String, page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(myApplications = ApplicationsUIState.Loading) }
            val tmpState = runCatching {
                repository.getMyApplications(authToken, page, perPage)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        ApplicationsUIState.Success(response.data?.applications ?: emptyList())
                    } else {
                        ApplicationsUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    ApplicationsUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(myApplications = tmpState) }
        }
    }

    fun postApplication(authToken: String, internshipId: String, motivation: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(applicationAdd = InternshipActionUIState.Loading) }
            val tmpState = runCatching {
                repository.postApplication(authToken, RequestApplication(internshipId, motivation))
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipActionUIState.Success(response.message)
                    } else {
                        InternshipActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(applicationAdd = tmpState) }
        }
    }

    fun deleteApplication(authToken: String, applicationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(applicationDelete = InternshipActionUIState.Loading) }
            val tmpState = runCatching {
                repository.deleteApplication(authToken, applicationId)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipActionUIState.Success(response.message)
                    } else {
                        InternshipActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(applicationDelete = tmpState) }
        }
    }

    fun putApplicationCV(authToken: String, applicationId: String, file: MultipartBody.Part) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(profileChangePhoto = InternshipActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putApplicationCV(authToken, applicationId, file)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipActionUIState.Success(response.message)
                    } else {
                        InternshipActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(profileChangePhoto = tmpState) }
        }
    }

    // ==========================================
    // User Profile Updates
    // ==========================================
    fun putUserMe(authToken: String, name: String, username: String, about: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(profileChange = InternshipActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putUserMe(authToken, RequestUserChange(name, username, about))
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipActionUIState.Success(response.message)
                    } else {
                        InternshipActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(profileChange = tmpState) }
        }
    }

    fun putUserMePassword(authToken: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(profileChangePassword = InternshipActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putUserMePassword(
                    authToken,
                    RequestUserChangePassword(newPassword = newPassword, password = oldPassword)
                )
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipActionUIState.Success(response.message)
                    } else {
                        InternshipActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(profileChangePassword = tmpState) }
        }
    }

    fun putUserMePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(profileChangePhoto = InternshipActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putUserMePhoto(authToken, file)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        InternshipActionUIState.Success(response.message)
                    } else {
                        InternshipActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    InternshipActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(profileChangePhoto = tmpState) }
        }
    }
}