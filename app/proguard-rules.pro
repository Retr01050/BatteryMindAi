# Add any ProGuard configurations here

# Keep TensorFlow Lite classes
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room classes
-keep class androidx.room.** { *; }

# Keep your app's classes
-keep class com.creativeideas.batterymindai.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Prevent obfuscation of native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
