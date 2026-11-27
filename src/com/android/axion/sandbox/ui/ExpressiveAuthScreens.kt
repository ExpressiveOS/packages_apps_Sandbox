/*
 * Copyright (C) 2025-2026 ExpressiveOS
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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Pattern
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.axion.compose.expressive.ExpressiveBlurSurface
import com.android.axion.compose.expressive.ExpressiveMorphButton
import com.android.axion.compose.expressive.ExpressiveMorphContainer
import com.android.axion.compose.expressive.ExpressivePolygons
import com.android.axion.compose.expressive.ExpressiveShapePreset
import com.android.axion.compose.expressive.MorphPolygonShape
import com.android.axion.compose.expressive.rememberExpressiveHaptics
import com.android.axion.compose.expressive.rememberExpressiveMorph
import com.android.axion.compose.expressive.rememberExpressiveReducedMotion
import com.android.axion.compose.preferences.PreferenceTokens
import com.android.axion.compose.scaffold.ExpressiveBackButton
import com.android.axion.compose.theme.withTabularNums
import com.android.axion.sandbox.R
import com.android.axion.sandbox.security.SandboxSecurityManager.BiometricType
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.delay

@Composable
private fun AuthShell(
    title: String,
    subtitle: String,
    isError: Boolean,
    icon: ImageVector,
    frostedSheet: Boolean,
    onBack: (() -> Unit)?,
    onForgotPassword: (() -> Unit)?,
    shakeTranslation: Float = 0f,
    content: @Composable () -> Unit,
) {
    val body: @Composable () -> Unit = {
        var emblemActive by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ExpressiveMorphContainer(
                active = emblemActive,
                inactivePreset = ExpressiveShapePreset.COOKIE_7,
                activePreset = ExpressiveShapePreset.SOFT_BURST,
                containerColor = if (isError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                contentColor = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                onClick = { emblemActive = !emblemActive },
                modifier = Modifier.size(72.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { translationX = shakeTranslation },
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            content()

            if (onForgotPassword != null) {
                Spacer(modifier = Modifier.height(18.dp))
                TextButton(onClick = onForgotPassword) {
                    Text(
                        text = stringResource(R.string.action_forgot_password),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (frostedSheet) {
                    Modifier
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surface)
                },
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        if (onBack != null) {
            ExpressiveBackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 8.dp),
            )
        }

        if (frostedSheet) {
            ExpressiveBlurSurface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .widthIn(max = 440.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(PreferenceTokens.CornerRadiusSheet),
                tintColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentAlignment = Alignment.Center,
            ) {
                body()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (onBack != null) 48.dp else 0.dp),
                contentAlignment = Alignment.Center,
            ) {
                body()
            }
        }
    }
}

@Composable
fun ExpressivePinScreen(
    isSetup: Boolean = false,
    promptText: String? = null,
    onUnlock: () -> Unit,
    onPinEntered: (String) -> Boolean,
    confirmPin: String? = null,
    onBack: (() -> Unit)? = null,
    biometricType: BiometricType = BiometricType.NONE,
    onBiometricClick: () -> Unit = {},
    onForgotPassword: (() -> Unit)? = null,
    frostedSheet: Boolean = false,
) {
    val reducedMotion = rememberExpressiveReducedMotion()
    val haptics = rememberExpressiveHaptics()
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val errorMismatch = stringResource(R.string.pin_error_mismatch)
    val errorIncorrect = stringResource(R.string.pin_error_incorrect)

    LaunchedEffect(confirmPin) {
        enteredPin = ""
        isError = false
    }

    val shakeProgress by animateFloatAsState(
        targetValue = if (isError && !reducedMotion) 1f else 0f,
        animationSpec = if (reducedMotion) {
            snap()
        } else {
            MaterialTheme.motionScheme.fastSpatialSpec()
        },
        label = "SandboxPinShake",
    )
    val shakeTranslation = if (isError && !reducedMotion) {
        sin(shakeProgress * 4f * PI.toFloat()) * 18f
    } else {
        0f
    }

    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 4) {
            if (!reducedMotion) delay(90)
            if (onPinEntered(enteredPin)) {
                haptics.performConfirm()
                enteredPin = ""
                onUnlock()
            } else {
                haptics.performBoundaryBump()
                errorMessage = if (isSetup && confirmPin != null) errorMismatch else errorIncorrect
                isError = true
                delay(if (reducedMotion) 120L else 380L)
                isError = false
                enteredPin = ""
            }
        }
    }

    val titleText = when {
        !isSetup -> stringResource(R.string.pin_title_enter)
        confirmPin == null -> stringResource(R.string.pin_title_create)
        else -> stringResource(R.string.pin_title_confirm)
    }
    val subtitleText = when {
        isError -> errorMessage
        promptText != null -> promptText
        !isSetup -> stringResource(R.string.pin_prompt_unlock)
        confirmPin == null -> stringResource(R.string.pin_prompt_create)
        else -> stringResource(R.string.pin_prompt_confirm)
    }

    AuthShell(
        title = titleText,
        subtitle = subtitleText,
        isError = isError,
        icon = if (isSetup) Icons.Rounded.Pin else Icons.Rounded.Lock,
        frostedSheet = frostedSheet,
        onBack = onBack,
        onForgotPassword = if (!isSetup) onForgotPassword else null,
        shakeTranslation = shakeTranslation,
    ) {
        val dotMorph = rememberExpressiveMorph(
            start = ExpressivePolygons.Squircle,
            end = ExpressivePolygons.Cookie7,
        )

        Row(
            modifier = Modifier
                .graphicsLayer { translationX = shakeTranslation }
                .padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(4) { index ->
                val filled = index < enteredPin.length
                val progress by animateFloatAsState(
                    targetValue = if (filled) 1f else 0f,
                    animationSpec = if (reducedMotion) {
                        snap()
                    } else {
                        MaterialTheme.motionScheme.fastSpatialSpec()
                    },
                    label = "SandboxPinDot_$index",
                )
                val dotColor by animateColorAsState(
                    targetValue = when {
                        isError -> MaterialTheme.colorScheme.error
                        filled -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                    animationSpec = if (reducedMotion) {
                        snap()
                    } else {
                        MaterialTheme.motionScheme.fastEffectsSpec()
                    },
                    label = "SandboxPinDotColor_$index",
                )
                val dotShape = remember(dotMorph, progress) {
                    MorphPolygonShape(morph = dotMorph, progress = progress)
                }
                Box(
                    modifier = Modifier
                        .size(if (filled) 22.dp else 14.dp)
                        .clip(dotShape)
                        .background(dotColor),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
            ).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    row.forEach { digit ->
                        ExpressivePinKey(
                            digit = digit,
                            onClick = {
                                if (enteredPin.length < 4 && !isError) {
                                    haptics.performStepTick()
                                    enteredPin += digit
                                }
                            },
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!isSetup && biometricType != BiometricType.NONE) {
                    ExpressiveMorphContainer(
                        active = false,
                        inactivePreset = ExpressiveShapePreset.SQUIRCLE,
                        activePreset = ExpressiveShapePreset.PILL,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = onBiometricClick,
                        modifier = Modifier.size(72.dp),
                    ) {
                        Icon(
                            imageVector = if (biometricType == BiometricType.FACE) {
                                Icons.Rounded.Face
                            } else {
                                Icons.Rounded.Fingerprint
                            },
                            contentDescription = stringResource(R.string.action_biometric_unlock),
                            modifier = Modifier.size(28.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(72.dp))
                }

                ExpressivePinKey(
                    digit = "0",
                    onClick = {
                        if (enteredPin.length < 4 && !isError) {
                            haptics.performStepTick()
                            enteredPin += "0"
                        }
                    },
                )

                ExpressiveMorphContainer(
                    active = false,
                    inactivePreset = ExpressiveShapePreset.SQUIRCLE,
                    activePreset = ExpressiveShapePreset.PILL,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    hapticOnClick = false,
                    onClick = {
                        if (enteredPin.isNotEmpty() && !isError) {
                            haptics.performStepTick()
                            enteredPin = enteredPin.dropLast(1)
                        }
                    },
                    modifier = Modifier.size(72.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = stringResource(R.string.action_backspace),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpressivePinKey(
    digit: String,
    onClick: () -> Unit,
) {
    ExpressiveMorphContainer(
        active = false,
        inactivePreset = ExpressiveShapePreset.SQUIRCLE,
        activePreset = ExpressiveShapePreset.PILL,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        hapticOnClick = false,
        onClick = onClick,
        modifier = Modifier.size(72.dp),
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMediumEmphasized.withTabularNums(),
        )
    }
}

@Composable
fun ExpressivePasswordScreen(
    isSetup: Boolean = false,
    promptText: String? = null,
    onUnlock: () -> Unit,
    onPasswordEntered: (String) -> Boolean,
    confirmPassword: String? = null,
    onBack: (() -> Unit)? = null,
    biometricType: BiometricType = BiometricType.NONE,
    onBiometricClick: () -> Unit = {},
    onForgotPassword: (() -> Unit)? = null,
    requestInitialFocus: Boolean = true,
    frostedSheet: Boolean = false,
) {
    val reducedMotion = rememberExpressiveReducedMotion()
    val haptics = rememberExpressiveHaptics()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val errorMismatch = stringResource(R.string.password_error_mismatch)
    val errorIncorrect = stringResource(R.string.password_error_incorrect)

    LaunchedEffect(confirmPassword) {
        password = ""
        isError = false
    }

    LaunchedEffect(requestInitialFocus) {
        if (requestInitialFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val shakeProgress by animateFloatAsState(
        targetValue = if (isError && !reducedMotion) 1f else 0f,
        animationSpec = if (reducedMotion) {
            snap()
        } else {
            MaterialTheme.motionScheme.fastSpatialSpec()
        },
        label = "SandboxPasswordShake",
    )
    val shakeTranslation = if (isError && !reducedMotion) {
        sin(shakeProgress * 4f * PI.toFloat()) * 16f
    } else {
        0f
    }

    val submit: () -> Unit = {
        if (password.length >= 4) {
            if (onPasswordEntered(password)) {
                haptics.performConfirm()
                password = ""
                onUnlock()
            } else {
                haptics.performBoundaryBump()
                errorMessage = if (isSetup && confirmPassword != null) errorMismatch else errorIncorrect
                isError = true
            }
        }
    }

    val titleText = when {
        !isSetup -> stringResource(R.string.password_title_enter)
        confirmPassword == null -> stringResource(R.string.password_title_create)
        else -> stringResource(R.string.password_title_confirm)
    }
    val subtitleText = when {
        isError -> errorMessage
        promptText != null -> promptText
        !isSetup -> stringResource(R.string.password_prompt_unlock)
        confirmPassword == null -> stringResource(R.string.password_prompt_create)
        else -> stringResource(R.string.password_prompt_confirm)
    }

    AuthShell(
        title = titleText,
        subtitle = subtitleText,
        isError = isError,
        icon = if (isSetup) Icons.Rounded.Password else Icons.Rounded.Lock,
        frostedSheet = frostedSheet,
        onBack = onBack,
        onForgotPassword = if (!isSetup) onForgotPassword else null,
        shakeTranslation = shakeTranslation,
    ) {
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (isError) isError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .graphicsLayer { translationX = shakeTranslation },
            label = { Text(stringResource(R.string.password_label)) },
            placeholder = { Text(stringResource(R.string.password_placeholder)) },
            singleLine = true,
            isError = isError,
            shape = CircleShape,
            visualTransformation = if (visible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            trailingIcon = {
                IconButton(
                    onClick = {
                        haptics.performStepTick()
                        visible = !visible
                    },
                ) {
                    Icon(
                        imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = stringResource(
                            if (visible) R.string.action_hide_password else R.string.action_show_password,
                        ),
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        )

        Spacer(modifier = Modifier.height(20.dp))

        val ctaLabel = when {
            !isSetup -> stringResource(R.string.action_continue)
            confirmPassword == null -> stringResource(R.string.action_continue)
            else -> stringResource(R.string.action_set_password)
        }

        ExpressiveMorphButton(
            onClick = submit,
            enabled = password.length >= 4,
            selected = true,
            restingPreset = ExpressiveShapePreset.PILL,
            pressedPreset = ExpressiveShapePreset.SQUIRCLE,
            selectedPreset = ExpressiveShapePreset.PILL,
            hapticOnClick = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = ctaLabel)
        }

        if (!isSetup && biometricType != BiometricType.NONE) {
            Spacer(modifier = Modifier.height(14.dp))
            ExpressiveMorphButton(
                onClick = onBiometricClick,
                selected = false,
                restingPreset = ExpressiveShapePreset.PILL,
                pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                leadingIcon = {
                    Icon(
                        imageVector = if (biometricType == BiometricType.FACE) {
                            Icons.Rounded.Face
                        } else {
                            Icons.Rounded.Fingerprint
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            ) {
                Text(text = stringResource(R.string.action_biometric_unlock))
            }
        }
    }
}

@Composable
fun ExpressivePatternScreen(
    isSetup: Boolean = false,
    promptText: String? = null,
    onUnlock: () -> Unit,
    onPatternEntered: (List<Int>) -> Boolean,
    confirmPattern: List<Int>? = null,
    onBack: (() -> Unit)? = null,
    biometricType: BiometricType = BiometricType.NONE,
    onBiometricClick: () -> Unit = {},
    onForgotPassword: (() -> Unit)? = null,
    frostedSheet: Boolean = false,
) {
    val reducedMotion = rememberExpressiveReducedMotion()
    val haptics = rememberExpressiveHaptics()
    val density = LocalDensity.current

    var selectedDots by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentTouch by remember { mutableStateOf<Offset?>(null) }
    var dotPositions by remember { mutableStateOf<Map<Int, Offset>>(emptyMap()) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val errorMismatch = stringResource(R.string.pattern_error_mismatch)
    val errorIncorrect = stringResource(R.string.pattern_error_incorrect)
    val errorTooShort = stringResource(R.string.pattern_error_too_short)

    LaunchedEffect(confirmPattern) {
        selectedDots = emptyList()
        isError = false
    }

    val shakeProgress by animateFloatAsState(
        targetValue = if (isError && !reducedMotion) 1f else 0f,
        animationSpec = if (reducedMotion) {
            snap()
        } else {
            MaterialTheme.motionScheme.fastSpatialSpec()
        },
        label = "SandboxPatternShake",
    )
    val shakeTranslation = if (isError && !reducedMotion) {
        sin(shakeProgress * 4f * PI.toFloat()) * 16f
    } else {
        0f
    }

    val dotRadius = with(density) { 10.dp.toPx() }
    val touchRadius = with(density) { 38.dp.toPx() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant

    val titleText = when {
        !isSetup -> stringResource(R.string.pattern_title_enter)
        confirmPattern == null -> stringResource(R.string.pattern_title_create)
        else -> stringResource(R.string.pattern_title_confirm)
    }
    val subtitleText = when {
        isError -> errorMessage
        promptText != null -> promptText
        !isSetup -> stringResource(R.string.pattern_prompt_unlock)
        confirmPattern == null -> stringResource(R.string.pattern_prompt_create)
        else -> stringResource(R.string.pattern_prompt_confirm)
    }

    AuthShell(
        title = titleText,
        subtitle = subtitleText,
        isError = isError,
        icon = if (isSetup) Icons.Rounded.Pattern else Icons.Rounded.Lock,
        frostedSheet = frostedSheet,
        onBack = onBack,
        onForgotPassword = if (!isSetup) onForgotPassword else null,
        shakeTranslation = shakeTranslation,
    ) {
        Box(
            modifier = Modifier
                .size(272.dp)
                .graphicsLayer { translationX = shakeTranslation }
                .clip(RoundedCornerShape(PreferenceTokens.CornerRadiusOuter))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(PreferenceTokens.CornerRadiusOuter),
                )
                .pointerInput(confirmPattern) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isError = false
                            errorMessage = ""
                            selectedDots = emptyList()
                            currentTouch = offset
                            val hit = locatePatternDot(offset, dotPositions, touchRadius)
                            if (hit != null) {
                                selectedDots = listOf(hit)
                                haptics.performStepTick()
                            }
                        },
                        onDrag = { change, _ ->
                            currentTouch = change.position
                            val hit = locatePatternDot(change.position, dotPositions, touchRadius)
                            if (hit != null && !selectedDots.contains(hit)) {
                                selectedDots = selectedDots + hit
                                haptics.performStepTick()
                            }
                        },
                        onDragEnd = {
                            currentTouch = null
                            if (selectedDots.isEmpty()) return@detectDragGestures
                            if (selectedDots.size < 4) {
                                haptics.performBoundaryBump()
                                errorMessage = errorTooShort
                                isError = true
                                return@detectDragGestures
                            }
                            if (onPatternEntered(selectedDots)) {
                                haptics.performConfirm()
                                selectedDots = emptyList()
                                onUnlock()
                            } else {
                                haptics.performBoundaryBump()
                                errorMessage = if (isSetup && confirmPattern != null) {
                                    errorMismatch
                                } else {
                                    errorIncorrect
                                }
                                isError = true
                            }
                        },
                        onDragCancel = {
                            currentTouch = null
                            selectedDots = emptyList()
                        },
                    )
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / 3f
                val positions = mutableMapOf<Int, Offset>()
                for (row in 0 until 3) {
                    for (col in 0 until 3) {
                        val idx = row * 3 + col
                        positions[idx] = Offset(
                            x = col * cellSize + cellSize / 2f,
                            y = row * cellSize + cellSize / 2f,
                        )
                    }
                }
                dotPositions = positions

                val lineColor = if (isError) errorColor else primaryColor
                if (selectedDots.size > 1) {
                    for (i in 0 until selectedDots.size - 1) {
                        val from = positions[selectedDots[i]]
                        val to = positions[selectedDots[i + 1]]
                        if (from != null && to != null) {
                            drawLine(
                                color = lineColor,
                                start = from,
                                end = to,
                                strokeWidth = 7.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }

                val touch = currentTouch
                if (selectedDots.isNotEmpty() && touch != null) {
                    positions[selectedDots.last()]?.let { last ->
                        drawLine(
                            color = lineColor.copy(alpha = 0.45f),
                            start = last,
                            end = touch,
                            strokeWidth = 7.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }

                positions.forEach { (idx, center) ->
                    val active = selectedDots.contains(idx)
                    val color = when {
                        isError && active -> errorColor
                        active -> primaryColor
                        else -> inactiveColor
                    }
                    if (active) {
                        drawCircle(
                            color = color.copy(alpha = 0.22f),
                            radius = dotRadius * 2.4f,
                            center = center,
                        )
                    }
                    drawCircle(
                        color = color,
                        radius = if (active) dotRadius * 1.3f else dotRadius * 0.9f,
                        center = center,
                    )
                }
            }
        }

        if (!isSetup && biometricType != BiometricType.NONE) {
            Spacer(modifier = Modifier.height(18.dp))
            ExpressiveMorphButton(
                onClick = onBiometricClick,
                selected = false,
                restingPreset = ExpressiveShapePreset.PILL,
                pressedPreset = ExpressiveShapePreset.SQUIRCLE,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                leadingIcon = {
                    Icon(
                        imageVector = if (biometricType == BiometricType.FACE) {
                            Icons.Rounded.Face
                        } else {
                            Icons.Rounded.Fingerprint
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            ) {
                Text(text = stringResource(R.string.action_biometric_unlock))
            }
        }
    }
}

private fun locatePatternDot(
    touch: Offset,
    dotPositions: Map<Int, Offset>,
    touchRadius: Float,
): Int? {
    for ((idx, pos) in dotPositions) {
        val dist = sqrt((touch.x - pos.x).pow(2) + (touch.y - pos.y).pow(2))
        if (dist <= touchRadius) return idx
    }
    return null
}
