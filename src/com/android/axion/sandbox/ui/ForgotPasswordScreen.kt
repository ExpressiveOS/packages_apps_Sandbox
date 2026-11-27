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
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.sandbox.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.android.axion.compose.expressive.ExpressiveMorphButton
import com.android.axion.compose.expressive.ExpressiveMorphContainer
import com.android.axion.compose.expressive.ExpressiveShapePreset
import com.android.axion.compose.expressive.rememberExpressiveHaptics
import com.android.axion.compose.preferences.BasePreference
import com.android.axion.compose.preferences.ExpressiveShapes
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.dialogWidth
import com.android.axion.compose.scaffold.AxionScaffold
import com.android.axion.sandbox.R

@Composable
fun ForgotPasswordScreen(
    securityQuestion: String,
    onAnswerSubmit: (String) -> Boolean,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    val haptics = rememberExpressiveHaptics()
    var answer by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    AxionScaffold(
        title = stringResource(R.string.forgot_password_title),
        onBackClick = onBack,
        collapsedByDefault = false,
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.forgot_password_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            PreferenceGroup {
                item {
                    BasePreference(
                        title = stringResource(R.string.recovery_question_label),
                        summary = securityQuestion,
                        icon = Icons.Outlined.HelpOutline,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = answer,
                onValueChange = {
                    answer = it
                    isError = false
                },
                label = { Text(stringResource(R.string.recovery_answer_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isError,
                supportingText = if (isError) {
                    { Text(stringResource(R.string.forgot_password_error)) }
                } else {
                    null
                },
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            ExpressiveMorphButton(
                onClick = {
                    if (onAnswerSubmit(answer)) {
                        haptics.performConfirm()
                        showConfirmDialog = true
                    } else {
                        haptics.performBoundaryBump()
                        isError = true
                    }
                },
                enabled = answer.isNotBlank(),
                selected = true,
                restingPreset = ExpressiveShapePreset.PILL,
                pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                selectedPreset = ExpressiveShapePreset.PILL,
                hapticOnClick = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text(text = stringResource(R.string.forgot_password_verify))
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            shape = ExpressiveShapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.width(dialogWidth()),
            icon = {
                ExpressiveMorphContainer(
                    active = true,
                    inactivePreset = ExpressiveShapePreset.COOKIE_7,
                    activePreset = ExpressiveShapePreset.COOKIE_7,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.forgot_password_reset_title),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.forgot_password_reset_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            },
            confirmButton = {
                ExpressiveMorphButton(
                    onClick = {
                        showConfirmDialog = false
                        onSuccess()
                    },
                    selected = true,
                    restingPreset = ExpressiveShapePreset.PILL,
                    pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                    selectedPreset = ExpressiveShapePreset.PILL,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Text(text = stringResource(R.string.action_reset))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        haptics.performStepTick()
                        showConfirmDialog = false
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
}
