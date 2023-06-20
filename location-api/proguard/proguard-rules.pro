-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.locationapi.impl'

-keep public class !io.appmetrica.analytics.locationapi.impl.**, io.appmetrica.analytics.locationapi.** {
    public *;
}
