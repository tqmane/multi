# Add project specific ProGuard rules here.
-keep class com.tqmane.multiwindowpatch.** { *; }
-keepclasseswithmembers class * {
    public <init>(de.robv.android.xposed.XC_LoadPackage.LoadPackageParam);
}

# Keep debug attributes for ARM64 compatibility
-keepattributes SourceFile,LineNumberTable
-dontobfuscate
