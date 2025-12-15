-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.networkokhttp.impl'

-keep public class !io.appmetrica.analytics.networkokhttp.impl.**, io.appmetrica.analytics.networkokhttp.** {
    public *;
}
