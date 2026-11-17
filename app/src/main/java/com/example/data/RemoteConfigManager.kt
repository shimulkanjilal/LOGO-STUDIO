// © 2026 App Owner. All rights reserved.
package com.example.data

import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AccessGateMode {
    ALLOWED,
    DISABLED,
    UPDATE_REQUIRED
}

data class AppAccessState(
    val appEnabled: Boolean = true,
    val minVersionCode: Long = 1L,
    val noticeText: String = "",
    val installedVersionCode: Int = BuildConfig.VERSION_CODE,
    val isRefreshing: Boolean = false
) {
    val accessMode: AccessGateMode
        get() = evaluateAccessMode(
            appEnabled = appEnabled,
            installedVersionCode = installedVersionCode,
            minVersionCode = minVersionCode
        )

    companion object {
        fun evaluateAccessMode(
            appEnabled: Boolean,
            installedVersionCode: Int,
            minVersionCode: Long
        ): AccessGateMode {
            return when {
                !appEnabled -> AccessGateMode.DISABLED
                installedVersionCode.toLong() < minVersionCode -> AccessGateMode.UPDATE_REQUIRED
                else -> AccessGateMode.ALLOWED
            }
        }
    }
}

object RemoteConfigManager {
    const val KEY_APP_ENABLED = "app_enabled"
    const val KEY_MIN_VERSION_CODE = "min_version_code"
    const val KEY_NOTICE_TEXT = "notice_text"

    private val _accessState = MutableStateFlow(AppAccessState())
    val accessState: StateFlow<AppAccessState> = _accessState.asStateFlow()

    private fun getRemoteConfigOrNull(): FirebaseRemoteConfig? {
        return try {
            Firebase.remoteConfig
        } catch (_: Exception) {
            null
        }
    }

    fun initializeAndFetch() {
        val remoteConfig = getRemoteConfigOrNull() ?: return
        try {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0L else 3600L
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
                .continueWithTask {
                    remoteConfig.setDefaultsAsync(
                        mapOf(
                            KEY_APP_ENABLED to true,
                            KEY_MIN_VERSION_CODE to 1L,
                            KEY_NOTICE_TEXT to ""
                        )
                    )
                }
                .addOnSuccessListener {
                    applyCurrentValues(remoteConfig, isRefreshing = true)
                    remoteConfig.fetchAndActivate().addOnCompleteListener {
                        applyCurrentValues(remoteConfig, isRefreshing = false)
                    }
                }
                .addOnFailureListener {
                    _accessState.value = _accessState.value.copy(isRefreshing = false)
                }
        } catch (_: Exception) {
            _accessState.value = _accessState.value.copy(isRefreshing = false)
        }
    }

    fun refresh() {
        val remoteConfig = getRemoteConfigOrNull()
        if (remoteConfig == null) {
            _accessState.value = _accessState.value.copy(isRefreshing = false)
            return
        }
        try {
            _accessState.value = _accessState.value.copy(isRefreshing = true)
            remoteConfig.fetchAndActivate().addOnCompleteListener {
                applyCurrentValues(remoteConfig, isRefreshing = false)
            }
        } catch (_: Exception) {
            _accessState.value = _accessState.value.copy(isRefreshing = false)
        }
    }

    private fun applyCurrentValues(remoteConfig: FirebaseRemoteConfig, isRefreshing: Boolean) {
        try {
            val enabledVal = remoteConfig.getValue(KEY_APP_ENABLED)
            val enabled = if (enabledVal.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
                true
            } else {
                enabledVal.asBoolean()
            }

            val minVerVal = remoteConfig.getValue(KEY_MIN_VERSION_CODE)
            val minVersion = if (minVerVal.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
                1L
            } else {
                minVerVal.asLong().coerceAtLeast(1L)
            }

            val noticeVal = remoteConfig.getValue(KEY_NOTICE_TEXT)
            val notice = if (noticeVal.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
                ""
            } else {
                noticeVal.asString().trim()
            }

            _accessState.value = AppAccessState(
                appEnabled = enabled,
                minVersionCode = minVersion,
                noticeText = notice,
                installedVersionCode = BuildConfig.VERSION_CODE,
                isRefreshing = isRefreshing
            )
        } catch (_: Exception) {
            _accessState.value = _accessState.value.copy(isRefreshing = isRefreshing)
        }
    }
}
