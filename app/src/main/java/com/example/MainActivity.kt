// © 2026 App Owner. All rights reserved.
package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.control.ActivityTracker
import com.example.control.AppConfig
import com.example.control.AppControl
import com.example.control.BlockScreen
import com.example.control.ConsentDialog
import com.example.control.ConsentStore
import com.example.data.AuthManager
import com.example.model.LogoPlacement
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BatchStudioScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.RecentExportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SingleEditorScreen
import com.example.ui.theme.NubccBlackBottom
import com.example.ui.theme.NubccTheme
import com.example.ui.viewmodel.BatchStudioViewModel
import com.example.ui.viewmodel.SingleEditorViewModel
import com.google.firebase.auth.FirebaseUser

sealed class Screen {
    object Home : Screen()
    data class SingleEditor(val initialUri: Uri? = null) : Screen()
    data class BatchStudio(val initialUris: List<Uri> = emptyList()) : Screen()
    object Presets : Screen()
    object RecentExports : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ActivityTracker.init(applicationContext)
        setContent {
            NubccTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NubccBlackBottom
                ) {
                    NubccStudioApp()
                }
            }
        }
    }
}

@Composable
fun NubccStudioApp() {
    val context = LocalContext.current
    val config by remember { AppControl.configFlow() }.collectAsState(initial = AppConfig())

    if (!config.enabled) {
        BlockScreen(title = "Maintenance", message = config.maintenanceMessage, buttonUrl = null)
        return
    }
    if (AppControl.versionCode(context) < config.minVersionCode) {
        BlockScreen(
            title = "Update required",
            message = "A new version is available. Please update to continue.",
            buttonUrl = config.updateUrl.ifBlank { null }
        )
        return
    }

    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val currentUser by AuthManager.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = AuthManager.currentUserOrNull())

    LaunchedEffect(Unit) {
        if (AuthManager.currentUserOrNull() == null) {
            AuthManager.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { }
        )
    } else {
        key(user.uid) {
            AuthenticatedStudioContent(
                user = user,
                noticeText = config.announcement,
                onSignOut = {
                    AuthManager.signOut(
                        credentialManager = credentialManager,
                        scope = scope
                    )
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedStudioContent(
    user: FirebaseUser,
    noticeText: String,
    onSignOut: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val context = LocalContext.current
    var consentAnswered by remember { mutableStateOf(ConsentStore.isAnswered(context)) }
    if (!consentAnswered) {
        ConsentDialog(onAnswered = { consentAnswered = true })
    }
    LaunchedEffect(consentAnswered, user.uid) {
        if (consentAnswered) ActivityTracker.onAppOpen()
    }
    LaunchedEffect(currentScreen, consentAnswered) {
        if (consentAnswered) {
            ActivityTracker.log(
                "screen_view",
                mapOf("screen" to (currentScreen::class.simpleName ?: "unknown"))
            )
        }
    }

    val singleEditorViewModel: SingleEditorViewModel = viewModel()
    val batchStudioViewModel: BatchStudioViewModel = viewModel()
    val userDisplayName = user.displayName?.takeIf { it.isNotBlank() } ?: "Studio Member"
    val userEmail = user.email.orEmpty()

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                noticeText = noticeText,
                userDisplayName = userDisplayName,
                userEmail = userEmail,
                onSignOut = onSignOut,
                onNavigateToSinglePhoto = { uri ->
                    if (uri != null) {
                        singleEditorViewModel.setPhoto(uri)
                    }
                    currentScreen = Screen.SingleEditor(uri)
                },
                onNavigateToBatch = { uris ->
                    if (uris.isNotEmpty()) {
                        batchStudioViewModel.addPhotos(uris)
                    }
                    currentScreen = Screen.BatchStudio(uris)
                },
                onNavigateToPresets = {
                    currentScreen = Screen.Presets
                },
                onNavigateToRecentExports = {
                    currentScreen = Screen.RecentExports
                },
                onNavigateToSettings = {
                    currentScreen = Screen.Settings
                }
            )
        }

        is Screen.SingleEditor -> {
            SingleEditorScreen(
                viewModel = singleEditorViewModel,
                initialPhotoUri = screen.initialUri,
                onNavigateBack = { currentScreen = Screen.Home }
            )
        }

        is Screen.BatchStudio -> {
            BatchStudioScreen(
                viewModel = batchStudioViewModel,
                initialUris = screen.initialUris,
                onNavigateBack = { currentScreen = Screen.Home }
            )
        }

        is Screen.Presets -> {
            val singleState by singleEditorViewModel.uiState.collectAsState()
            val batchState by batchStudioViewModel.uiState.collectAsState()
            val activePlacement = when {
                singleState.layers.isNotEmpty() -> singleState.placement
                batchState.globalLayers.isNotEmpty() -> batchState.globalPlacement
                else -> null
            }
            PresetsScreen(
                onNavigateBack = { currentScreen = Screen.Home },
                onSelectPreset = { placement: LogoPlacement ->
                    singleEditorViewModel.updatePlacement(placement)
                    batchStudioViewModel.updateGlobalPlacement(placement, recordHistory = true)
                },
                currentPlacement = activePlacement
            )
        }

        is Screen.RecentExports -> {
            RecentExportsScreen(
                onNavigateBack = { currentScreen = Screen.Home }
            )
        }

        is Screen.Settings -> {
            SettingsScreen(
                userDisplayName = userDisplayName,
                userEmail = userEmail,
                onSignOut = onSignOut,
                onNavigateBack = { currentScreen = Screen.Home }
            )
        }
    }
}
