-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.location.impl'

-keep public class !io.appmetrica.analytics.location.impl.**, io.appmetrica.analytics.location.** {
    public *;
}
