// © 2026 App Owner. All rights reserved.
package com.example.control

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccElectricViolet
import com.example.ui.theme.NubccSurfaceDark
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import com.example.ui.theme.SpaceGroteskFontFamily

object ConsentStore {
    private const val PREFS_NAME = "studio_consent_prefs"
    private const val KEY_ANSWERED = "consent_answered"
    private const val KEY_GRANTED = "consent_granted"

    fun isAnswered(context: Context): Boolean {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ANSWERED, false)
    }

    fun isGranted(context: Context): Boolean {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_GRANTED, false)
    }

    fun saveConsent(context: Context, granted: Boolean) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ANSWERED, true)
            .putBoolean(KEY_GRANTED, granted)
            .apply()
    }
}

@Composable
fun ConsentDialog(
    onAnswered: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        containerColor = NubccSurfaceDark,
        titleContentColor = NubccTextPrimary,
        textContentColor = NubccTextSecondary,
        title = {
            Text(
                text = "Activity & Diagnostics Consent",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NubccTextPrimary
            )
        },
        text = {
            Text(
                text = "Allow Logo Studio to collect your name, email, device model, app version and which screens you open, to help improve and maintain the app? We never see your photos or logos. All watermark editing works either way.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = NubccTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    ConsentStore.saveConsent(context, granted = true)
                    onAnswered()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NubccElectricViolet,
                    contentColor = NubccTextPrimary
                ),
                modifier = Modifier.testTag("consent_allow_button")
            ) {
                Text("Allow", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    ConsentStore.saveConsent(context, granted = false)
                    onAnswered()
                },
                modifier = Modifier.testTag("consent_decline_button")
            ) {
                Text(
                    text = "Decline",
                    color = NubccCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
