package com.ftsdoc.mobile.ui.document

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ftsdoc.mobile.data.local.CameraFiles
import com.ftsdoc.mobile.domain.model.DocumentCatalog
import com.ftsdoc.mobile.domain.model.DocumentType
import com.ftsdoc.mobile.domain.model.PickedFile
import com.ftsdoc.mobile.ui.LocalAppContainer
import com.ftsdoc.mobile.ui.common.BannerTone
import com.ftsdoc.mobile.ui.common.StatusBanner

@Composable
fun DocumentScreen(
    viewModel: DocumentViewModel = documentViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val saved = pendingCameraUri
        if (success && saved != null) {
            viewModel.addUris(listOf(Uri.parse(saved)))
        }
    }

    fun openCamera() {
        val uri = CameraFiles.createCaptureUri(context)
        pendingCameraUri = uri.toString()
        try {
            takePicture.launch(uri)
        } catch (_: android.content.ActivityNotFoundException) {
            viewModel.reportError("На устройстве нет приложения камеры")
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            viewModel.reportError("Разрешите доступ к камере или выберите файл")
        }
    }

    val pickFiles = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // Временного разрешения текущей сессии достаточно для отправки.
            }
        }
        viewModel.addUris(uris)
    }

    DocumentContent(
        state = state,
        onTakePhoto = {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) openCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
        },
        onPickFile = { pickFiles.launch(arrayOf("image/*", "application/pdf")) },
        onNameChange = viewModel::onNameChange,
        onTypeChange = viewModel::onTypeChange,
        onSelect = viewModel::select,
        onRemove = viewModel::remove,
        onSend = viewModel::send,
    )
}

@Composable
private fun documentViewModel(): DocumentViewModel {
    val container = LocalAppContainer.current
    return viewModel(
        factory = DocumentViewModel.Factory(
            documentRepository = container.documentRepository,
            settingsRepository = container.settingsRepository,
            fileReader = container.fileReader,
        ),
    )
}

@Composable
private fun DocumentContent(
    state: DocumentUiState,
    onTakePhoto: () -> Unit,
    onPickFile: () -> Unit,
    onNameChange: (String) -> Unit,
    onTypeChange: (DocumentType) -> Unit,
    onSelect: (Int) -> Unit,
    onRemove: (String) -> Unit,
    onSend: () -> Unit,
) {
    val enabled = !state.isSending
    val selected = state.files.getOrNull(state.selectedIndex)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onTakePhoto,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
            Text("Сделать фото", modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedButton(
            onClick = onPickFile,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            Icon(Icons.Filled.FolderOpen, contentDescription = null)
            Text("Выбрать файл", modifier = Modifier.padding(start = 8.dp))
        }
        PreviewArea(
            file = selected,
            filesError = state.filesError,
            onRemove = selected?.let { file -> { onRemove(file.uri) } },
        )
        if (state.files.size > 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(state.files, key = { _, file -> file.uri }) { index, file ->
                    FileThumb(
                        file = file,
                        selected = index == state.selectedIndex,
                        onClick = { onSelect(index) },
                    )
                }
            }
        }
        if (state.files.isNotEmpty()) {
            val pages = state.files.sumOf { it.pageCount }
            Text(
                text = "Файлов: ${state.files.size} · страниц: $pages",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.filesError) {
            Text(
                text = "Добавьте хотя бы один файл",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        OutlinedTextField(
            value = state.documentName,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Наименование документа") },
            singleLine = true,
            enabled = enabled,
            isError = state.nameError,
            supportingText = if (state.nameError) {
                { Text("Укажите наименование документа") }
            } else if (state.files.size == 1) {
                { Text("Для одного файла подставляется имя файла") }
            } else {
                null
            },
        )
        DocumentTypeField(
            selected = state.documentType,
            isError = state.typeError,
            enabled = enabled,
            onSelected = onTypeChange,
        )
        if (state.useStubApi) {
            Text(
                text = "Локальный режим: документ не уйдёт на сервер, счётчик на устройстве увеличится.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        state.error?.let { StatusBanner(text = it, tone = BannerTone.Error) }
        state.successMessage?.let { StatusBanner(text = it, tone = BannerTone.Success) }
        Button(
            onClick = onSend,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = enabled,
        ) {
            if (state.isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Отправить на сервер")
            }
        }
    }
}

@Composable
private fun PreviewArea(
    file: PickedFile?,
    filesError: Boolean,
    onRemove: (() -> Unit)?,
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (filesError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .border(1.dp, borderColor, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (file == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Здесь появится предпросмотр фото или файла",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    if (file.isImage) {
                        AsyncImage(
                            model = Uri.parse(file.uri),
                            contentDescription = file.displayName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (file.isPdf) Icons.Filled.PictureAsPdf else Icons.Filled.Description,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = file.displayName,
                                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${file.displayName} · стр. ${file.pageCount}",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (onRemove != null) {
                        TextButton(onClick = onRemove) { Text("Убрать") }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileThumb(
    file: PickedFile,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (file.isImage) {
            AsyncImage(
                model = Uri.parse(file.uri),
                contentDescription = file.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = if (file.isPdf) Icons.Filled.PictureAsPdf else Icons.Filled.Description,
                contentDescription = file.displayName,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DocumentTypeField(
    selected: DocumentType?,
    isError: Boolean,
    enabled: Boolean,
    onSelected: (DocumentType) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected?.title.orEmpty(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = enabled,
            isError = isError,
            label = { Text("Вид документа") },
            placeholder = { Text("ТТН или ТН") },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Открыть список видов")
            },
            supportingText = if (isError) {
                { Text("Выберите вид документа") }
            } else {
                null
            },
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DocumentCatalog.types.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.title) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    },
                )
            }
        }
    }
}
