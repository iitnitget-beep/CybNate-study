# CybNate release hardening rules (R8)
#
# Minify + obfuscation are enabled in build.gradle.kts. Firebase/Compose ship
# their own consumer rules, so only app-specific keep rules live here.

# Keep line numbers & source names minimally for readable release crash reports
# while still allowing full obfuscation. The original file name is hidden.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# AppCheck debug provider is never present in release; do not keep it.
-dontwarn com.google.firebase.appcheck.debug.**
