# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/marcel/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#Line numbers
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

#ProtWall
-keepnames class com.protoolapps.firewall.** { *; }

#JNI
-keepclasseswithmembernames class * {
    native <methods>;
}

#JNI callbacks
-keep class com.protoolapps.firewall.Allowed { *; }
-keep class com.protoolapps.firewall.Packet { *; }
-keep class com.protoolapps.firewall.ResourceRecord { *; }
-keep class com.protoolapps.firewall.Usage { *; }
-keep class com.protoolapps.firewall.ServiceSinkhole {
    void nativeExit(java.lang.String);
    void nativeError(int, java.lang.String);
    void logPacket(com.protoolapps.firewall.Packet);
    void dnsResolved(com.protoolapps.firewall.ResourceRecord);
    boolean isDomainBlocked(java.lang.String);
    com.protoolapps.firewall.Allowed isAddressAllowed(com.protoolapps.firewall.Packet);
    void accountUsage(com.protoolapps.firewall.Usage);
}

#Support library
-keep class android.support.v7.widget.** { *; }
-dontwarn android.support.v4.**

#Picasso
-dontwarn com.squareup.okhttp.**

#AdMob
-dontwarn com.google.android.gms.internal.**

#SweetAlertDialog
-keep class cn.pedant.** { *; }
