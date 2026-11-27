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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.unit.dp
import com.android.axion.compose.expressive.ExpressiveMorphButton
import com.android.axion.compose.expressive.ExpressiveShapePreset
import com.android.axion.compose.expressive.rememberExpressiveHaptics
import com.android.axion.compose.preferences.ListPreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.scaffold.AxionScaffold
import com.android.axion.sandbox.R
import com.android.axion.sandbox.security.SandboxSecurityManager

@Composable
fun SecurityQuestionSetupScreen(
    onComplete: (question: String, answer: String) -> Unit,
    onSkip: (() -> Unit)? = null,
    onBack: () -> Unit,
) {
    val haptics = rememberExpressiveHaptics()
    var selectedQuestion by remember { mutableStateOf<String?>(null) }
    var answer by remember { mutableStateOf("") }
    val questionOptions = remember {
        SandboxSecurityManager.SECURITY_QUESTIONS.map { it to it }
    }

    AxionScaffold(
        title = stringResource(R.string.recovery_title),
        onBackClick = onBack,
        collapsedByDefault = false,
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.recovery_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            PreferenceGroup {
                item {
                    ListPreference(
                        title = stringResource(R.string.recovery_question_label),
                        summary = stringResource(R.string.recovery_question_placeholder),
                        options = questionOptions,
                        value = selectedQuestion.orEmpty(),
                        icon = Icons.Outlined.HelpOutline,
                        onValueChange = { selectedQuestion = it },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text(stringResource(R.string.recovery_answer_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = selectedQuestion != null,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )

            Spacer(modifier = Modifier.height(28.dp))

            ExpressiveMorphButton(
                onClick = {
                    selectedQuestion?.let { q ->
                        onComplete(q, answer)
                    }
                },
                enabled = selectedQuestion != null && answer.isNotBlank(),
                selected = true,
                restingPreset = ExpressiveShapePreset.PILL,
                pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                selectedPreset = ExpressiveShapePreset.PILL,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text(text = stringResource(R.string.action_continue))
            }

            if (onSkip != null) {
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        haptics.performStepTick()
                        onSkip()
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.action_skip),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
