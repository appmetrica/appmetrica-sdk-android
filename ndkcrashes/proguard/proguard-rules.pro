-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.ndkcrashes.impl'

-keep public class !io.appmetrica.analytics.ndkcrashes.impl.**, io.appmetrica.analytics.ndkcrashes.** {
    public *;
}
