# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Magisk related classes
-keep class com.topjohnwu.magisk.** { *; }
-keep class com.dfroot.root.** { *; }
-keep class com.dfroot.hide.** { *; }
-keep class com.dfroot.exploit.** { *; }

# Keep model classes
-keep class com.dfroot.model.** { *; }

# Keep service classes
-keep class com.dfroot.service.** { *; }

# Keep utility classes
-keep class com.dfroot.utils.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

# Timber
-dontwarn timber.log.Timber

# Coroutines
-keepclassmembers class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepclassmembers class kotlinx.coroutines.CoroutineExceptionHandler {}

# Biometric
-keep class androidx.biometric.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep Parcelable
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Enum
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}