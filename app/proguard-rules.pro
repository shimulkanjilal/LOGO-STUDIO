# Add project specific ProGuard rules here.
# Preserve source file and line numbers for crash diagnostics while obfuscating names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database entities, DAOs, and generated implementations
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-keep class com.example.data.entity.** { *; }
-keep class com.example.model.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Firebase Remote Config, Firebase Auth, Firestore, Credential Manager & Google Play Services Tasks
-keep class com.google.firebase.remoteconfig.** { *; }
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.FirebaseApp { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class com.example.control.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn io.perfmark.**
