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

/**
 * Pure lifecycle state machine for [AuthenticateActivity] coordinating system BiometricPrompt
 * visibility with Activity pause/leave callbacks.
 *
 * While [AuthState.PROMPT_SHOWING] is active, [shouldCancelOnPause] and
 * [shouldCancelOnUserLeaveHint] return `false` so launching the system biometric sheet never
 * aborts authentication and kicks the user back to HOME.
 */
internal class AuthLifecycleStateMachine {

    enum class AuthState { IDLE, PROMPT_SHOWING, EXITING, FINISHED }

    var state: AuthState = AuthState.IDLE
        private set

    fun beginBiometricPrompt(): Boolean {
        if (state != AuthState.IDLE) return false
        state = AuthState.PROMPT_SHOWING
        return true
    }

    fun onBiometricNegativeButton() {
        if (state == AuthState.PROMPT_SHOWING) {
            state = AuthState.IDLE
        }
    }

    fun onBiometricSucceeded(): Boolean {
        if (state == AuthState.FINISHED) return false
        if (state == AuthState.PROMPT_SHOWING) {
            state = AuthState.IDLE
        }
        return true
    }

    fun onBiometricFailed() {
        if (state == AuthState.PROMPT_SHOWING) {
            state = AuthState.IDLE
        }
    }

    /**
     * Handles a terminal biometric error callback.
     *
     * @return `true` if the host activity should cancel and finish, or `false` if it should remain
     *   open on the credential fallback screen.
     */
    fun onBiometricError(stayOnCredentialScreen: Boolean): Boolean {
        if (state == AuthState.FINISHED) return false
        if (state == AuthState.PROMPT_SHOWING) {
            state = AuthState.IDLE
        }
        return !stayOnCredentialScreen
    }

    fun shouldCancelOnPause(): Boolean =
        state != AuthState.FINISHED && state != AuthState.PROMPT_SHOWING

    fun shouldCancelOnUserLeaveHint(): Boolean =
        state == AuthState.IDLE

    fun markFinished(): Boolean {
        if (state == AuthState.FINISHED) return false
        state = AuthState.FINISHED
        return true
    }
}
