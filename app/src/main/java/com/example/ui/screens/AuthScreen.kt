// © 2026 App Owner. All rights reserved.
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.AuthManager
import com.example.ui.components.GlassCard
import com.example.ui.components.NubccMainClubLogo
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.NubccAmber
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccElectricViolet
import com.example.ui.theme.NubccTextMuted
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StudioNeonBorderGradient
import com.example.ui.theme.studioAuraBackground

enum class AuthMode(val label: String) {
    SIGN_IN("Sign In"),
    SIGN_UP("Sign Up")
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var selectedMode by rememberSaveable { mutableStateOf(AuthMode.SIGN_IN) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var suggestedMode by remember { mutableStateOf<AuthMode?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .studioAuraBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x4D8B5CF6),
                                        Color(0x2822D3EE),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension * 0.62f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.5.dp, StudioNeonBorderGradient, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        NubccMainClubLogo(modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "LOGO",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        color = NubccCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STUDIO",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        color = NubccTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Photo Watermark & Batch Overlay Suite",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp,
                    color = NubccAmber
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Sign In / Sign Up Mode Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xCC060916))
                                .border(1.dp, Color(0x336366F1), RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AuthMode.entries.forEach { mode ->
                                val isSelected = selectedMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(
                                            if (isSelected) NubccElectricViolet else Color.Transparent
                                        )
                                        .clickable(enabled = !isLoading) {
                                            selectedMode = mode
                                            errorMessage = null
                                            suggestedMode = null
                                        }
                                        .testTag(
                                            if (mode == AuthMode.SIGN_IN) "auth_tab_signin" else "auth_tab_signup"
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.label,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) NubccTextPrimary else NubccTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val isSignUp = selectedMode == AuthMode.SIGN_UP
                        Text(
                            text = if (isSignUp) "Create Your Member Account" else "Welcome Back",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = NubccTextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isSignUp) {
                                "Create a new Logo Studio account with your Google profile before signing in."
                            } else {
                                "Sign in with your already-registered Google account to open your workspace."
                            },
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = NubccTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x33EF4444))
                                    .border(1.dp, Color(0x66EF4444), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Authentication error",
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = errorMessage.orEmpty(),
                                        fontSize = 12.sp,
                                        color = Color(0xFFFEE2E2)
                                    )
                                }
                                val targetMode = suggestedMode
                                if (targetMode != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextButton(
                                        onClick = {
                                            selectedMode = targetMode
                                            errorMessage = null
                                            suggestedMode = null
                                        },
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .testTag("auth_switch_suggested_mode_button")
                                    ) {
                                        Text(
                                            text = "Switch to ${targetMode.label}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NubccCyan
                                        )
                                    }
                                }
                            }
                        }

                        if (isLoading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x337C3AED)),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = NubccCyan,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isSignUp) "Creating account..." else "Signing in...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NubccTextPrimary
                                )
                            }
                        } else {
                            PrimaryButton(
                                text = if (isSignUp) "SIGN UP WITH GOOGLE" else "SIGN IN WITH GOOGLE",
                                icon = if (isSignUp) Icons.Default.PersonAdd else Icons.Default.AccountCircle,
                                onClick = {
                                    errorMessage = null
                                    suggestedMode = null
                                    isLoading = true
                                    AuthManager.signInWithGoogle(
                                        context = context,
                                        credentialManager = credentialManager,
                                        scope = scope,
                                        mode = selectedMode,
                                        onAuthSuccess = {
                                            isLoading = false
                                            onAuthSuccess()
                                        },
                                        onAuthError = { err, targetMode ->
                                            isLoading = false
                                            errorMessage = err
                                            suggestedMode = targetMode
                                        },
                                        onAuthCancelled = {
                                            isLoading = false
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = if (isSignUp) "google_sign_up_button" else "google_sign_in_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isSignUp) {
                                "New users must complete Sign Up here before Sign In will allow access."
                            } else {
                                "Only registered accounts can Sign In. New here? Switch to the Sign Up tab first."
                            },
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = NubccTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureBadge(
                        icon = Icons.Default.HighQuality,
                        label = "Original Res",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureBadge(
                        icon = Icons.Default.Layers,
                        label = "Batch Studio",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureBadge(
                        icon = Icons.Default.AutoFixHigh,
                        label = "Smart Place",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureBadge(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NubccCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = NubccTextSecondary,
                maxLines = 1
            )
        }
    }
}
