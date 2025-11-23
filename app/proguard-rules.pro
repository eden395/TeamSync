# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /path/to/Android/sdk/tools/proguard/proguard-android.txt

-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
}

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
