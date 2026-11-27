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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.android.axion.compose.expressive.ExpressiveAlertDialog
import com.android.axion.compose.expressive.ExpressiveMorphButton
import com.android.axion.compose.expressive.ExpressiveShapePreset
import com.android.axion.compose.expressive.rememberExpressiveHaptics
import com.android.axion.compose.preferences.BasePreference
import com.android.axion.compose.preferences.PreferenceGroup
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
                .imePadding()
                .verticalScroll(rememberScrollState())
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
                        summary = localizedSecurityQuestion(securityQuestion),
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
                    .defaultMinSize(minHeight = 54.dp),
            ) {
                Text(text = stringResource(R.string.forgot_password_verify))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showConfirmDialog) {
        ExpressiveAlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = stringResource(R.string.forgot_password_reset_title),
            text = stringResource(R.string.forgot_password_reset_message),
            confirmText = stringResource(R.string.action_reset),
            onConfirm = {
                showConfirmDialog = false
                onSuccess()
            },
            dismissText = stringResource(R.string.action_cancel),
            onDismiss = { showConfirmDialog = false },
            icon = Icons.Filled.Warning,
            destructive = true,
        )
    }
}
