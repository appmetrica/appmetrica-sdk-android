-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.reporterextension.impl'

-keep public class !io.appmetrica.analytics.reporterextension.impl.**, io.appmetrica.analytics.reporterextension.** {
    public *;
}
