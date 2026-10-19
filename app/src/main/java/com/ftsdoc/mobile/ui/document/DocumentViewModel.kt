package com.ftsdoc.mobile.ui.document

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ftsdoc.mobile.data.local.DocumentFileReader
import com.ftsdoc.mobile.domain.model.DocumentFilePayload
import com.ftsdoc.mobile.domain.model.DocumentType
import com.ftsdoc.mobile.domain.model.DocumentUpload
import com.ftsdoc.mobile.domain.model.PickedFile
import com.ftsdoc.mobile.domain.repository.DocumentRepository
import com.ftsdoc.mobile.domain.repository.SettingsRepository
import com.ftsdoc.mobile.domain.validation.DocumentValidator
import com.ftsdoc.mobile.ui.common.toUiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DocumentUiState(
    val files: List<PickedFile> = emptyList(),
    val selectedIndex: Int = 0,
    val documentName: String = "",
    val documentType: DocumentType? = null,
    val nameError: Boolean = false,
    val typeError: Boolean = false,
    val filesError: Boolean = false,
    val isSending: Boolean = false,
    val useStubApi: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,
)

class DocumentViewModel(
    private val documentRepository: DocumentRepository,
    private val settingsRepository: SettingsRepository,
    private val fileReader: DocumentFileReader,
) : ViewModel() {
    private val _state = MutableStateFlow(DocumentUiState())
    val uiState: StateFlow<DocumentUiState> = _state.asStateFlow()
    private var nameEdited = false

    init {
        viewModelScope.launch {
            settingsRepository.useStubApi.collect { enabled ->
                _state.update { it.copy(useStubApi = enabled) }
            }
        }
    }

    fun onNameChange(value: String) {
        nameEdited = true
        _state.update {
            it.copy(documentName = value, nameError = false, error = null, successMessage = null)
        }
    }

    fun onTypeChange(type: DocumentType) {
        _state.update {
            it.copy(documentType = type, typeError = false, error = null, successMessage = null)
        }
    }

    fun select(index: Int) {
        _state.update { current ->
            if (index !in current.files.indices) current else current.copy(selectedIndex = index)
        }
    }

    fun reportError(message: String) {
        _state.update { it.copy(error = message, successMessage = null) }
    }

    fun addUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            try {
                val picked = withContext(Dispatchers.IO) {
                    uris.map { uri -> fileReader.inspect(uri) }
                }
                _state.update { current -> mergeFiles(current, picked) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(error = error.toUiText(), successMessage = null) }
            }
        }
    }

    fun remove(uri: String) {
        _state.update { current ->
            val files = current.files.filterNot { file -> file.uri == uri }
            current.copy(
                files = files,
                documentName = autoName(current.documentName, files),
                selectedIndex = current.selectedIndex.coerceAtMost((files.lastIndex).coerceAtLeast(0)),
                filesError = false,
                error = null,
                successMessage = null,
            )
        }
    }

    fun send() {
        val snapshot = _state.value
        if (snapshot.isSending) return
        val name = when {
            snapshot.documentName.isBlank() && snapshot.files.size == 1 -> snapshot.files.first().displayName
            else -> snapshot.documentName.trim()
        }
        val errors = DocumentValidator.validate(name, snapshot.documentType, snapshot.files.size)
        if (errors.hasAny) {
            _state.update {
                it.copy(
                    documentName = name,
                    nameError = errors.name,
                    typeError = errors.type,
                    filesError = errors.files,
                    successMessage = null,
                )
            }
            return
        }
        val type = snapshot.documentType ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    documentName = name,
                    isSending = true,
                    error = null,
                    successMessage = null,
                    nameError = false,
                    typeError = false,
                    filesError = false,
                )
            }
            val upload = try {
                withContext(Dispatchers.IO) {
                    DocumentUpload(
                        documentName = name,
                        documentType = type,
                        files = snapshot.files.map { file ->
                            DocumentFilePayload(
                                fileName = file.displayName,
                                bytes = fileReader.readBytes(file.uri),
                                pageCount = file.pageCount,
                            )
                        },
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(isSending = false, error = error.toUiText()) }
                return@launch
            }
            documentRepository.upload(upload)
                .onSuccess { result ->
                    nameEdited = false
                    _state.value = DocumentUiState(
                        useStubApi = _state.value.useStubApi,
                        successMessage = "Документ отправлен. Файлов: ${result.fileCount}, страниц: ${result.pageCount}.",
                    )
                }
                .onFailure { error ->
                    _state.update { it.copy(isSending = false, error = error.toUiText()) }
                }
        }
    }

    private fun mergeFiles(current: DocumentUiState, incoming: List<PickedFile>): DocumentUiState {
        val known = current.files.map { it.uri }.toSet()
        val fresh = incoming.filter { it.uri !in known }
        if (fresh.isEmpty()) return current.copy(successMessage = null)
        val room = MAX_FILES - current.files.size
        if (room <= 0) {
            return current.copy(
                error = "Можно прикрепить не больше $MAX_FILES файлов",
                successMessage = null,
            )
        }
        val accepted = fresh.take(room)
        val files = current.files + accepted
        return current.copy(
            files = files,
            selectedIndex = files.lastIndex,
            documentName = autoName(current.documentName, files),
            filesError = false,
            error = if (fresh.size > accepted.size) "Можно прикрепить не больше $MAX_FILES файлов" else null,
            successMessage = null,
        )
    }

    private fun autoName(currentName: String, files: List<PickedFile>): String {
        if (nameEdited) return currentName
        return when (files.size) {
            0 -> ""
            1 -> files.first().displayName
            else -> currentName
        }
    }

    class Factory(
        private val documentRepository: DocumentRepository,
        private val settingsRepository: SettingsRepository,
        private val fileReader: DocumentFileReader,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DocumentViewModel(documentRepository, settingsRepository, fileReader) as T
        }
    }

    private companion object {
        const val MAX_FILES = 10
    }
}
