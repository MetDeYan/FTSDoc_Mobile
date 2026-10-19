-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-keep class com.ftsdoc.mobile.data.remote.** { *; }

-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn org.bouncycastle.**
