/*
 * Copyright (C) 2025-2026 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.sandbox.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.android.axion.compose.expressive.ExpressiveBlurSurface
import com.android.axion.compose.expressive.ExpressiveFloatingToolbar
import com.android.axion.compose.expressive.ExpressiveMorphButton
import com.android.axion.compose.expressive.ExpressiveMorphContainer
import com.android.axion.compose.expressive.ExpressiveMorphLoadingIndicator
import com.android.axion.compose.expressive.ExpressiveSegmentOption
import com.android.axion.compose.expressive.ExpressiveSegmentedButtonRow
import com.android.axion.compose.expressive.ExpressiveShapePreset
import com.android.axion.compose.expressive.ExpressiveToolbarIconButton
import com.android.axion.compose.expressive.ExpressiveToolbarPrimaryPill
import com.android.axion.compose.expressive.ExpressiveWavyCircularProgress
import com.android.axion.compose.expressive.rememberExpressiveHaptics
import com.android.axion.compose.expressive.rememberExpressiveReducedMotion
import com.android.axion.compose.preferences.ExpressiveShapes
import com.android.axion.compose.preferences.PreferenceTokens
import com.android.axion.compose.preferences.dialogWidth
import com.android.axion.compose.preferences.preferencePosition
import com.android.axion.compose.preferences.preferenceShape
import com.android.axion.compose.theme.withTabularNums
import com.android.axion.sandbox.R
import com.android.axion.sandbox.io.FileVaultManager
import com.android.axion.sandbox.io.VaultFile
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class VaultFilter {
    ALL,
    MEDIA,
    FILES,
}

@Composable
fun VaultTab(
    isUnlocked: Boolean = false,
    onUnlockRequest: () -> Unit = {},
    onPickingFilesChange: (Boolean) -> Unit = {},
) {
    if (!isUnlocked) {
        LockedVaultScreen(onUnlockRequest)
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberExpressiveHaptics()
    val vaultManager = remember { FileVaultManager(context) }
    var files by remember { mutableStateOf<List<VaultFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by rememberSaveable { mutableStateOf(VaultFilter.ALL) }

    fun refreshFiles() {
        scope.launch(Dispatchers.IO) {
            try {
                vaultManager.migrateLegacyIfNeeded()
                val list = vaultManager.getVaultFiles()
                withContext(Dispatchers.Main) {
                    files = list
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onPickingFilesChange(false)
                refreshFiles()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var selectedFiles by remember { mutableStateOf(setOf<String>()) }
    val isMultiSelectMode by remember { derivedStateOf { selectedFiles.isNotEmpty() } }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("") }
    var restoredPopupMessage by remember { mutableStateOf<String?>(null) }

    fun toggleSelection(id: String) {
        haptics.performStepTick()
        selectedFiles = if (selectedFiles.contains(id)) selectedFiles - id else selectedFiles + id
    }

    LaunchedEffect(Unit) { refreshFiles() }

    val pickFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            isProcessing = true
            processingMessage = context.getString(R.string.action_import_vault)
            scope.launch(Dispatchers.IO) {
                try {
                    uris.forEach { vaultManager.importFile(it) }
                } finally {
                    withContext(Dispatchers.Main) {
                        isProcessing = false
                        refreshFiles()
                    }
                }
            }
        }
    }

    fun executeRestore() {
        isProcessing = true
        processingMessage = context.getString(R.string.action_export)
        scope.launch(Dispatchers.IO) {
            try {
                val toRestore = files.filter { selectedFiles.contains(it.id) }
                val count = vaultManager.restoreFiles(toRestore)
                withContext(Dispatchers.Main) {
                    if (count == 1) {
                        val file = toRestore.first()
                        val path = if (!file.originalPath.isNullOrEmpty()) {
                            file.originalPath
                        } else {
                            "Downloads/Vault_Restored/" + file.name
                        }
                        restoredPopupMessage = "Restored to: $path"
                    } else if (count > 1) {
                        restoredPopupMessage = "$count files successfully restored!"
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    selectedFiles = emptySet()
                    refreshFiles()
                }
            }
        }
    }

    fun executeDelete() {
        isProcessing = true
        processingMessage = context.getString(R.string.action_delete)
        scope.launch(Dispatchers.IO) {
            try {
                val toDelete = files.filter { selectedFiles.contains(it.id) }
                vaultManager.deleteFiles(toDelete)
            } finally {
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    selectedFiles = emptySet()
                    refreshFiles()
                }
            }
        }
    }

    fun handleOpenFile(file: VaultFile) {
        haptics.performStepTick()
        onPickingFilesChange(true)
        openVaultFileInternal(context, file, vaultContentUri(file))
    }

    val videos = remember(files) { files.filter { it.mimeType.startsWith("video/") } }
    val images = remember(files) { files.filter { it.mimeType.startsWith("image/") } }
    val otherFiles = remember(files) {
        files.filter { !it.mimeType.startsWith("image/") && !it.mimeType.startsWith("video/") }
    }

    val filterOptions = remember {
        listOf(
            ExpressiveSegmentOption(VaultFilter.ALL, "All", Icons.Outlined.FolderOpen),
            ExpressiveSegmentOption(VaultFilter.MEDIA, "Media", Icons.Default.PhotoLibrary),
            ExpressiveSegmentOption(VaultFilter.FILES, "Files", Icons.Default.Description),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            VaultLoadingScreen()
        } else if (files.isEmpty()) {
            EmptyVaultScreen()
        } else {
            val showMedia = selectedFilter == VaultFilter.ALL || selectedFilter == VaultFilter.MEDIA
            val showDocs = selectedFilter == VaultFilter.ALL || selectedFilter == VaultFilter.FILES

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 172.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                item(span = { GridItemSpan(3) }) {
                    ExpressiveSegmentedButtonRow(
                        options = filterOptions,
                        selectedValue = selectedFilter,
                        onValueSelected = { selectedFilter = it },
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }

                if (showMedia && images.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        VaultSectionHeader("Images", images.size)
                    }
                    items(images, key = { it.id }) { file ->
                        MediaGridItem(
                            file = file,
                            vaultManager = vaultManager,
                            isSelected = selectedFiles.contains(file.id),
                            isVideo = false,
                            onClick = {
                                if (isMultiSelectMode) {
                                    toggleSelection(file.id)
                                } else {
                                    handleOpenFile(file)
                                }
                            },
                            onLongClick = {
                                haptics.performLongPress()
                                toggleSelection(file.id)
                            },
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }

                if (showMedia && videos.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Spacer(modifier = Modifier.height(12.dp))
                        VaultSectionHeader("Videos", videos.size)
                    }
                    items(videos, key = { it.id }) { file ->
                        MediaGridItem(
                            file = file,
                            vaultManager = vaultManager,
                            isSelected = selectedFiles.contains(file.id),
                            isVideo = true,
                            onClick = {
                                if (isMultiSelectMode) {
                                    toggleSelection(file.id)
                                } else {
                                    handleOpenFile(file)
                                }
                            },
                            onLongClick = {
                                haptics.performLongPress()
                                toggleSelection(file.id)
                            },
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }

                if (showDocs && otherFiles.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Spacer(modifier = Modifier.height(12.dp))
                        VaultSectionHeader("Files", otherFiles.size)
                    }
                    itemsIndexed(
                        items = otherFiles,
                        key = { _, file -> file.id },
                        span = { _, _ -> GridItemSpan(3) },
                    ) { index, file ->
                        val shape = preferenceShape(
                            preferencePosition(index = index, count = otherFiles.size),
                        )
                        FileListItem(
                            file = file,
                            shape = shape,
                            isSelected = selectedFiles.contains(file.id),
                            onClick = {
                                if (isMultiSelectMode) {
                                    toggleSelection(file.id)
                                } else {
                                    handleOpenFile(file)
                                }
                            },
                            onLongClick = {
                                haptics.performLongPress()
                                toggleSelection(file.id)
                            },
                        )
                    }
                }
            }
        }

        // Action bar positioned cleanly above the bottom Sandbox floating navigation bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 92.dp),
        ) {
            if (!isMultiSelectMode) {
                ExpressiveFloatingToolbar(
                    frosted = true,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    ExpressiveToolbarPrimaryPill(
                        label = stringResource(R.string.action_import_vault),
                        icon = Icons.Default.Add,
                        onClick = {
                            onPickingFilesChange(true)
                            pickFilesLauncher.launch(arrayOf("*/*"))
                        },
                    )
                }
            } else {
                ExpressiveFloatingToolbar(
                    frosted = true,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    leadingContent = {
                        ExpressiveToolbarIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_cancel),
                            onClick = { selectedFiles = emptySet() },
                        )
                        Text(
                            text = "${selectedFiles.size}",
                            style = MaterialTheme.typography.titleMediumEmphasized.withTabularNums(),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp),
                        )
                    },
                ) {
                    ExpressiveMorphButton(
                        onClick = { showRestoreConfirm = true },
                        selected = false,
                        restingPreset = ExpressiveShapePreset.PILL,
                        pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    ) {
                        Text(text = stringResource(R.string.action_export))
                    }

                    ExpressiveMorphButton(
                        onClick = { showDeleteConfirm = true },
                        selected = true,
                        restingPreset = ExpressiveShapePreset.PILL,
                        pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                        selectedPreset = ExpressiveShapePreset.PILL,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    ) {
                        Text(text = stringResource(R.string.action_delete))
                    }
                }
            }
        }

        if (showDeleteConfirm || showRestoreConfirm) {
            val isDelete = showDeleteConfirm
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirm = false
                    showRestoreConfirm = false
                },
                shape = ExpressiveShapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.width(dialogWidth()),
                title = {
                    Text(
                        text = if (isDelete) {
                            stringResource(R.string.delete_confirm_title)
                        } else {
                            stringResource(R.string.action_export)
                        },
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                    )
                },
                text = {
                    Text(
                        text = if (isDelete) {
                            "Permanently delete ${selectedFiles.size} selected file(s)?"
                        } else {
                            "Restore ${selectedFiles.size} selected file(s) from the vault?"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                confirmButton = {
                    ExpressiveMorphButton(
                        onClick = {
                            if (isDelete) executeDelete() else executeRestore()
                            showDeleteConfirm = false
                            showRestoreConfirm = false
                        },
                        selected = true,
                        restingPreset = ExpressiveShapePreset.PILL,
                        pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                        selectedPreset = ExpressiveShapePreset.PILL,
                        containerColor = if (isDelete) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        contentColor = if (isDelete) {
                            MaterialTheme.colorScheme.onError
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = if (isDelete) {
                                stringResource(R.string.action_delete)
                            } else {
                                stringResource(R.string.action_export)
                            },
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            haptics.performStepTick()
                            showDeleteConfirm = false
                            showRestoreConfirm = false
                        },
                        shape = CircleShape,
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            style = MaterialTheme.typography.labelLargeEmphasized,
                        )
                    }
                },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            )
        }

        if (isProcessing) {
            Dialog(onDismissRequest = {}) {
                ExpressiveBlurSurface(
                    shape = ExpressiveShapes.extraLarge,
                    tintColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    applyDialogWindowBlur = true,
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(min = 260.dp)
                            .padding(horizontal = 24.dp, vertical = 22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ExpressiveWavyCircularProgress(
                            size = 44.dp,
                            strokeWidth = 4.dp,
                        )
                        Text(
                            text = processingMessage,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        restoredPopupMessage?.let { msg ->
            LaunchedEffect(msg) {
                delay(4000)
                restoredPopupMessage = null
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 160.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .wrapContentSize()
                        .animateContentSize(),
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodyMediumEmphasized,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ExpressiveMorphLoadingIndicator(
            size = 56.dp,
            contained = true,
        )
    }
}

@Composable
private fun VaultSectionHeader(
    title: String,
    count: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmallEmphasized,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmallEmphasized.withTabularNums(),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun MediaGridItem(
    file: VaultFile,
    vaultManager: FileVaultManager,
    isSelected: Boolean,
    isVideo: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = rememberExpressiveReducedMotion()
    var bitmap by remember(file.id) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(file.id) {
        if (bitmap == null) {
            withContext(Dispatchers.IO) {
                val decoded = if (isVideo) {
                    vaultManager.decryptToVideoThumbnail(file)
                } else {
                    vaultManager.decryptToBitmap(file)
                }
                withContext(Dispatchers.Main) { bitmap = decoded }
            }
        }
    }

    val cornerDp by animateDpAsState(
        targetValue = if (isSelected) 12.dp else PreferenceTokens.CornerRadiusOuter,
        animationSpec = if (reducedMotion) {
            snap()
        } else {
            MaterialTheme.motionScheme.fastSpatialSpec()
        },
        label = "VaultMediaTileCorner",
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerDp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = file.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = if (isVideo) Icons.Default.VideoFile else Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        if (isVideo) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.86f),
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center),
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .clip(CircleShape)
                        .align(Alignment.TopEnd),
                )
            }
        }
    }
}

@Composable
private fun FileListItem(
    file: VaultFile,
    shape: androidx.compose.ui.graphics.Shape,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        shape = shape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = when {
                file.mimeType.startsWith("video/") -> Icons.Default.VideoFile
                file.mimeType.startsWith("audio/") -> Icons.Default.AudioFile
                file.mimeType == "application/pdf" -> Icons.Default.PictureAsPdf
                file.mimeType == "application/vnd.android.package-archive" -> Icons.Default.Android
                file.mimeType.contains("zip") -> Icons.Default.FolderZip
                else -> Icons.Default.InsertDriveFile
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatFileSize(file.size),
                    style = MaterialTheme.typography.bodySmall.withTabularNums(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

private fun vaultContentUri(file: VaultFile): Uri =
    Uri.parse("content://$VAULT_AUTHORITY/${file.id}")

private fun openVaultFileInternal(context: Context, file: VaultFile, uri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, file.mimeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (uri.authority == VAULT_AUTHORITY) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        val chooser = Intent.createChooser(intent, "Open with")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (_: Exception) {
    }
}

private const val VAULT_AUTHORITY = "com.android.axion.sandbox.vault"

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        .coerceIn(0, units.lastIndex)
    return String.format(
        Locale.getDefault(),
        "%.1f %s",
        size / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups],
    )
}

@Composable
private fun LockedVaultScreen(onUnlockRequest: () -> Unit) {
    var morphed by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            ExpressiveMorphContainer(
                active = morphed,
                inactivePreset = ExpressiveShapePreset.COOKIE_7,
                activePreset = ExpressiveShapePreset.SOFT_BURST,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { morphed = !morphed },
                modifier = Modifier.size(92.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.vault_locked_title),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.vault_locked_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            ExpressiveMorphButton(
                onClick = onUnlockRequest,
                selected = true,
                restingPreset = ExpressiveShapePreset.PILL,
                pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                selectedPreset = ExpressiveShapePreset.PILL,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            ) {
                Text(text = stringResource(R.string.action_unlock))
            }
        }
    }
}

@Composable
private fun EmptyVaultScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            ExpressiveMorphContainer(
                active = false,
                inactivePreset = ExpressiveShapePreset.COOKIE_7,
                activePreset = ExpressiveShapePreset.SOFT_BURST,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(88.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.vault_empty_title),
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.vault_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
