# ProGuard rules for Android NetTools

# SSHJ and related classes
-keep class net.schmizz.** { *; }
-keep class com.hierynomus.** { *; }
-dontwarn net.schmizz.**
-dontwarn com.hierynomus.**

# Bouncy Castle
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# SLF4J (used by SSHJ)
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

# Keep Kotlin serialization
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# General Android
-keepattributes SourceFile,LineNumberTable
