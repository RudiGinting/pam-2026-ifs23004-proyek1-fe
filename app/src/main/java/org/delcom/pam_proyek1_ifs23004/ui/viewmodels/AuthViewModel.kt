package org.delcom.pam_proyek1_ifs23004.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.delcom.pam_proyek1_ifs23004.network.internships.data.RequestAuthLogin
import org.delcom.pam_proyek1_ifs23004.network.internships.data.RequestAuthLogout
import org.delcom.pam_proyek1_ifs23004.network.internships.data.RequestAuthRefreshToken
import org.delcom.pam_proyek1_ifs23004.network.internships.data.RequestAuthRegister
import org.delcom.pam_proyek1_ifs23004.network.internships.data.ResponseAuthLogin
import org.delcom.pam_proyek1_ifs23004.network.internships.service.IInternshipRepository  // PERBAIKAN: Ganti ITechnicianRepository menjadi IInternshipRepository
import org.delcom.pam_proyek1_ifs23004.prefs.AuthTokenPref
import javax.inject.Inject

sealed interface AuthUIState {
    data class Success(val data: ResponseAuthLogin) : AuthUIState
    data class Error(val message: String) : AuthUIState
    object Loading : AuthUIState
}

sealed interface AuthActionUIState {
    data class Success(val message: String) : AuthActionUIState
    data class Error(val message: String) : AuthActionUIState
    object Loading : AuthActionUIState
}

sealed interface AuthLogoutUIState {
    data class Success(val message: String) : AuthLogoutUIState
    data class Error(val message: String) : AuthLogoutUIState
    object Loading : AuthLogoutUIState
}

data class UIStateAuth(
    val auth: AuthUIState = AuthUIState.Loading,
    var authRegister: AuthActionUIState = AuthActionUIState.Loading,
    var authLogout: AuthLogoutUIState = AuthLogoutUIState.Loading,
    var authRefreshToken: AuthActionUIState = AuthActionUIState.Loading,
)

@HiltViewModel
@Keep
class AuthViewModel @Inject constructor(
    private val repository: IInternshipRepository,  // PERBAIKAN: Ganti ITechnicianRepository menjadi IInternshipRepository
    private val authTokenPref: AuthTokenPref
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateAuth())
    val uiState = _uiState.asStateFlow()

    init {
        loadTokenFromPreferences()
    }

    fun register(
        name: String,
        username: String,
        password: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(authRegister = AuthActionUIState.Loading) }
            val tmpState = runCatching {
                repository.postRegister(
                    RequestAuthRegister(name = name, username = username, password = password)
                )
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        AuthActionUIState.Success(response.data!!.userId)
                    } else {
                        AuthActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    AuthActionUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(authRegister = tmpState) }
        }
    }

    fun login(
        username: String,
        password: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(auth = AuthUIState.Loading) }

            val result = runCatching {
                repository.postLogin(RequestAuthLogin(username = username, password = password))
            }

            val tmpState = result.fold(
                onSuccess = { response ->
                    if (response.status == "success" && response.data != null) {
                        authTokenPref.saveTokens(response.data.authToken, response.data.refreshToken)
                        AuthUIState.Success(response.data)
                    } else {
                        AuthUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    AuthUIState.Error(error.message ?: "Unknown error")
                }
            )

            _uiState.update { it.copy(auth = tmpState) }
        }
    }

    fun logout(authToken: String) {
        viewModelScope.launch {
            authTokenPref.clearTokens()

            _uiState.update { it.copy(authLogout = AuthLogoutUIState.Loading) }

            val tmpState = runCatching {
                repository.postLogout(RequestAuthLogout(authToken = authToken))
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        AuthLogoutUIState.Success(response.message)
                    } else {
                        AuthLogoutUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    AuthLogoutUIState.Error(error.message ?: "Unknown error")
                }
            )

            _uiState.update { it.copy(authLogout = tmpState) }
        }
    }

    fun refreshToken(
        authToken: String,
        refreshToken: String,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    auth = AuthUIState.Loading,
                    authRefreshToken = AuthActionUIState.Loading
                )
            }

            var tmpStateAuth: AuthUIState = AuthUIState.Loading
            var tmpStateAuthRefreshToken: AuthActionUIState = AuthActionUIState.Loading

            runCatching {
                repository.postRefreshToken(
                    RequestAuthRefreshToken(authToken = authToken, refreshToken = refreshToken)
                )
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success" && response.data != null) {
                        authTokenPref.saveTokens(response.data.authToken, response.data.refreshToken)
                        tmpStateAuth = AuthUIState.Success(response.data)
                        tmpStateAuthRefreshToken = AuthActionUIState.Success(response.message)
                    } else {
                        tmpStateAuth = AuthUIState.Error(response.message)
                        tmpStateAuthRefreshToken = AuthActionUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    tmpStateAuth = AuthUIState.Error(error.message ?: "Unknown error")
                    tmpStateAuthRefreshToken = AuthActionUIState.Error(error.message ?: "Unknown error")
                }
            )

            _uiState.update {
                it.copy(
                    auth = tmpStateAuth,
                    authRefreshToken = tmpStateAuthRefreshToken
                )
            }
        }
    }

    fun loadTokenFromPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(auth = AuthUIState.Loading) }

            combine(
                authTokenPref.getAuthToken(),
                authTokenPref.getRefreshToken()
            ) { authToken, refreshToken ->
                if (authToken.isNullOrEmpty() || refreshToken.isNullOrEmpty()) {
                    AuthUIState.Error("Token tidak tersedia")
                } else {
                    AuthUIState.Success(
                        ResponseAuthLogin(
                            authToken = authToken,
                            refreshToken = refreshToken
                        )
                    )
                }
            }.collect { authState ->
                _uiState.update { it.copy(auth = authState) }
            }
        }
    }
}