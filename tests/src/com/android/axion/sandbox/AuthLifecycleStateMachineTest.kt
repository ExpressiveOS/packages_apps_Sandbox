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
package com.android.axion.sandbox

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthLifecycleStateMachineTest {

    @Test(timeout = 5_000)
    fun promptShowingSurvivesOnPauseAndUserLeaveHint() {
        val sm = AuthLifecycleStateMachine()
        assertEquals(AuthLifecycleStateMachine.AuthState.IDLE, sm.state)

        assertTrue(sm.beginBiometricPrompt())
        assertEquals(AuthLifecycleStateMachine.AuthState.PROMPT_SHOWING, sm.state)
        assertFalse("onPause must not cancel auth while BiometricPrompt is showing", sm.shouldCancelOnPause())
        assertFalse("onUserLeaveHint must not cancel auth while BiometricPrompt is showing", sm.shouldCancelOnUserLeaveHint())
        assertFalse("duplicate beginBiometricPrompt while already showing must be ignored", sm.beginBiometricPrompt())
    }

    @Test(timeout = 5_000)
    fun idleStateCancelsOnPauseAndUserLeaveHint() {
        val sm = AuthLifecycleStateMachine()
        assertTrue(sm.shouldCancelOnPause())
        assertTrue(sm.shouldCancelOnUserLeaveHint())
    }

    @Test(timeout = 5_000)
    fun biometricSuccessClearsPromptShowingAndAllowsFinish() {
        val sm = AuthLifecycleStateMachine()
        assertTrue(sm.beginBiometricPrompt())

        assertTrue(sm.onBiometricSucceeded())
        assertEquals(AuthLifecycleStateMachine.AuthState.IDLE, sm.state)
        assertTrue(sm.markFinished())
        assertEquals(AuthLifecycleStateMachine.AuthState.FINISHED, sm.state)
        assertFalse(sm.shouldCancelOnPause())
        assertFalse(sm.markFinished())
    }

    @Test(timeout = 5_000)
    fun biometricFailureClearsPromptShowingAndAllowsRetrySuccess() {
        val sm = AuthLifecycleStateMachine()
        assertTrue(sm.beginBiometricPrompt())
        assertFalse(sm.shouldCancelOnPause())

        sm.onBiometricFailed()
        assertEquals(AuthLifecycleStateMachine.AuthState.IDLE, sm.state)

        // Subsequent retry on the still-open prompt can still succeed cleanly.
        assertTrue(sm.onBiometricSucceeded())
        assertTrue(sm.markFinished())
        assertEquals(AuthLifecycleStateMachine.AuthState.FINISHED, sm.state)
    }

    @Test(timeout = 5_000)
    fun biometricFallbackErrorClearsPromptShowingWithoutCancellingActivity() {
        val sm = AuthLifecycleStateMachine()
        assertTrue(sm.beginBiometricPrompt())

        val shouldCancel = sm.onBiometricError(stayOnCredentialScreen = true)
        assertFalse(shouldCancel)
        assertEquals(AuthLifecycleStateMachine.AuthState.IDLE, sm.state)
        // Once back on credential screen in IDLE, a real pause/home navigation cancels cleanly.
        assertTrue(sm.shouldCancelOnPause())
    }

    @Test(timeout = 5_000)
    fun fatalBiometricErrorClearsPromptShowingAndCancelsActivity() {
        val sm = AuthLifecycleStateMachine()
        assertTrue(sm.beginBiometricPrompt())

        val shouldCancel = sm.onBiometricError(stayOnCredentialScreen = false)
        assertTrue(shouldCancel)
        assertEquals(AuthLifecycleStateMachine.AuthState.IDLE, sm.state)
    }
}
