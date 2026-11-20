package com.example.studenttaskmanager.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studenttaskmanager.data.prefs.SettingsRepository
import com.example.studenttaskmanager.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val username: String = "",
    val fullName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val usernameError: String? = null,
    val fullNameError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val generalError: String? = null,
    val loading: Boolean = false,
    val successUserId: Long? = null
)

/**
 * Login and registration share this ViewModel. A successful call stores the
 * session and emits [AuthUiState.successUserId] so the screen can navigate.
 */
class AuthViewModel(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun onUsernameChange(value: String) = _uiState.update {
        it.copy(username = value, usernameError = null, generalError = null)
    }

    fun onFullNameChange(value: String) = _uiState.update {
        it.copy(fullName = value, fullNameError = null, generalError = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, generalError = null)
    }

    fun onConfirmPasswordChange(value: String) = _uiState.update {
        it.copy(confirmPassword = value, confirmError = null, generalError = null)
    }

    fun login() {
        val state = _uiState.value
        val usernameError = if (state.username.isBlank()) "Enter your username." else null
        val passwordError = if (state.password.isBlank()) "Enter your password." else null
        if (usernameError != null || passwordError != null) {
            _uiState.update { it.copy(usernameError = usernameError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, generalError = null) }
            userRepository.login(state.username, state.password)
                .onSuccess { user ->
                    settingsRepository.setSessionUser(user.id)
                    _uiState.update { it.copy(loading = false, successUserId = user.id, password = "") }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            generalError = error.message ?: "Could not sign in."
                        )
                    }
                }
        }
    }

    fun register() {
        val state = _uiState.value
        val usernameError = when {
            state.username.isBlank() -> "Choose a username."
            state.username.trim().length < 3 -> "At least 3 characters."
            else -> null
        }
        val fullNameError = if (state.fullName.isBlank()) "Enter your full name." else null
        val passwordError = when {
            state.password.isBlank() -> "Choose a password."
            state.password.length < 6 -> "At least 6 characters."
            else -> null
        }
        val confirmError = if (state.confirmPassword != state.password) "Passwords do not match." else null

        if (listOf(usernameError, fullNameError, passwordError, confirmError).any { it != null }) {
            _uiState.update {
                it.copy(
                    usernameError = usernameError,
                    fullNameError = fullNameError,
                    passwordError = passwordError,
                    confirmError = confirmError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, generalError = null) }
            userRepository.register(state.username, state.fullName, state.password)
                .onSuccess { user ->
                    settingsRepository.setOnboardingDone(true)
                    settingsRepository.setSessionUser(user.id)
                    _uiState.update { it.copy(loading = false, successUserId = user.id, password = "", confirmPassword = "") }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(loading = false, generalError = error.message ?: "Could not create the account.")
                    }
                }
        }
    }

    fun consumeSuccess() = _uiState.update { it.copy(successUserId = null) }

    fun reset() {
        _uiState.value = AuthUiState()
    }
}
