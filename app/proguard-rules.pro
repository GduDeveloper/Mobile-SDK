# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes Exceptions,InnerClasses,*Annotation*,Signature,EnclosingMethod

-dontshrink
-dontoptimize

-dontwarn com.amap.api.**

-keepclassmembers enum * {
    public static <methods>;
}

-keep class com.gdu.** { *; }
-keep class com.lib.** { *; }
#不混淆 使用反射机制的类
-keepattributes Signature

-keepnames class * implements java.io.Serializable

-keep   class com.amap.api.maps.**{*;}
-keep   class com.autonavi.**{*;}
-keep   class com.amap.api.trace.**{*;}