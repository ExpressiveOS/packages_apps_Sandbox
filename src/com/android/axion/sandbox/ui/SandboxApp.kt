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
@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)

package com.android.axion.sandbox.ui

import android.app.AxSandboxManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.util.IconDrawableFactory
import android.util.Log
import android.util.LruCache
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.android.axion.compose.applist.AppEntry
import com.android.axion.compose.applist.AppFilter
import com.android.axion.compose.applist.rememberAppList
import com.android.axion.compose.expressive.ExpressiveFloatingToolbar
import com.android.axion.compose.expressive.ExpressiveMorphButton
import com.android.axion.compose.expressive.ExpressiveMorphContainer
import com.android.axion.compose.expressive.ExpressiveMorphLoadingIndicator
import com.android.axion.compose.expressive.ExpressiveSegmentOption
import com.android.axion.compose.expressive.ExpressiveSegmentedButtonRow
import com.android.axion.compose.expressive.ExpressiveShapePreset
import com.android.axion.compose.expressive.rememberExpressiveHaptics
import com.android.axion.compose.expressive.rememberExpressiveReducedMotion
import com.android.axion.compose.preferences.BasePreference
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.PreferencePosition
import com.android.axion.compose.preferences.PreferenceTokens
import com.android.axion.compose.preferences.SettingsType
import com.android.axion.compose.preferences.SwitchPreference
import com.android.axion.compose.preferences.preferencePosition
import com.android.axion.compose.preferences.preferenceShape
import com.android.axion.compose.preferences.rememberSettingsFlow
import com.android.axion.compose.scaffold.AxionLargeTopAppBar
import com.android.axion.compose.scaffold.AxionScaffold
import com.android.axion.compose.sheet.BottomSheetDialog
import com.android.axion.compose.theme.withTabularNums
import com.android.axion.sandbox.R
import com.android.internal.app.HiddenNotificationInfo
import com.android.internal.app.IHiddenNotificationListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SandboxApp"
private const val SANDBOX_CONFIG_KEY = "sandbox_config"
private const val SHEET_HEIGHT_FRACTION = 0.78f

private enum class AppCategoryFilter {
    ALL,
    PRIVATE,
    ISOLATED,
}

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val iconPainter: Painter = EmptyPainter,
    val uid: Int,
    val isLocked: Boolean,
    val isHidden: Boolean,
    val isLauncherHidden: Boolean,
    val isSandboxed: Boolean,
    val isSystem: Boolean = false,
    val userId: Int = 0,
    val isClone: Boolean = false,
) {
    val key: String get() = "${packageName}_$userId"
}

private data class UserSandboxPackages(
    val locked: Set<String> = emptySet(),
    val hidden: Set<String> = emptySet(),
    val launcherHidden: Set<String> = emptySet(),
    val sandboxed: Set<String> = emptySet(),
)

private fun AxSandboxManager?.getUserPackages(userId: Int): UserSandboxPackages {
    if (this == null) return UserSandboxPackages()
    return try {
        val locked = getLockedPackages(userId)?.toSet() ?: emptySet()
        val hidden = getHiddenPackages(userId)?.toSet() ?: emptySet()
        val launcherHidden = getHiddenFromLauncherPackages(userId)?.toSet() ?: emptySet()
        val sandboxed = getSandboxedPackages(userId)?.toSet() ?: emptySet()
        UserSandboxPackages(locked, hidden, launcherHidden, sandboxed)
    } catch (_: Exception) {
        UserSandboxPackages()
    }
}

