-include ../../proguard/proguard-root-project.txt
-dontwarn com.google.android.gms.**

-repackageclasses 'io.appmetrica.analytics.gpllibrary.impl'

-keep class !io.appmetrica.analytics.gpllibrary.impl.**, io.appmetrica.analytics.gpllibrary.** {
    public *;
}
