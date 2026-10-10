# DeepRoot — R8 rules (security-first: obfuscate everything, keep only what's required)

# --- AndroidX / Compose (safe defaults) ---
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.**

# --- Firebase ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- OkHttp / logging interceptor ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# Hide original file names in stack traces (harder to reverse-engineer)
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Keep app entry points only (everything else gets obfuscated)
-keep class com.deeproot.app.DeepRootApplication { *; }
-keep class com.deeproot.app.MainActivity { *; }
