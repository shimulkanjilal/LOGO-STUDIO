// © 2026 App Owner. All rights reserved.
package com.example.control

import android.content.Context
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Activity log. Consent na dile ar login na thakle kichui pathay na.
 * Shudhu screen/feature er nam pathay; photo, file, design kokhono na.
 * Admin dashboard er shathe milte (default) Firestore database use kore:
 *   users/{uid}  ar  events/{id}
 */
object ActivityTracker {
    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun db(): FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun currentUser() = try { Firebase.auth.currentUser } catch (_: Exception) { null }

    private fun allowed(): Boolean = appContext?.let { ConsentStore.isGranted(it) } ?: false

    private fun versionName(): String = try {
        val c = appContext!!
        c.packageManager.getPackageInfo(c.packageName, 0).versionName ?: "?"
    } catch (_: Exception) { "?" }

    /** App khulle / login hole call hoy: user record update kore + app_open log kore. */
    fun onAppOpen() {
        if (!allowed()) return
        val ctx = appContext ?: return
        val user = currentUser() ?: return
        val ref = db().collection("users").document(user.uid)
        ref.get()
            .addOnSuccessListener { snap ->
                val data = mutableMapOf<String, Any>(
                    "lastSeen" to FieldValue.serverTimestamp(),
                    "email" to (user.email ?: ""),
                    "name" to (user.displayName ?: ""),
                    "device" to "${Build.MANUFACTURER} ${Build.MODEL}",
                    "androidVersion" to Build.VERSION.RELEASE,
                    "appVersion" to versionName(),
                    "appVersionCode" to AppControl.versionCode(ctx)
                )
                if (!snap.exists()) data["firstSeen"] = FieldValue.serverTimestamp()
                ref.set(data, SetOptions.merge())
                    .addOnFailureListener { handleFirestoreError(it, OperationType.WRITE, "users/${user.uid}") }
            }
            .addOnFailureListener { handleFirestoreError(it, OperationType.GET, "users/${user.uid}") }
        log("app_open")
    }

    fun log(eventName: String, params: Map<String, Any> = emptyMap()) {
        if (!allowed()) return
        val user = currentUser() ?: return
        try {
            db().collection("events").add(
                mapOf(
                    "uid" to user.uid,
                    "event" to eventName,
                    "params" to params,
                    "appVersion" to versionName(),
                    "ts" to FieldValue.serverTimestamp()
                )
            ).addOnFailureListener { handleFirestoreError(it, OperationType.CREATE, "events") }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "events")
        }
    }
}
