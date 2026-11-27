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
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.sandbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Pattern
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.axion.compose.expressive.ExpressiveMorphContainer
import com.android.axion.compose.expressive.ExpressiveShapePreset
import com.android.axion.compose.expressive.rememberExpressiveHaptics
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.sandbox.R
import com.android.axion.sandbox.security.SecurityType

@Composable
fun SecuritySetupScreen(
    onSecurityTypeSelected: (SecurityType) -> Unit,
    onSkip: () -> Unit = {},
    showSkip: Boolean = true,
) {
    val haptics = rememberExpressiveHaptics()
    var emblemActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .widthIn(max = 420.dp),
        ) {
            ExpressiveMorphContainer(
                active = emblemActive,
                inactivePreset = ExpressiveShapePreset.COOKIE_7,
                activePreset = ExpressiveShapePreset.SOFT_BURST,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { emblemActive = !emblemActive },
                modifier = Modifier.size(84.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.headlineMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.setup_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(36.dp))

            PreferenceGroup {
                item {
                    ClickablePreference(
                        title = stringResource(R.string.setup_pin_title),
                        summary = stringResource(R.string.setup_pin_description),
                        icon = Icons.Outlined.Pin,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = {
                            haptics.performConfirm()
                            onSecurityTypeSelected(SecurityType.PIN)
                        },
                    )
                }
                item {
                    ClickablePreference(
                        title = stringResource(R.string.setup_password_title),
                        summary = stringResource(R.string.setup_password_description),
                        icon = Icons.Outlined.Password,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {
                            haptics.performConfirm()
                            onSecurityTypeSelected(SecurityType.PASSWORD)
                        },
                    )
                }
                item {
                    ClickablePreference(
                        title = stringResource(R.string.setup_pattern_title),
                        summary = stringResource(R.string.setup_pattern_description),
                        icon = Icons.Outlined.Pattern,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = {
                            haptics.performConfirm()
                            onSecurityTypeSelected(SecurityType.PATTERN)
                        },
                    )
                }
            }

            if (showSkip) {
                Spacer(modifier = Modifier.height(24.dp))

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
        }
    }
}
