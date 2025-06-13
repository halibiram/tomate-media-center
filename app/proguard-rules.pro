# Proguard rules for the app module.
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in Android SDK tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If you use reflectionientaions of an interface
#-keepclassmembers class * implements android.os.Parcelable {
#  public static final android.os.Parcelable$Creator CREATOR;
#}
# If you use reflection on data classes
#-keepclassmembers class * extends kotlin.jvm.internal.Lambda {
#    <fields>;
#    <methods>;
#}
