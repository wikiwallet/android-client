# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class userId to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this tro
# hide the original source file userId.
#-renamesourcefileattribute SourceFile
-keep class io.golos.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep class org.slf4j.impl.** { *; }
