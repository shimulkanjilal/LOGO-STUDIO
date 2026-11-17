// © 2026 App Owner. All rights reserved.
package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NubccAccentGradient
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StudioNeonBorderGradient

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "primary_button"
) {
    val shape = RoundedCornerShape(14.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .testTag(testTag),
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) Color(0x55FFFFFF) else Color(0x1EFFFFFF)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = NubccTextPrimary,
            disabledContainerColor = Color(0x2E4F46E5),
            disabledContentColor = Color(0x66FFFFFF)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(
                    brush = if (enabled) {
                        NubccAccentGradient
                    } else {
                        Brush.horizontalGradient(listOf(Color(0x334F46E5), Color(0x3306B6D4)))
                    },
                    shape = shape
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (enabled) NubccTextPrimary else Color(0x66FFFFFF)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.8.sp,
                    color = if (enabled) NubccTextPrimary else Color(0x66FFFFFF)
                )
            }
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "secondary_button"
) {
    val shape = RoundedCornerShape(14.dp)
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .testTag(testTag),
        shape = shape,
        border = BorderStroke(
            width = 1.2.dp,
            brush = if (enabled) {
                StudioNeonBorderGradient
            } else {
                Brush.linearGradient(listOf(Color(0x22FFFFFF), Color(0x22FFFFFF)))
            }
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xB3090F22),
            contentColor = NubccTextPrimary,
            disabledContentColor = Color(0x55FFFFFF)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (enabled) NubccCyan else Color(0x55FFFFFF)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
fun NubccBrandHeader(
    title: String,
    subtitle: String = "Photo Watermark & Batch Studio",
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x990D142C))
                    .border(1.dp, StudioNeonBorderGradient, CircleShape)
                    .testTag("nav_back_button")
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_revert),
                    contentDescription = "Back",
                    tint = NubccTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            // Logo Studio Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .border(1.2.dp, StudioNeonBorderGradient, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_studio_icon),
                    contentDescription = "Logo Studio Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(46.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 1.0.sp,
                color = NubccTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                letterSpacing = 0.4.sp,
                color = NubccTextSecondary,
                maxLines = 1
            )
        }

        if (actions != null) {
            actions()
        }
    }
}
