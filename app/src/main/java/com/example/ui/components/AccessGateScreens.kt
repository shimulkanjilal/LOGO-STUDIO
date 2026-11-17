// © 2026 App Owner. All rights reserved.
package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NubccAmber
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccElectricViolet
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import com.example.ui.theme.studioAuraBackground

@Composable
fun AppDisabledLockScreen(
    isRefreshing: Boolean,
    onRetry: () -> Unit
) {
    AccessGateCardLayout(
        icon = Icons.Default.Block,
        iconTint = Color(0xFFF87171),
        title = "App Access Disabled",
        message = "Logo Studio is currently disabled by the app administrator. Please check back later or contact the app owner.",
        badgeText = "REMOTE LOCK",
        isRefreshing = isRefreshing,
        buttonTestTag = "gate_retry_button",
        onRetry = onRetry
    )
}

@Composable
fun UpdateRequiredGateScreen(
    installedVersionCode: Int,
    requiredVersionCode: Long,
    isRefreshing: Boolean,
    onRetry: () -> Unit
) {
    AccessGateCardLayout(
        icon = Icons.Default.SystemUpdate,
        iconTint = NubccAmber,
        title = "Update Required",
        message = "Your installed build (v$installedVersionCode) is older than the required version (v$requiredVersionCode). Please install the latest APK from the app owner.",
        badgeText = "v$installedVersionCode → v$requiredVersionCode+",
        isRefreshing = isRefreshing,
        buttonTestTag = "update_retry_button",
        onRetry = onRetry
    )
}

@Composable
private fun AccessGateCardLayout(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    message: String,
    badgeText: String,
    isRefreshing: Boolean,
    buttonTestTag: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .studioAuraBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(iconTint.copy(alpha = 0.16f))
                        .border(1.dp, iconTint.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NubccElectricViolet.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = NubccCyan
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NubccTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = NubccTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                if (isRefreshing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NubccCyan,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Checking status...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NubccTextSecondary
                        )
                    }
                } else {
                    PrimaryButton(
                        text = "CHECK AGAIN",
                        icon = Icons.Default.Refresh,
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = buttonTestTag
                    )
                }
            }
        }
    }
}

@Composable
fun NoticeBanner(
    noticeText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_notice_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NubccElectricViolet.copy(alpha = 0.14f))
                .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NubccCyan.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Announcement",
                    tint = NubccCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ADMIN NOTICE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = NubccAmber
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = noticeText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = NubccTextPrimary
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("home_notice_dismiss_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss notice",
                    tint = NubccTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
