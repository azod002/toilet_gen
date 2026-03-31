# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.toiletgen.core.network.model.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }

# Room
-keep class * extends androidx.room.RoomDatabase
