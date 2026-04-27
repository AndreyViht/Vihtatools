# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }
-keep interface androidx.datastore.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-keep interface kotlinx.coroutines.** { *; }

# Keep our app classes
-keep class com.vihttools.mobile.** { *; }
-keep interface com.vihttools.mobile.** { *; }

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
