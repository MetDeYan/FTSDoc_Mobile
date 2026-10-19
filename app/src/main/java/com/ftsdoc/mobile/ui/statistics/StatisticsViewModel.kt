package com.ftsdoc.mobile.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ftsdoc.mobile.domain.model.DocumentStatistics
import com.ftsdoc.mobile.domain.repository.SettingsRepository
import com.ftsdoc.mobile.domain.repository.StatisticsRepository
import com.ftsdoc.mobile.ui.common.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val statistics: DocumentStatistics? = null,
    val organizationName: String = "",
    val error: String? = null,
)

class StatisticsViewModel(
    private val statisticsRepository: StatisticsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update { it.copy(organizationName = settings.name) }
            }
        }
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            statisticsRepository.load()
                .onSuccess { statistics ->
                    _state.update { it.copy(isLoading = false, statistics = statistics) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                }
        }
    }

    class Factory(
        private val statisticsRepository: StatisticsRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatisticsViewModel(statisticsRepository, settingsRepository) as T
        }
    }
}