@Composable
fun SandboxApp(
    onSettingsClick: () -> Unit = {},
    isSearchExpanded: Boolean = false,
    onSearchExpandedChange: (Boolean) -> Unit = {},
    isPrivateUnlocked: Boolean = false,
    onUnlockRequest: () -> Unit = {},
    isPrivateAreaExpanded: Boolean = false,
    onPrivateAreaExpandChange: (Boolean) -> Unit = {},
    isSecuritySetup: Boolean = false,
    onSetupSecurity: () -> Unit = {},
    selectedTabIndex: Int = 0,
    onTabIndexChange: (Int) -> Unit = {},
    isPickingFiles: Boolean = false,
    onPickingFilesChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = rememberExpressiveHaptics()
    val reducedMotion = rememberExpressiveReducedMotion()

    val appsState = rememberAppList(
        AppFilter.ALL,
        AppFilter.NO_OVERLAYS,
        AppFilter.INCLUDE_DUAL_APPS,
    )
    var refreshKey by remember { mutableStateOf(0) }
    val sandboxManager = remember {
        context.getSystemService(Context.AX_SANDBOX_SERVICE) as? AxSandboxManager
    }

    val density = LocalDensity.current
    val iconTargetPx = remember(density) { with(density) { 52.dp.roundToPx() } }

    val apps by produceState<List<AppInfo>>(
        initialValue = emptyList(),
        appsState.value,
        refreshKey,
    ) {
        if (appsState.value.isEmpty()) {
            value = emptyList()
            return@produceState
        }
        withContext(Dispatchers.Default) {
            val userPackagesMap = appsState.value
                .map { it.userId }
                .distinct()
                .associateWith { userId -> sandboxManager.getUserPackages(userId) }

            val iconDrawableFactory = IconDrawableFactory.newInstance(context)

            suspend fun processEntries(entries: List<AppEntry>): List<AppInfo> = coroutineScope {
                if (entries.isEmpty()) return@coroutineScope emptyList()
                val parallelism = Runtime.getRuntime().availableProcessors().coerceIn(2, 6)
                val chunkSize = (entries.size + parallelism - 1) / parallelism
                entries.chunked(chunkSize).map { chunk ->
                    async(Dispatchers.Default) {
                        chunk.map { entry ->
                            val userPkgs = userPackagesMap[entry.userId] ?: UserSandboxPackages()
                            val painter = AppIconCache.loadIconPainter(
                                packageName = entry.packageName,
                                userId = entry.userId,
                                icon = entry.icon,
                                iconDrawableFactory = iconDrawableFactory,
                                targetPx = iconTargetPx,
                            )
                            AppInfo(
                                packageName = entry.packageName,
                                label = entry.label,
                                icon = entry.icon,
                                iconPainter = painter,
                                uid = 0,
                                isLocked = userPkgs.locked.contains(entry.packageName),
                                isHidden = userPkgs.hidden.contains(entry.packageName),
                                isLauncherHidden = userPkgs.launcherHidden.contains(entry.packageName),
                                isSandboxed = userPkgs.sandboxed.contains(entry.packageName),
                                isSystem = entry.isSystem,
                                userId = entry.userId,
                                isClone = entry.isClone,
                            )
                        }
                    }
                }.awaitAll().flatten()
            }

            val (regularEntries, systemEntries) = appsState.value.partition { !it.isSystem }
            val regularApps = processEntries(regularEntries)
            value = regularApps

            val systemApps = processEntries(systemEntries)
            value = (regularApps + systemApps).sortedBy { it.label.lowercase() }
        }
    }
    val isLoading = apps.isEmpty()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }

    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { 3 },
    )

    LaunchedEffect(pagerState.currentPage) {
        onTabIndexChange(pagerState.currentPage)
    }

    val reloadApps: () -> Unit = {
        refreshKey++
        selectedApp?.let { selected ->
            selectedApp = apps.find {
                it.packageName == selected.packageName && it.userId == selected.userId
            }
        }
    }

    val settingsFlow = rememberSettingsFlow(SettingsType.SECURE)
    LaunchedEffect(settingsFlow) {
        settingsFlow.observe(SANDBOX_CONFIG_KEY).collect {
            reloadApps()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                reloadApps()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isEmpty()) {
            apps
        } else {
            apps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var notificationCount by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val mgr = context.getSystemService(Context.AX_SANDBOX_SERVICE) as? AxSandboxManager

        val listener = object : IHiddenNotificationListener.Stub() {
            override fun onHiddenNotificationPosted(info: HiddenNotificationInfo) {
                scope.launch(Dispatchers.Main) {
                    notificationCount++
                }
            }

            override fun onHiddenNotificationRemoved(key: String) {
                scope.launch(Dispatchers.Main) {
                    if (notificationCount > 0) notificationCount--
                }
            }
        }

        try {
            mgr?.registerHiddenNotificationListener(listener)
            scope.launch(Dispatchers.IO) {
                val count = mgr?.hiddenNotifications?.size ?: 0
                withContext(Dispatchers.Main) {
                    notificationCount = count
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register notification listener", e)
        }

        onDispose {
            try {
                mgr?.unregisterHiddenNotificationListener(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unregister notification listener", e)
            }
        }
    }

    if (selectedApp != null) {
        AppDetailScreen(
            app = selectedApp!!,
            onBackClick = { selectedApp = null },
            onLockToggle = { app ->
                scope.launch {
                    toggleAppLock(context, app.packageName, app.userId, !app.isLocked)
                    selectedApp = app.copy(isLocked = !app.isLocked)
                    reloadApps()
                }
            },
            onHideToggle = { app ->
                scope.launch {
                    val nextHideState = !app.isHidden
                    toggleAppHidden(context, app.packageName, app.userId, nextHideState)
                    if (nextHideState) {
                        toggleAppLauncherHidden(context, app.packageName, app.userId, false)
                        selectedApp = app.copy(isHidden = true, isLauncherHidden = false)
                    } else {
                        selectedApp = app.copy(isHidden = false)
                    }
                    reloadApps()
                }
            },
            onLauncherHideToggle = { app ->
                scope.launch {
                    val nextLauncherHideState = !app.isLauncherHidden
                    toggleAppLauncherHidden(
                        context,
                        app.packageName,
                        app.userId,
                        nextLauncherHideState,
                    )
                    if (nextLauncherHideState) {
                        toggleAppHidden(context, app.packageName, app.userId, false)
                        selectedApp = app.copy(isLauncherHidden = true, isHidden = false)
                    } else {
                        selectedApp = app.copy(isLauncherHidden = false)
                    }
                    reloadApps()
                }
            },
            onSandboxToggle = { app ->
                scope.launch {
                    toggleAppSandboxed(context, app.packageName, app.userId, !app.isSandboxed)
                    selectedApp = app.copy(isSandboxed = !app.isSandboxed)
                    reloadApps()
                }
            },
            onLaunch = { app ->
                launchApp(context, app.packageName, app.userId)
            },
            isSecuritySetup = isSecuritySetup,
        )
    } else {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            state = rememberTopAppBarState(),
            snapAnimationSpec = if (reducedMotion) {
                snap()
            } else {
                MaterialTheme.motionScheme.defaultSpatialSpec()
            },
        )

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                AxionLargeTopAppBar(
                    title = stringResource(R.string.sandbox_title),
                    scrollBehavior = scrollBehavior,
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContent = if (isSearchExpanded) {
                        {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(stringResource(R.string.search_hint)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                singleLine = true,
                                shape = CircleShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                        }
                    } else {
                        null
                    },
                    actions = {
                        FilledIconToggleButton(
                            checked = isSearchExpanded,
                            onCheckedChange = {
                                haptics.performToggle(it)
                                onSearchExpandedChange(it)
                                if (!it) searchQuery = ""
                            },
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.action_search),
                            )
                        }

                        Box {
                            FilledIconButton(
                                onClick = {
                                    haptics.performStepTick()
                                    scope.launch {
                                        if (reducedMotion) {
                                            pagerState.scrollToPage(1)
                                        } else {
                                            pagerState.animateScrollToPage(1)
                                        }
                                    }
                                },
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (notificationCount > 0) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                ),
                            ) {
                                Icon(
                                    imageVector = if (notificationCount > 0) {
                                        Icons.Filled.Notifications
                                    } else {
                                        Icons.Outlined.Notifications
                                    },
                                    contentDescription = stringResource(R.string.action_notifications),
                                )
                            }

                            if (notificationCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-2).dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = if (notificationCount > 9) {
                                            "9+"
                                        } else {
                                            notificationCount.toString()
                                        },
                                        style = MaterialTheme.typography.labelSmallEmphasized.withTabularNums(),
                                        color = MaterialTheme.colorScheme.onError,
                                    )
                                }
                            }
                        }

                        FilledIconButton(
                            onClick = {
                                haptics.performStepTick()
                                onSettingsClick()
                            },
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.action_settings),
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    when (page) {
                        0 -> AppsTab(
                            apps = filteredApps,
                            isLoading = isLoading,
                            onOpenAppDetail = { app -> selectedApp = app },
                            isPrivateAreaExpanded = isPrivateAreaExpanded,
                            onPrivateAreaExpandChange = onPrivateAreaExpandChange,
                            onUnlockRequest = onUnlockRequest,
                            isPrivateUnlocked = isPrivateUnlocked,
                            isSecuritySetup = isSecuritySetup,
                            onSetupSecurity = onSetupSecurity,
                        )

                        1 -> NotificationsTab(
                            isUnlocked = isPrivateUnlocked,
                            onUnlockRequest = onUnlockRequest,
                        )

                        2 -> VaultTab(
                            isUnlocked = isPrivateUnlocked,
                            onUnlockRequest = onUnlockRequest,
                            onPickingFilesChange = onPickingFilesChange,
                        )
                    }
                }

                SandboxFloatingNavToolbar(
                    selectedTab = pagerState.currentPage,
                    onTabSelected = { index ->
                        scope.launch {
                            if (reducedMotion) {
                                pagerState.scrollToPage(index)
                            } else {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    },
                    notificationCount = notificationCount,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun SandboxFloatingNavToolbar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    notificationCount: Int,
    modifier: Modifier = Modifier,
) {
    ExpressiveFloatingToolbar(
        modifier = modifier,
        frosted = true,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    ) {
        SandboxNavPillItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = if (selectedTab == 0) Icons.Filled.Apps else Icons.Outlined.Apps,
            label = stringResource(R.string.tab_apps),
        )
        SandboxNavPillItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = if (selectedTab == 1) Icons.Filled.Notifications else Icons.Outlined.Notifications,
            label = stringResource(R.string.tab_notifications),
            badgeCount = notificationCount,
        )
        SandboxNavPillItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = if (selectedTab == 2) Icons.Filled.FolderOpen else Icons.Outlined.FolderOpen,
            label = stringResource(R.string.tab_vault),
        )
    }
}

