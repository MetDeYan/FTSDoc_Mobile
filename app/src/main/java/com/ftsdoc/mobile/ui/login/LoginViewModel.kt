package com.ftsdoc.mobile.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ftsdoc.mobile.domain.ServerEndpoints
import com.ftsdoc.mobile.domain.model.DEFAULT_SERVER_URL
import com.ftsdoc.mobile.domain.repository.AuthRepository
import com.ftsdoc.mobile.domain.repository.SettingsRepository
import com.ftsdoc.mobile.domain.validation.LoginValidator
import com.ftsdoc.mobile.ui.common.toUiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val serverUrl: String = DEFAULT_SERVER_URL,
    val useStubApi: Boolean = true,
    val usernameError: Boolean = false,
    val passwordError: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    private var serverEdited = false

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                if (!serverEdited) {
                    _state.update { it.copy(serverUrl = settings.serverUrl) }
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.useStubApi.collect { enabled ->
                _state.update { it.copy(useStubApi = enabled) }
            }
        }
    }

    fun onUsernameChange(value: String) {
        _state.update { it.copy(username = value, usernameError = false, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, passwordError = false, error = null) }
    }

    fun onServerUrlChange(value: String) {
        serverEdited = true
        _state.update { it.copy(serverUrl = value, error = null) }
    }

    fun onStubChanged(enabled: Boolean) {
        _state.update { it.copy(useStubApi = enabled) }
        viewModelScope.launch {
            settingsRepository.setUseStubApi(enabled)
        }
    }

    fun login() {
        val snapshot = _state.value
        if (snapshot.isLoading) return
        val errors = LoginValidator.validate(snapshot.username, snapshot.password)
        if (errors.hasAny) {
            _state.update {
                it.copy(usernameError = errors.username, passwordError = errors.password, error = null)
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val serverUrl = _state.value.serverUrl.trim()
            if (!_state.value.useStubApi && !ServerEndpoints.isValidBase(serverUrl)) {
                _state.update {
                    it.copy(isLoading = false, error = "Укажите корректный адрес сервера")
                }
                return@launch
            }
            if (ServerEndpoints.isValidBase(serverUrl)) {
                settingsRepository.updateServerUrl(serverUrl)
            }
            authRepository.login(_state.value.username, _state.value.password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.emit(Unit)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                }
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(authRepository, settingsRepository) as T
        }
    }
}
