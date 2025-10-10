-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.idsync.impl'

-keep public class !io.appmetrica.analytics.idsync.impl.**, io.appmetrica.analytics.idsync.** {
    public *;
}