@Composable
private fun SandboxNavPillItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    badgeCount: Int = 0,
) {
    val haptics = rememberExpressiveHaptics()

    ExpressiveMorphContainer(
        active = selected,
        inactivePreset = ExpressiveShapePreset.SQUIRCLE,
        activePreset = ExpressiveShapePreset.PILL,
        containerColor = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        hapticOnClick = false,
        onClick = {
            haptics.performStepTick()
            onClick()
        },
        modifier = Modifier.height(48.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (selected) 16.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(22.dp),
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .size(if (badgeCount > 9) 16.dp else 14.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                                .withTabularNums(),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onError
                            },
                        )
                    }
                }
            }
            if (selected) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SandboxOptionsDropdown(
    app: AppInfo,
    sandboxManager: AxSandboxManager?,
    onSandboxToggle: (AppInfo) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val expanded = remember(app.packageName, app.userId) { app.isSandboxed }

    var restrictInternet by remember(app.packageName, app.userId) {
        val gids = sandboxManager?.getRestrictedGids(app.packageName, app.userId)
        mutableStateOf(gids != null && gids.contains(AxSandboxManager.GID_INET))
    }

    var restrictStorage by remember(app.packageName, app.userId) {
        val gids = sandboxManager?.getRestrictedGids(app.packageName, app.userId)
        val storageGids = AxSandboxManager.STORAGE_GIDS
        mutableStateOf(gids != null && storageGids.any { gids.contains(it) })
    }

    var dataIsolation by remember(app.packageName, app.userId) {
        mutableStateOf(
            sandboxManager?.isSandboxDataIsolationEnabled(app.packageName, app.userId) == true,
        )
    }

    PreferenceGroup(
        title = stringResource(R.string.sandbox_options_title),
        collapsible = true,
        initiallyExpanded = expanded,
    ) {
        item {
            SwitchPreference(
                title = stringResource(R.string.action_isolate),
                summary = stringResource(R.string.isolate_app_description),
                checked = app.isSandboxed,
                onCheckedChange = { onSandboxToggle(app) },
            )
        }
        item {
            SwitchPreference(
                title = stringResource(R.string.restrict_internet_title),
                summary = stringResource(R.string.restrict_internet_description),
                checked = restrictInternet,
                onCheckedChange = {
                    if (sandboxManager == null) return@SwitchPreference
                    restrictInternet = !restrictInternet
                    scope.launch(Dispatchers.IO) {
                        val current = sandboxManager.getRestrictedGids(app.packageName, app.userId)
                            ?.toMutableList() ?: mutableListOf()
                        if (restrictInternet) {
                            if (!current.contains(AxSandboxManager.GID_INET)) {
                                current.add(AxSandboxManager.GID_INET)
                            }
                        } else {
                            current.remove(AxSandboxManager.GID_INET)
                        }
                        sandboxManager.setRestrictedGids(
                            app.packageName,
                            current.toIntArray(),
                            app.userId,
                        )
                    }
                },
            )
        }
        item {
            SwitchPreference(
                title = stringResource(R.string.restrict_storage_title),
                summary = stringResource(R.string.restrict_storage_description),
                checked = restrictStorage,
                onCheckedChange = {
                    if (sandboxManager == null) return@SwitchPreference
                    restrictStorage = !restrictStorage
                    scope.launch(Dispatchers.IO) {
                        val current = sandboxManager.getRestrictedGids(app.packageName, app.userId)
                            ?.toMutableList() ?: mutableListOf()
                        val storageGids = AxSandboxManager.STORAGE_GIDS
                        if (restrictStorage) {
                            for (gid in storageGids) {
                                if (!current.contains(gid)) current.add(gid)
                            }
                        } else {
                            current.removeAll(storageGids.toSet())
                        }
                        sandboxManager.setRestrictedGids(
                            app.packageName,
                            current.toIntArray(),
                            app.userId,
                        )
                    }
                },
            )
        }
        item {
            SwitchPreference(
                title = stringResource(R.string.data_isolation_title),
                summary = stringResource(R.string.data_isolation_description),
                checked = dataIsolation,
                onCheckedChange = {
                    if (sandboxManager == null) return@SwitchPreference
                    dataIsolation = !dataIsolation
                    scope.launch(Dispatchers.IO) {
                        sandboxManager.setSandboxDataIsolationEnabled(
                            app.packageName,
                            dataIsolation,
                            app.userId,
                        )
                    }
                },
            )
        }
    }
}

private data class SpoofSettingEntry(
    val key: String,
    val titleRes: Int,
    val descriptionRes: Int,
)

private val SPOOF_SETTINGS = listOf(
    SpoofSettingEntry(
        "adb_enabled",
        R.string.spoof_adb_enabled,
        R.string.spoof_adb_enabled_description,
    ),
    SpoofSettingEntry(
        "development_settings_enabled",
        R.string.spoof_dev_settings_enabled,
        R.string.spoof_dev_settings_enabled_description,
    ),
    SpoofSettingEntry(
        "adb_wifi_enabled",
        R.string.spoof_adb_wifi_enabled,
        R.string.spoof_adb_wifi_enabled_description,
    ),
    SpoofSettingEntry(
        "package_verifier_user_consent",
        R.string.spoof_package_verifier,
        R.string.spoof_package_verifier_description,
    ),
    SpoofSettingEntry(
        "verify_apps_over_usb",
        R.string.spoof_verify_apps_usb,
        R.string.spoof_verify_apps_usb_description,
    ),
)

