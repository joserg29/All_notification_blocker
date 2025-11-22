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
-keep class com.projects.allnotificationblocker.blockthemall.data.db.entities.* { *; }
-keepclassmembers class com.projects.allnotificationblocker.blockthemall.data.db.entities.* { *; }
-keep class com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesManager { *; }
-keepclassmembers class com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesManager { *; }
# Keep Gson annotations
-keepattributes *Annotation*
-keepattributes Signature

# Gson specific classes
-keep class com.google.gson.stream.** { *; }

-keep class com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.NotificationsAdapter { *; }
-keep class com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.NotificationsAdapter$* { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
