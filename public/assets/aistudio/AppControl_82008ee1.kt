// © 2026 App Owner. All rights reserved.
package com.example.control

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// ---------------------------------------------------------------------------
// Admin dashboard theke edit kora setting (Firestore: config/app)
// Fail-open: config na pele ba internet na thakle app chalu thake.
// ---------------------------------------------------------------------------
data class AppConfig(
    val enabled: Boolean = true,
    val maintenanceMessage: String = "The app is under maintenance. Please try again later.",
    val minVersionCode: Long = 0L,
    val updateUrl: String = "",
    val announcement: String = ""
)

object AppControl {
    fun configFlow(): Flow<AppConfig> = callbackFlow {
        val registration = FirebaseFirestore.getInstance()
            .collection("config").document("app")
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    trySend(
                        AppConfig(
                            enabled = snap.getBoolean("enabled") ?: true,
                            maintenanceMessage = snap.getString("maintenanceMessage")
                                ?: AppConfig().maintenanceMessage,
                            minVersionCode = snap.getLong("minVersionCode") ?: 0L,
                            updateUrl = snap.getString("updateUrl") ?: "",
                            announcement = snap.getString("announcement") ?: ""
                        )
                    )
                }
            }
        awaitClose { registration.remove() }
    }

    fun versionCode(ctx: Context): Long = try {
        PackageInfoCompat.getLongVersionCode(ctx.packageManager.getPackageInfo(ctx.packageName, 0))
    } catch (e: Exception) { 0L }
}

@Composable
fun BlockScreen(title: String, message: String, buttonUrl: String?) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (buttonUrl != null) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = { uriHandler.openUri(buttonUrl) }) { Text("Update") }
        }
    }
}
