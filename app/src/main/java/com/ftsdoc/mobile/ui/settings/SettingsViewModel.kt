package com.ftsdoc.mobile.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ftsdoc.mobile.domain.model.OrganizationSettings
import com.ftsdoc.mobile.domain.repository.AuthRepository
import com.ftsdoc.mobile.domain.repository.SettingsRepository
import com.ftsdoc.mobile.domain.validation.SettingsValidator
import com.ftsdoc.mobile.ui.common.toUiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val initialized: Boolean = false,
    val name: String = "",
    val organizationId: String = "",
    val userId: String = "",
    val serverUrl: String = "",
    val useStubApi: Boolean = true,
    val nameError: Boolean = false,
    val idError: Boolean = false,
    val userIdError: Boolean = false,
    val urlError: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
    val messageIsError: Boolean = false,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            _state.update {
                it.copy(
                    initialized = true,
                    name = settings.name,
                    organizationId = settings.organizationId,
                    userId = settings.userId,
                    serverUrl = settings.serverUrl,
                )
            }
        }
        viewModelScope.launch {
            settingsRepository.useStubApi.collect { enabled ->
                _state.update { it.copy(useStubApi = enabled) }
            }
        }
    }

    fun onNameChange(value: String) = clearMessage { it.copy(name = value, nameError = false) }

    fun onIdChange(value: String) = clearMessage { it.copy(organizationId = value, idError = false) }

    fun onUserIdChange(value: String) = clearMessage { it.copy(userId = value, userIdError = false) }

    fun onServerUrlChange(value: String) = clearMessage { it.copy(serverUrl = value, urlError = false) }

    fun onStubChanged(enabled: Boolean) {
        _state.update { it.copy(useStubApi = enabled) }
        viewModelScope.launch {
            settingsRepository.setUseStubApi(enabled)
        }
    }

    fun save() {
        val snapshot = _state.value
        if (snapshot.isSaving) return
        val settings = OrganizationSettings(
            name = snapshot.name,
            organizationId = snapshot.organizationId,
            userId = snapshot.userId,
            serverUrl = snapshot.serverUrl,
        )
        val errors = SettingsValidator.validate(settings)
        if (errors.hasAny) {
            _state.update {
                it.copy(
                    nameError = errors.name,
                    idError = errors.organizationId,
                    userIdError = errors.userId,
                    urlError = errors.serverUrl,
                    message = null,
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null) }
            try {
                settingsRepository.save(settings)
                _state.update {
                    it.copy(isSaving = false, message = "Настройки сохранены", messageIsError = false)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update {
                    it.copy(isSaving = false, message = error.toUiText(), messageIsError = true)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    private fun clearMessage(reduce: (SettingsUiState) -> SettingsUiState) {
        _state.update { reduce(it).copy(message = null) }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository, authRepository) as T
        }
    }
}
