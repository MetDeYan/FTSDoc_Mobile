package com.ftsdoc.mobile.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsdoc.mobile.domain.model.DocumentStatistics
import com.ftsdoc.mobile.domain.model.StatsSource
import com.ftsdoc.mobile.ui.LocalAppContainer
import com.ftsdoc.mobile.ui.common.BannerTone
import com.ftsdoc.mobile.ui.common.StatusBanner
import com.ftsdoc.mobile.ui.theme.FtsDocTheme

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = statisticsViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            viewModel.refresh()
        }
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    StatisticsContent(state = state, onRefresh = viewModel::refresh)
}

@Composable
private fun statisticsViewModel(): StatisticsViewModel {
    val container = LocalAppContainer.current
    return viewModel(
        factory = StatisticsViewModel.Factory(
            statisticsRepository = container.statisticsRepository,
            settingsRepository = container.settingsRepository,
        ),
    )
}

@Composable
private fun StatisticsContent(
    state: StatisticsUiState,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.organizationName.isNotBlank()) {
            Text(
                text = state.organizationName,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (state.isLoading && state.statistics == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            val stats = state.statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    title = "Документы",
                    value = stats?.documentsCount ?: 0,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "Страницы",
                    value = stats?.pagesCount ?: 0,
                    modifier = Modifier.weight(1f),
                )
            }
            stats?.let {
                val source = if (it.source == StatsSource.REMOTE) {
                    "Данные с сервера"
                } else {
                    "Данные этого устройства"
                }
                Text(
                    text = source,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            stats?.notice?.let { notice ->
                StatusBanner(text = notice, tone = BannerTone.Info)
            }
            state.error?.let { message ->
                StatusBanner(text = message, tone = BannerTone.Error)
            }
            Button(onClick = onRefresh, enabled = !state.isLoading) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text("Обновить", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsPreview() {
    FtsDocTheme {
        StatisticsContent(
            state = StatisticsUiState(
                isLoading = false,
                organizationName = "ООО Пример",
                statistics = DocumentStatistics(
                    documentsCount = 12,
                    pagesCount = 37,
                    source = StatsSource.LOCAL,
                    notice = "Локальный режим. Показаны данные этого устройства.",
                ),
            ),
            onRefresh = {},
        )
    }
}