private val SPOOF_ACCESSIBILITY_KEYS = listOf(
    "accessibility_enabled",
    "enabled_accessibility_services",
    "accessibility_display_inversion_enabled",
)

@Composable
private fun SpoofSettingsDropdown(
    app: AppInfo,
    sandboxManager: AxSandboxManager?,
) {
    if (sandboxManager == null) return

    val scope = rememberCoroutineScope()

    val enabledSettings = remember(app.packageName, app.userId) {
        mutableStateMapOf<String, Boolean>().apply {
            SPOOF_SETTINGS.forEach { entry ->
                put(
                    entry.key,
                    sandboxManager.isSpoofSettingEnabled(
                        app.packageName,
                        entry.key,
                        app.userId,
                    ),
                )
            }
            put(
                "accessibility_spoof",
                SPOOF_ACCESSIBILITY_KEYS.all { key ->
                    sandboxManager.isSpoofSettingEnabled(app.packageName, key, app.userId)
                },
            )
        }
    }

    PreferenceGroup(
        title = stringResource(R.string.spoof_settings_title),
        collapsible = true,
        initiallyExpanded = false,
    ) {
        SPOOF_SETTINGS.forEach { entry ->
            item {
                val isEnabled = enabledSettings[entry.key] ?: false
                SwitchPreference(
                    title = stringResource(entry.titleRes),
                    summary = stringResource(entry.descriptionRes),
                    checked = isEnabled,
                    onCheckedChange = {
                        val newValue = !isEnabled
                        enabledSettings[entry.key] = newValue
                        scope.launch(Dispatchers.IO) {
                            sandboxManager.setSpoofSettingEnabled(
                                app.packageName,
                                entry.key,
                                newValue,
                                app.userId,
                            )
                        }
                    },
                )
            }
        }
        item {
            val accessibilityEnabled = enabledSettings["accessibility_spoof"] ?: false
            SwitchPreference(
                title = stringResource(R.string.spoof_accessibility),
                summary = stringResource(R.string.spoof_accessibility_description),
                checked = accessibilityEnabled,
                onCheckedChange = {
                    val newValue = !accessibilityEnabled
                    enabledSettings["accessibility_spoof"] = newValue
                    scope.launch(Dispatchers.IO) {
                        SPOOF_ACCESSIBILITY_KEYS.forEach { key ->
                            sandboxManager.setSpoofSettingEnabled(
                                app.packageName,
                                key,
                                newValue,
                                app.userId,
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun HideSettingsDropdown(
    app: AppInfo,
    onHideToggle: (AppInfo) -> Unit,
    onLauncherHideToggle: (AppInfo) -> Unit,
) {
    val expanded = remember(app.packageName, app.userId) {
        app.isHidden || app.isLauncherHidden
    }

    PreferenceGroup(
        title = stringResource(R.string.hide_settings_title),
        collapsible = true,
        initiallyExpanded = expanded,
    ) {
        item {
            SwitchPreference(
                title = stringResource(R.string.hide_app_title),
                summary = if (app.isHidden) {
                    stringResource(R.string.hide_app_description_on)
                } else {
                    stringResource(R.string.hide_app_description_off)
                },
                checked = app.isHidden,
                onCheckedChange = { onHideToggle(app) },
            )
        }
        item {
            SwitchPreference(
                title = stringResource(R.string.hide_from_launcher_title),
                summary = if (app.isLauncherHidden) {
                    stringResource(R.string.hide_from_launcher_description_on)
                } else {
                    stringResource(R.string.hide_from_launcher_description_off)
                },
                checked = app.isLauncherHidden,
                onCheckedChange = { onLauncherHideToggle(app) },
            )
        }
    }
}

@Composable
fun AppDetailScreen(
    app: AppInfo,
    onBackClick: () -> Unit,
    onLockToggle: (AppInfo) -> Unit,
    onHideToggle: (AppInfo) -> Unit,
    onLauncherHideToggle: (AppInfo) -> Unit,
    onSandboxToggle: (AppInfo) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    isSecuritySetup: Boolean = false,
) {
    AxionScaffold(
        title = app.label,
        onBackClick = onBackClick,
        collapsedByDefault = false,
        containerColor = MaterialTheme.colorScheme.surface,
        actions = {
            FilledTonalIconButton(onClick = { onLaunch(app) }) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = stringResource(R.string.action_launch),
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                ExpressiveMorphContainer(
                    active = app.isLocked || app.isHidden || app.isSandboxed,
                    inactivePreset = ExpressiveShapePreset.SQUIRCLE,
                    activePreset = ExpressiveShapePreset.COOKIE_7,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.size(96.dp),
                ) {
                    Image(
                        painter = app.iconPainter,
                        contentDescription = app.label,
                        modifier = Modifier.size(68.dp),
                    )
                }
            }

            item {
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isSecuritySetup) {
                item {
                    PreferenceGroup {
                        item {
                            SwitchPreference(
                                title = stringResource(R.string.lock_app_title),
                                summary = stringResource(R.string.lock_app_description),
                                checked = app.isLocked,
                                onCheckedChange = { onLockToggle(app) },
                                icon = if (app.isLocked) Icons.Filled.Lock else Icons.Outlined.Lock,
                            )
                        }
                    }
                }

                item {
                    HideSettingsDropdown(
                        app = app,
                        onHideToggle = onHideToggle,
                        onLauncherHideToggle = onLauncherHideToggle,
                    )
                }
            }

            item {
                val context = LocalContext.current
                val sandboxManager = remember {
                    context.getSystemService(AxSandboxManager::class.java)
                }
                SandboxOptionsDropdown(
                    app = app,
                    sandboxManager = sandboxManager,
                    onSandboxToggle = onSandboxToggle,
                )
            }

            item {
                val context = LocalContext.current
                val sandboxManager = remember {
                    context.getSystemService(AxSandboxManager::class.java)
                }
                SpoofSettingsDropdown(
                    app = app,
                    sandboxManager = sandboxManager,
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun AppsTab(
    apps: List<AppInfo>,
    isLoading: Boolean,
    onOpenAppDetail: (AppInfo) -> Unit,
    isPrivateAreaExpanded: Boolean = false,
    onPrivateAreaExpandChange: (Boolean) -> Unit = {},
    onUnlockRequest: () -> Unit = {},
    isPrivateUnlocked: Boolean = false,
    isSecuritySetup: Boolean = false,
    onSetupSecurity: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberExpressiveHaptics()
    var quickActionApp by remember { mutableStateOf<AppInfo?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSystemApps by rememberSaveable { mutableStateOf(false) }
    var categoryFilter by rememberSaveable { mutableStateOf(AppCategoryFilter.ALL) }

    val privateApps = remember(apps) { apps.filter { it.isLocked || it.isHidden } }
    val sandboxedApps = remember(apps) {
        apps.filter { it.isSandboxed && !it.isLocked && !it.isHidden }
    }
    val regularApps = remember(apps) {
        apps.filter { !it.isLocked && !it.isHidden && !it.isSandboxed && !it.isSystem }
    }
    val systemApps = remember(apps) {
        apps.filter { !it.isLocked && !it.isHidden && !it.isSandboxed && it.isSystem }
    }

    val effectivePrivateExpanded = isPrivateUnlocked &&
        (isPrivateAreaExpanded || categoryFilter == AppCategoryFilter.PRIVATE)

    val onLockToggle: (AppInfo) -> Unit = { app ->
        scope.launch {
            toggleAppLock(context, app.packageName, app.userId, !app.isLocked)
            quickActionApp = app.copy(isLocked = !app.isLocked)
        }
    }

    val onHideToggle: (AppInfo) -> Unit = { app ->
        scope.launch {
            val nextHideState = !app.isHidden
            toggleAppHidden(context, app.packageName, app.userId, nextHideState)
            if (nextHideState) {
                toggleAppLauncherHidden(context, app.packageName, app.userId, false)
                quickActionApp = app.copy(isHidden = true, isLauncherHidden = false)
            } else {
                quickActionApp = app.copy(isHidden = false)
            }
        }
    }

    val onLauncherHideToggle: (AppInfo) -> Unit = { app ->
        scope.launch {
            val nextLauncherHideState = !app.isLauncherHidden
            toggleAppLauncherHidden(context, app.packageName, app.userId, nextLauncherHideState)
            if (nextLauncherHideState) {
                toggleAppHidden(context, app.packageName, app.userId, false)
                quickActionApp = app.copy(isLauncherHidden = true, isHidden = false)
            } else {
                quickActionApp = app.copy(isLauncherHidden = false)
            }
        }
    }

    val onSandboxToggle: (AppInfo) -> Unit = { app ->
        scope.launch {
            toggleAppSandboxed(context, app.packageName, app.userId, !app.isSandboxed)
            quickActionApp = app.copy(isSandboxed = !app.isSandboxed)
        }
    }

    val onSheetDismiss = {
        showBottomSheet = false
        quickActionApp = null
    }

    if (showBottomSheet && quickActionApp != null) {
        BottomSheetDialog(
            onDismiss = onSheetDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            heightFraction = SHEET_HEIGHT_FRACTION,
        ) {
            AppQuickActionsSheet(
                app = quickActionApp!!,
                onLockToggle = onLockToggle,
                onHideToggle = onHideToggle,
                onLauncherHideToggle = onLauncherHideToggle,
                onSandboxToggle = onSandboxToggle,
                onLaunch = { app -> launchApp(context, app.packageName, app.userId) },
                onOpenDetail = { app ->
                    onSheetDismiss()
                    onOpenAppDetail(app)
                },
            )
        }
    }

    val filterOptions = listOf(
        ExpressiveSegmentOption(
            value = AppCategoryFilter.ALL,
            label = stringResource(R.string.section_all_apps),
            icon = Icons.Outlined.Apps,
        ),
        ExpressiveSegmentOption(
            value = AppCategoryFilter.PRIVATE,
            label = stringResource(R.string.section_private_apps),
            icon = Icons.Outlined.Lock,
        ),
        ExpressiveSegmentOption(
            value = AppCategoryFilter.ISOLATED,
            label = stringResource(R.string.section_isolated_apps),
            icon = Icons.Outlined.Security,
        ),
    )

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ExpressiveMorphLoadingIndicator(size = 56.dp, contained = true)
        }
    } else if (apps.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_apps_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(PreferenceTokens.ItemSpacing),
        ) {
            item(key = "category_filter_row") {
                ExpressiveSegmentedButtonRow(
                    options = filterOptions,
                    selectedValue = categoryFilter,
                    onValueSelected = { next ->
                        categoryFilter = next
                        if (next == AppCategoryFilter.PRIVATE && isSecuritySetup && !isPrivateUnlocked) {
                            onUnlockRequest()
                        }
                    },
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }

            if (!isSecuritySetup) {
                item(key = "setup_security_banner") {
                    PreferenceGroup(modifier = Modifier.padding(bottom = 10.dp)) {
                        item {
                            ClickablePreference(
                                title = stringResource(R.string.setup_private_apps_title),
                                summary = stringResource(R.string.setup_private_apps_description),
                                icon = Icons.Filled.Lock,
                                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                onClick = onSetupSecurity,
                            )
                        }
                    }
                }
            } else if (
                (categoryFilter == AppCategoryFilter.ALL || categoryFilter == AppCategoryFilter.PRIVATE) &&
                privateApps.isNotEmpty()
            ) {
                item(key = "private_header") {
                    CollapsibleSectionHeader(
                        icon = if (effectivePrivateExpanded) Icons.Filled.LockOpen else Icons.Filled.Lock,
                        title = stringResource(R.string.section_private_apps),
                        count = privateApps.size,
                        color = MaterialTheme.colorScheme.primary,
                        isExpanded = effectivePrivateExpanded,
                        hasItemsBelow = effectivePrivateExpanded && privateApps.isNotEmpty(),
                        onExpandChange = { wantExpand ->
                            if (wantExpand && !isPrivateUnlocked) {
                                onUnlockRequest()
                            } else {
                                onPrivateAreaExpandChange(wantExpand)
                            }
                        },
                    )
                }

                if (effectivePrivateExpanded) {
                    itemsIndexed(
                        items = privateApps,
                        key = { _, app -> "private_${app.key}" },
                    ) { index, app ->
                        val position = preferencePosition(
                            index = index,
                            count = privateApps.size,
                            firstIsMiddle = true,
                        )
                        SegmentedAppCard(
                            app = app,
                            shape = preferenceShape(position),
                            onClick = {
                                quickActionApp = app
                                showBottomSheet = true
                            },
                            onDetailClick = { onOpenAppDetail(app) },
                        )
                    }
                }

                item(key = "private_spacer") {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else if (categoryFilter == AppCategoryFilter.PRIVATE && privateApps.isEmpty()) {
                item(key = "private_empty") {
                    EmptyFilteredState(title = stringResource(R.string.no_apps_found))
                }
            }

            if (
                (categoryFilter == AppCategoryFilter.ALL || categoryFilter == AppCategoryFilter.ISOLATED) &&
                sandboxedApps.isNotEmpty()
            ) {
                item(key = "isolated_header") {
                    SectionHeader(
                        icon = Icons.Filled.Security,
                        title = stringResource(R.string.section_isolated_apps),
                        count = sandboxedApps.size,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }

                itemsIndexed(
                    items = sandboxedApps,
                    key = { _, app -> "isolated_${app.key}" },
                ) { index, app ->
                    val shape = preferenceShape(
                        preferencePosition(index = index, count = sandboxedApps.size),
                    )
                    SegmentedAppCard(
                        app = app,
                        shape = shape,
                        onClick = {
                            quickActionApp = app
                            showBottomSheet = true
                        },
                        onDetailClick = { onOpenAppDetail(app) },
                    )
                }

                item(key = "isolated_spacer") {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else if (categoryFilter == AppCategoryFilter.ISOLATED && sandboxedApps.isEmpty()) {
                item(key = "isolated_empty") {
                    EmptyFilteredState(title = stringResource(R.string.no_apps_found))
                }
            }

            if (categoryFilter == AppCategoryFilter.ALL) {
                item(key = "all_apps_header") {
                    SectionHeader(
                        icon = Icons.Filled.Apps,
                        title = stringResource(R.string.section_all_apps),
                        count = regularApps.size,
                        color = MaterialTheme.colorScheme.primary,
                        trailing = {
                            FilterChip(
                                selected = showSystemApps,
                                onClick = {
                                    haptics.performToggle(!showSystemApps)
                                    showSystemApps = !showSystemApps
                                },
                                label = {
                                    Text(
                                        text = stringResource(R.string.show_system_apps),
                                        style = MaterialTheme.typography.labelSmallEmphasized,
                                    )
                                },
                                leadingIcon = if (showSystemApps) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                } else {
                                    null
                                },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                                modifier = Modifier.height(30.dp),
                            )
                        },
                    )
                }

                itemsIndexed(
                    items = regularApps,
                    key = { _, app -> "regular_${app.key}" },
                ) { index, app ->
                    val shape = preferenceShape(
                        preferencePosition(index = index, count = regularApps.size),
                    )
                    SegmentedAppCard(
                        app = app,
                        shape = shape,
                        onClick = {
                            quickActionApp = app
                            showBottomSheet = true
                        },
                        onDetailClick = { onOpenAppDetail(app) },
                    )
                }

                if (showSystemApps && systemApps.isNotEmpty()) {
                    item(key = "system_apps_spacer") {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    item(key = "system_apps_header") {
                        SectionHeader(
                            icon = Icons.Default.Security,
                            title = stringResource(R.string.show_system_apps),
                            count = systemApps.size,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    itemsIndexed(
                        items = systemApps,
                        key = { _, app -> "system_${app.key}" },
                    ) { index, app ->
                        val shape = preferenceShape(
                            preferencePosition(index = index, count = systemApps.size),
                        )
                        SegmentedAppCard(
                            app = app,
                            shape = shape,
                            onClick = {
                                quickActionApp = app
                                showBottomSheet = true
                            },
                            onDetailClick = { onOpenAppDetail(app) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFilteredState(title: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(PreferenceTokens.CornerRadiusOuter),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
        )
    }
}

@Composable
private fun SegmentedAppCard(
    app: AppInfo,
    shape: Shape,
    onClick: () -> Unit,
    onDetailClick: () -> Unit,
) {
    val haptics = rememberExpressiveHaptics()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable {
                haptics.performStepTick()
                onClick()
            },
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = app.iconPainter,
                contentDescription = app.label,
                modifier = Modifier.size(44.dp),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (app.isLocked || app.isHidden || app.isSandboxed || app.isLauncherHidden) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (app.isLocked) {
                            ProtectionPillBadge(
                                icon = Icons.Filled.Lock,
                                label = stringResource(R.string.action_lock),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        if (app.isHidden || app.isLauncherHidden) {
                            ProtectionPillBadge(
                                icon = Icons.Filled.VisibilityOff,
                                label = stringResource(R.string.action_hide),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                        if (app.isSandboxed) {
                            ProtectionPillBadge(
                                icon = Icons.Filled.Security,
                                label = stringResource(R.string.action_isolate),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalIconButton(
                onClick = {
                    haptics.performStepTick()
                    onDetailClick()
                },
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ProtectionPillBadge(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmallEmphasized,
            color = contentColor,
        )
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    count: Int,
    color: Color,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = color,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmallEmphasized.withTabularNums(),
                    color = color,
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
private fun CollapsibleSectionHeader(
    icon: ImageVector,
    title: String,
    count: Int,
    color: Color,
    isExpanded: Boolean,
    hasItemsBelow: Boolean,
    onExpandChange: (Boolean) -> Unit,
) {
    val reducedMotion = rememberExpressiveReducedMotion()
    val haptics = rememberExpressiveHaptics()
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = if (reducedMotion) {
            snap()
        } else {
            MaterialTheme.motionScheme.fastSpatialSpec()
        },
        label = "PrivateSectionChevron",
    )

    val headerShape = if (hasItemsBelow) {
        preferenceShape(PreferencePosition.Top)
    } else {
        preferenceShape(PreferencePosition.Single)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(headerShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable {
                val next = !isExpanded
                haptics.performToggle(next)
                onExpandChange(next)
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = color,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier
                .wrapContentSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMediumEmphasized.withTabularNums(),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { rotationZ = rotationAngle },
            )
        }
    }
}

@Composable
private fun AppQuickActionsSheet(
    app: AppInfo,
    onLockToggle: (AppInfo) -> Unit,
    onHideToggle: (AppInfo) -> Unit,
    onLauncherHideToggle: (AppInfo) -> Unit,
    onSandboxToggle: (AppInfo) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onOpenDetail: (AppInfo) -> Unit,
) {
    val context = LocalContext.current
    val sandboxManager = remember {
        context.getSystemService(AxSandboxManager::class.java)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            PreferenceGroup {
                item {
                    ClickablePreference(
                        title = app.label,
                        summary = app.packageName,
                        onClick = { onOpenDetail(app) },
                        customIcon = {
                            Image(
                                painter = app.iconPainter,
                                contentDescription = app.label,
                                modifier = Modifier.size(40.dp),
                            )
                        },
                        showExternalIcon = false,
                    )
                }
                item {
                    BasePreference(
                        title = stringResource(R.string.action_launch),
                        icon = Icons.Default.OpenInNew,
                        widget = {
                            ExpressiveMorphButton(
                                onClick = { onLaunch(app) },
                                selected = true,
                                restingPreset = ExpressiveShapePreset.PILL,
                                pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                                selectedPreset = ExpressiveShapePreset.PILL,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Text(text = stringResource(R.string.action_open))
                            }
                        },
                    )
                }
            }
        }

        item {
            PreferenceGroup {
                item {
                    SwitchPreference(
                        title = stringResource(R.string.action_lock),
                        summary = stringResource(R.string.lock_app_description),
                        checked = app.isLocked,
                        onCheckedChange = { onLockToggle(app) },
                        icon = if (app.isLocked) Icons.Filled.Lock else Icons.Outlined.Lock,
                    )
                }
            }
        }

        item {
            HideSettingsDropdown(
                app = app,
                onHideToggle = onHideToggle,
                onLauncherHideToggle = onLauncherHideToggle,
            )
        }

        item {
            SandboxOptionsDropdown(
                app = app,
                sandboxManager = sandboxManager,
                onSandboxToggle = onSandboxToggle,
            )
        }

        item {
            SpoofSettingsDropdown(
                app = app,
                sandboxManager = sandboxManager,
            )
        }
    }
}

data class HiddenNotification(
    val key: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val timestamp: Long,
    val icon: Drawable? = null,
    val contentIntent: PendingIntent? = null,
    val userId: Int = 0,
)

@Composable
fun NotificationsTab(
    isUnlocked: Boolean = false,
    onUnlockRequest: () -> Unit = {},
) {
    if (!isUnlocked) {
        var morphed by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
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
                    text = stringResource(R.string.notifications_locked_title),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.notifications_locked_description),
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
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf<List<HiddenNotification>>(emptyList()) }

    DisposableEffect(Unit) {
        val sandboxManager =
            context.getSystemService(Context.AX_SANDBOX_SERVICE) as? AxSandboxManager

        val listener = object : IHiddenNotificationListener.Stub() {
            override fun onHiddenNotificationPosted(info: HiddenNotificationInfo) {
                scope.launch(Dispatchers.Main) {
                    val pm = context.packageManager
                    val icon = info.appIcon?.loadDrawable(context) ?: try {
                        pm.getApplicationIcon(info.packageName)
                    } catch (_: Exception) {
                        null
                    }

                    val notif = HiddenNotification(
                        key = info.key,
                        packageName = info.packageName,
                        title = info.title?.toString(),
                        text = info.text?.toString(),
                        timestamp = info.postTime,
                        icon = icon,
                        contentIntent = info.contentIntent,
                        userId = info.userId,
                    )
                    notifications = listOf(notif) + notifications.filter { it.key != info.key }
                }
            }

            override fun onHiddenNotificationRemoved(key: String) {
                scope.launch(Dispatchers.Main) {
                    notifications = notifications.filter { it.key != key }
                }
            }
        }

        try {
            sandboxManager?.registerHiddenNotificationListener(listener)
            scope.launch(Dispatchers.IO) {
                val current = sandboxManager?.hiddenNotifications ?: emptyList()
                withContext(Dispatchers.Main) {
                    val pm = context.packageManager
                    notifications = current.map { info ->
                        val icon = info.appIcon?.loadDrawable(context) ?: try {
                            pm.getApplicationIcon(info.packageName)
                        } catch (_: Exception) {
                            null
                        }

                        HiddenNotification(
                            key = info.key,
                            packageName = info.packageName,
                            title = info.title?.toString(),
                            text = info.text?.toString(),
                            timestamp = info.postTime,
                            icon = icon,
                            contentIntent = info.contentIntent,
                            userId = info.userId,
                        )
                    }.sortedByDescending { it.timestamp }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register notification listener", e)
        }

        onDispose {
            try {
                sandboxManager?.unregisterHiddenNotificationListener(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unregister notification listener", e)
            }
        }
    }

    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
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
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.no_private_notifications_title),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.no_private_notifications_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(PreferenceTokens.ItemSpacing),
        ) {
            itemsIndexed(
                items = notifications,
                key = { _, item -> item.key },
            ) { index, notification ->
                val shape = preferenceShape(
                    preferencePosition(index = index, count = notifications.size),
                )
                NotificationItem(
                    notification = notification,
                    shape = shape,
                    onLaunch = {
                        try {
                            notification.contentIntent?.send()
                                ?: launchApp(context, notification.packageName, notification.userId)
                        } catch (_: Exception) {
                            launchApp(context, notification.packageName, notification.userId)
                        }
                    },
                    onDismiss = {
                        notifications = notifications.filter { it.key != notification.key }
                    },
                )
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: HiddenNotification,
    shape: Shape,
    onLaunch: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val haptics = rememberExpressiveHaptics()

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable {
                haptics.performStepTick()
                onLaunch()
            },
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    if (notification.icon != null) {
                        Icon(
                            painter = rememberDrawablePainter(notification.icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = notification.title ?: notification.packageName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )

                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(Date(notification.timestamp)),
                            style = MaterialTheme.typography.labelSmallEmphasized.withTabularNums(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (!notification.text.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notification.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        haptics.performStepTick()
                        onDismiss()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_dismiss),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                ExpressiveMorphButton(
                    onClick = onLaunch,
                    selected = false,
                    restingPreset = ExpressiveShapePreset.PILL,
                    pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                ) {
                    Text(text = stringResource(R.string.action_open))
                }
            }
        }
    }
}

private suspend fun toggleAppLock(
    context: Context,
    packageName: String,
    userId: Int,
    lock: Boolean,
) = withContext(Dispatchers.IO) {
    try {
        val sandboxManager =
            context.getSystemService(Context.AX_SANDBOX_SERVICE) as? AxSandboxManager
                ?: return@withContext
        if (lock) {
            sandboxManager.addLockedApp(packageName, userId)
        } else {
            sandboxManager.removeLockedApp(packageName, userId)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error toggling app lock for $packageName", e)
    }
}

private suspend fun toggleAppHidden(
    context: Context,
    packageName: String,
    userId: Int,
    hidden: Boolean,
) = withContext(Dispatchers.IO) {
    try {
        val sandboxManager =
            context.getSystemService(Context.AX_SANDBOX_SERVICE) as? AxSandboxManager
                ?: return@withContext
        sandboxManager.setPackageHidden(packageName, hidden, userId)
    } catch (e: Exception) {
        Log.e(TAG, "Error toggling app hidden for $packageName", e)
    }
}

private suspend fun toggleAppLauncherHidden(
    context: Context,
    packageName: String,
    userId: Int,
    hidden: Boolean,
) = withContext(Dispatchers.IO) {
    try {
        val sandboxManager =
            context.getSystemService(Context.AX_SANDBOX_SERVICE) as? AxSandboxManager
                ?: return@withContext
        sandboxManager.setPackageHiddenFromLauncher(packageName, hidden, userId)
    } catch (e: Exception) {
        Log.e(TAG, "Error toggling app launcher hidden for $packageName", e)
    }
}

private suspend fun toggleAppSandboxed(
    context: Context,
    packageName: String,
    userId: Int,
    sandboxed: Boolean,
) = withContext(Dispatchers.IO) {
    try {
        val sandboxManager =
            context.getSystemService(Context.AX_SANDBOX_SERVICE) as? AxSandboxManager
                ?: return@withContext
        if (sandboxed) {
            sandboxManager.addSandboxedPackage(packageName, userId)
        } else {
            sandboxManager.removeSandboxedPackage(packageName, userId)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error toggling app sandboxed for $packageName", e)
    }
}

private fun launchApp(context: Context, packageName: String, userId: Int = 0) {
    try {
        if (userId != 0) {
            val launcherApps = context.getSystemService(LauncherApps::class.java)
            val activities = launcherApps?.getActivityList(packageName, UserHandle.of(userId))
            if (!activities.isNullOrEmpty()) {
                launcherApps.startMainActivity(
                    activities[0].componentName,
                    UserHandle.of(userId),
                    null,
                    null,
                )
                return
            }
        }
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error launching $packageName", e)
    }
}

private object AppIconCache {
    private val cache = LruCache<String, Painter>(1024)

    fun loadIconPainter(
        packageName: String,
        userId: Int,
        icon: Drawable,
        iconDrawableFactory: IconDrawableFactory,
        targetPx: Int,
    ): Painter {
        val key = "${packageName}_$userId"
        val cached = cache.get(key)
        if (cached != null) return cached

        val shadowedDrawable = try {
            iconDrawableFactory.getShadowedIcon(icon)
        } catch (_: Exception) {
            icon
        }

        val bitmapWidth =
            if (shadowedDrawable.intrinsicWidth > 0) shadowedDrawable.intrinsicWidth else targetPx * 2
        val bitmapHeight =
            if (shadowedDrawable.intrinsicHeight > 0) shadowedDrawable.intrinsicHeight else targetPx * 2
        val bitmap = shadowedDrawable.toBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val painter = BitmapPainter(bitmap.asImageBitmap(), filterQuality = FilterQuality.High)
        cache.put(key, painter)
        return painter
    }
}

@Composable
internal fun rememberAppIconPainter(app: AppInfo): Painter = app.iconPainter

@Composable
internal fun rememberDrawablePainter(drawable: Drawable?): Painter = remember(drawable) {
    when (drawable) {
        null -> EmptyPainter
        is BitmapDrawable -> {
            drawable.isFilterBitmap = true
            BitmapPainter(drawable.bitmap.asImageBitmap(), filterQuality = FilterQuality.High)
        }

        is ColorDrawable -> ColorPainter(Color(drawable.color))
        else -> DrawablePainter(drawable.mutate())
    }
}

internal class DrawablePainter(
    val drawable: Drawable,
) : Painter(), RememberObserver {
    private val drawInvalidateTick = mutableStateOf(0)
    private val drawableIntrinsicSize = mutableStateOf(drawable.intrinsicSize)

    private val callback: Drawable.Callback by lazy {
        object : Drawable.Callback {
            override fun invalidateDrawable(d: Drawable) {
                drawInvalidateTick.value++
                drawableIntrinsicSize.value = drawable.intrinsicSize
            }

            override fun scheduleDrawable(d: Drawable, what: Runnable, time: Long) {
                MAIN_HANDLER.postAtTime(what, time)
            }

            override fun unscheduleDrawable(d: Drawable, what: Runnable) {
                MAIN_HANDLER.removeCallbacks(what)
            }
        }
    }

    init {
        if (drawable.intrinsicWidth >= 0 && drawable.intrinsicHeight >= 0) {
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        }
    }

    override fun onRemembered() {
        drawable.callback = callback
        drawable.setVisible(true, true)
        if (drawable is Animatable) drawable.start()
    }

    override fun onAbandoned() = onForgotten()

    override fun onForgotten() {
        if (drawable is Animatable) drawable.stop()
        drawable.setVisible(false, false)
        drawable.callback = null
    }

    override fun applyAlpha(alpha: Float): Boolean {
        drawable.alpha = (alpha * 255).roundToInt().coerceIn(0, 255)
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        drawable.colorFilter = colorFilter?.asAndroidColorFilter()
        return true
    }

    override fun applyLayoutDirection(layoutDirection: LayoutDirection): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            return drawable.setLayoutDirection(
                when (layoutDirection) {
                    LayoutDirection.Ltr -> View.LAYOUT_DIRECTION_LTR
                    LayoutDirection.Rtl -> View.LAYOUT_DIRECTION_RTL
                },
            )
        }
        return false
    }

    override val intrinsicSize: Size get() = drawableIntrinsicSize.value

    override fun DrawScope.onDraw() {
        drawIntoCanvas { canvas ->
            drawInvalidateTick.value

            drawable.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())

            canvas.withSave {
                drawable.draw(canvas.nativeCanvas)
            }
        }
    }
}

private val MAIN_HANDLER by lazy(LazyThreadSafetyMode.NONE) {
    Handler(Looper.getMainLooper())
}

private val Drawable.intrinsicSize: Size
    get() = when {
        intrinsicWidth >= 0 && intrinsicHeight >= 0 -> {
            Size(width = intrinsicWidth.toFloat(), height = intrinsicHeight.toFloat())
        }

        else -> Size.Unspecified
    }

internal object EmptyPainter : Painter() {
    override val intrinsicSize: Size get() = Size.Unspecified
    override fun DrawScope.onDraw() {}
}
