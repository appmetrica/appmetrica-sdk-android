-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.certificates.impl'

-keep public class !io.appmetrica.analytics.certificates.impl.**, io.appmetrica.analytics.certificates.** {
    public *;
}
