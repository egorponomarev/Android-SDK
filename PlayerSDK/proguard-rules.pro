# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/mharkins/Programming/Android/android-sdk-mac_x86/tools/proguard/proguard-android.txt
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


## androidsvg - ignore the ref to its R class
-dontwarn com.caverock.androidsvg.**

## okhttp
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

## otto
-keepattributes *Annotation*, Signature, Exception
-keepclassmembers class fm.feed.** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

## okio
# https://github.com/square/okio/issues/42
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

## retrofit
# http://square.github.io/retrofit/

-dontwarn retrofit.**
-keep class retrofit.** { *; }
#-keepattributes Signature
#-keepattributes Exceptions

## gson
# http://google-gson.googlecode.com/svn/trunk/examples/android-proguard-example/proguard.cfg

# added above, with the *Annotation* rule
#-keepattributes Signature

-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

-keep class fm.feed.android.playersdk.service.webservice.model.* { *; }
-keep class fm.feed.android.playersdk.model.* { *; }

-keep class fm.feed.android.playersdk.fragment.* { *; }
-keep class fm.feed.android.playersdk.view.* { *; }
