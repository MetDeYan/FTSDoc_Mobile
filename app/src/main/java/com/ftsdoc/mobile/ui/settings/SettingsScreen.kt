package com.ftsdoc.mobile.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsdoc.mobile.ui.LocalAppContainer
import com.ftsdoc.mobile.ui.common.BannerTone
import com.ftsdoc.mobile.ui.common.StatusBanner

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = settingsViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmLogout by rememberSaveable { mutableStateOf(false) }

    if (!state.initialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Выйти из аккаунта?") },
            text = { Text("Для новой отправки документов нужно будет войти снова.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                        viewModel.logout()
                    },
                ) { Text("Выйти") }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Отмена") }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle("Подключение")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Локальный режим", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Заглушки вместо сервера. Экран можно проверить без сети.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = state.useStubApi,
                onCheckedChange = viewModel::onStubChanged,
            )
        }

        SectionTitle("Организация")
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название организации") },
            singleLine = true,
            enabled = !state.isSaving,
            isError = state.nameError,
            supportingText = if (state.nameError) {
                { Text("Укажите название") }
            } else {
                null
            },
        )
        OutlinedTextField(
            value = state.organizationId,
            onValueChange = viewModel::onIdChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("ID организации") },
            singleLine = true,
            enabled = !state.isSaving,
            isError = state.idError,
            supportingText = if (state.idError) {
                { Text("Укажите ID") }
            } else {
                null
            },
        )
        OutlinedTextField(
            value = state.serverUrl,
            onValueChange = viewModel::onServerUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Адрес сервера") },
            singleLine = true,
            enabled = !state.isSaving,
            isError = state.urlError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            supportingText = {
                Text(
                    if (state.urlError) {
                        "Укажите адрес вида http://сервер или https://сервер"
                    } else {
                        "Без /import/docs/. Для эмулятора: https://10.0.2.2:443/dev010AI/hs"
                    },
                )
            },
        )
        state.message?.let { message ->
            StatusBanner(
                text = message,
                tone = if (state.messageIsError) BannerTone.Error else BannerTone.Success,
            )
        }
        Button(
            onClick = viewModel::save,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !state.isSaving,
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Сохранить")
            }
        }

        SectionTitle("Сессия")
        OutlinedButton(
            onClick = { confirmLogout = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = null,
            )
            Text("Выйти", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun settingsViewModel(): SettingsViewModel {
    val container = LocalAppContainer.current
    return viewModel(
        factory = SettingsViewModel.Factory(
            settingsRepository = container.settingsRepository,
            authRepository = container.authRepository,
        ),
    )
}
