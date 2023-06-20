-keep class io.appmetrica.analytics.** { *; }

-keep class com.android.installreferrer.api.* { *; }
-dontwarn com.android.installreferrer.api.*
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient** { *; }

-keep class kotlin.KotlinVersion { *; }

# version of com.android.billingclient:billing is determined using this class
-keep public class com.android.billingclient.BuildConfig { *; }
-keep public class com.google.android.gms.appset.AppSet

-keep class com.unity3d.player.UnityPlayer
-keep class mono.MonoPackageManager
-keep class org.apache.cordova.CordovaPlugin
-keep class com.facebook.react.ReactRootView
-keep class io.flutter.embedding.engine.FlutterEngine

-keepattributes *Annotation*

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
