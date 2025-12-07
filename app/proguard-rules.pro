# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.r3d1.interval.data.model.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Glance widget
-keep class androidx.glance.** { *; }
