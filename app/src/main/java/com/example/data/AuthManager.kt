// © 2026 App Owner. All rights reserved.
package com.example.data

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.ui.screens.AuthMode
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.abs

sealed class AuthValidationResult {
    object Allowed : AuthValidationResult()
    data class Rejected(
        val message: String,
        val deleteAutoCreatedUser: Boolean,
        val suggestedMode: AuthMode
    ) : AuthValidationResult()
}

object AuthManager {
    private const val TAG = "Auth"
    private val _verificationInProgress = MutableStateFlow(false)

    private fun getAuthOrNull(): FirebaseAuth? {
        return try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }
    }

    fun currentUserOrNull(): FirebaseUser? =
        if (_verificationInProgress.value) null else getAuthOrNull()?.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val auth = getAuthOrNull()
        if (auth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        fun emitVerifiedUser() {
            trySend(if (_verificationInProgress.value) null else auth.currentUser)
        }

        emitVerifiedUser()
        val listener = FirebaseAuth.AuthStateListener {
            emitVerifiedUser()
        }
        auth.addAuthStateListener(listener)
        val verifierJob = launch {
            _verificationInProgress.collect {
                emitVerifiedUser()
            }
        }
        awaitClose {
            verifierJob.cancel()
            auth.removeAuthStateListener(listener)
        }
    }

    internal fun validateAuthModeForUserStatus(
        mode: AuthMode,
        isNewUser: Boolean
    ): AuthValidationResult {
        return when {
            mode == AuthMode.SIGN_IN && isNewUser -> AuthValidationResult.Rejected(
                message = "No account found for this Google account. Please Sign Up first before signing in.",
                deleteAutoCreatedUser = true,
                suggestedMode = AuthMode.SIGN_UP
            )
            mode == AuthMode.SIGN_UP && !isNewUser -> AuthValidationResult.Rejected(
                message = "This Google account is already registered. Please use Sign In instead.",
                deleteAutoCreatedUser = false,
                suggestedMode = AuthMode.SIGN_IN
            )
            else -> AuthValidationResult.Allowed
        }
    }

    private fun isNewlyCreatedUser(authResult: AuthResult): Boolean {
        val additionalFlag = authResult.additionalUserInfo?.isNewUser
        if (additionalFlag != null) return additionalFlag
        val meta = authResult.user?.metadata ?: return false
        return abs(meta.lastSignInTimestamp - meta.creationTimestamp) < 5_000L
    }

    private suspend fun cleanupRejectedSession(
        auth: FirebaseAuth,
        authResult: AuthResult,
        credentialManager: CredentialManager,
        deleteAutoCreatedUser: Boolean
    ) {
        withContext(NonCancellable) {
            try {
                if (deleteAutoCreatedUser) {
                    try {
                        authResult.user?.delete()?.await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete auto-created unverified user: ${e.message}")
                    }
                }
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clear credential state: ${e.message}")
                }
            } finally {
                auth.signOut()
            }
        }
    }

    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onAuthSuccess: () -> Unit = {},
        onUnauthenticated: () -> Unit = {}
    ) {
        val auth = getAuthOrNull()
        if (auth == null) {
            onUnauthenticated()
            return
        }
        if (auth.currentUser != null && !_verificationInProgress.value) {
            onAuthSuccess()
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            _verificationInProgress.value = true
            try {
                val activityOrContext = context.findActivity() ?: context
                val result = credentialManager.getCredential(activityOrContext, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val isNewUser = isNewlyCreatedUser(authResult)
                    if (isNewUser) {
                        cleanupRejectedSession(
                            auth = auth,
                            authResult = authResult,
                            credentialManager = credentialManager,
                            deleteAutoCreatedUser = true
                        )
                        _verificationInProgress.value = false
                        onUnauthenticated()
                    } else {
                        _verificationInProgress.value = false
                        onAuthSuccess()
                    }
                } else {
                    _verificationInProgress.value = false
                    onUnauthenticated()
                }
            } catch (_: Exception) {
                _verificationInProgress.value = false
                onUnauthenticated()
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        mode: AuthMode,
        onAuthSuccess: () -> Unit,
        onAuthError: (String, AuthMode?) -> Unit,
        onAuthCancelled: () -> Unit = {}
    ) {
        val auth = getAuthOrNull()
        if (auth == null) {
            onAuthError("Firebase Authentication is not initialized.", null)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            onAuthError("Google Sign-In configuration missing: default_web_client_id not found.", null)
            return
        }

        val activity = context.findActivity()
        if (activity == null) {
            onAuthError("Unable to launch Google Sign-In from this context.", null)
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            _verificationInProgress.value = true
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val isNewUser = isNewlyCreatedUser(authResult)

                    when (val validation = validateAuthModeForUserStatus(mode, isNewUser)) {
                        is AuthValidationResult.Allowed -> {
                            _verificationInProgress.value = false
                            onAuthSuccess()
                        }
                        is AuthValidationResult.Rejected -> {
                            cleanupRejectedSession(
                                auth = auth,
                                authResult = authResult,
                                credentialManager = credentialManager,
                                deleteAutoCreatedUser = validation.deleteAutoCreatedUser
                            )
                            _verificationInProgress.value = false
                            onAuthError(validation.message, validation.suggestedMode)
                        }
                    }
                } else {
                    _verificationInProgress.value = false
                    onAuthError("Unexpected credential type returned.", null)
                }
            } catch (e: GetCredentialCancellationException) {
                _verificationInProgress.value = false
                Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                onAuthCancelled()
            } catch (e: NoCredentialException) {
                _verificationInProgress.value = false
                Log.w(TAG, "No Google credentials found on device: ${e.message}", e)
                onAuthError(
                    "No Google account found on this device. Please sign in to a Google account in device Settings and try again.",
                    null
                )
            } catch (e: GetCredentialProviderConfigurationException) {
                _verificationInProgress.value = false
                Log.e(TAG, "Credential provider configuration error", e)
                onAuthError(
                    "Google Play Services Credential Manager is unavailable or outdated on this device.",
                    null
                )
            } catch (e: GetCredentialUnsupportedException) {
                _verificationInProgress.value = false
                Log.e(TAG, "Credential Manager unsupported on device", e)
                onAuthError(
                    "Google Sign-In is not supported by Google Play Services on this device.",
                    null
                )
            } catch (e: Exception) {
                _verificationInProgress.value = false
                Log.e(TAG, "Google Sign-In failed", e)
                onAuthError(e.localizedMessage ?: "Sign in failed. Please try again.", null)
            }
        }
    }

    fun signOut(
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSignOutComplete: () -> Unit = {}
    ) {
        scope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clear credential state: ${e.message}")
                } finally {
                    _verificationInProgress.value = false
                    getAuthOrNull()?.signOut()
                    onSignOutComplete()
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
