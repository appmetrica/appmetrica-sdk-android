-include ../../proguard/proguard-root-project.txt

-repackageclasses 'io.appmetrica.analytics.billing.impl'

-keep public class !io.appmetrica.analytics.billing.impl.**, io.appmetrica.analytics.billing.** {
    public *;
}
