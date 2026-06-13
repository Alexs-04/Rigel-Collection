# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

# Gson
-dontwarn com.google.gson.**
-keep class com.google.**
-keep interface com.google.gson.**
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Coil
-dontwarn coil.**
-keep class coil.** { *; }

# Keep data classes
-keep class com.korebit.rigel.data.** { *; }
-keepclassmembers class com.korebit.rigel.data.** { *; }

# Keep composables
-keep @androidx.compose.runtime.Composable class * { *; }

